/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.entity.distpacher

import org.vicky.platform.entity.*
import org.vicky.platform.utils.ResourceLocation
import org.vicky.platform.world.PlatformBlock
import org.vicky.utilities.ContextLogger.AsyncContextLogger
import org.vicky.utilities.ContextLogger.ContextLogger
import org.vicky.utilities.Pair
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.random.Random

/** Global compiled task registry (you already have BookBasedAntagnosticCompiler.compile) */
object CompiledTaskRegistry {
    private val tasks = ConcurrentHashMap<ResourceLocation, CompiledTask>()
    fun register(task: CompiledTask) { tasks[task.id] = task }
    fun get(id: ResourceLocation) = tasks[id]
}

/** Per-entity lightweight state — keep very small. */
class EntityTaskState {
    var cooldownTicksRemaining: MutableMap<ResourceLocation, Int> = mutableMapOf()
    val activeEntityTimed = ArrayList<ActiveTimedAction>(4) // small pre-sized list
    val activeBlockTimed = ArrayList<ActiveTimedBlockAction>(4) // small pre-sized list
    val lastSelectorTick = mutableMapOf<ResourceLocation, Long>() // throttle per-selector per-entity
    val assignedTasks = LinkedHashMap<ResourceLocation, Pair<Int, Map<String, Any>>>(4) // list of task IDs assigned to this entity
    // NEW: tasks currently blocked waiting for signals:
    // map: taskId -> remainingSignalsSet

    val executions = mutableMapOf<ResourceLocation, TaskExecutionState>()

    val lastSelectorResult: MutableMap<ResourceLocation, Any?> = HashMap()
    val waitingTasks = LinkedHashMap<ResourceLocation, MutableSet<String>>(4)
}
data class TaskExecutionState(
    val taskId: ResourceLocation,
    var phase: Int = 0,
    var waitingForBlocking: Boolean = false,
    val activeBlockingIds: MutableSet<ResourceLocation> = mutableSetOf()
)
class TaskInstance(
    val compiled: CompiledTask,
    val priority: Int,
    val parameters: Map<String, Any> = HashMap()
)

/** Manager that owns entity states; use WeakHashMap or remove entries when entity dies. */
object EntityTaskManager {
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors().coerceAtMost(4))
    private val states = ConcurrentHashMap<UUID, EntityTaskState>()
    private val LOGGER = AsyncContextLogger(ContextLogger.ContextType.SYSTEM, "ENTITY-TASK-MANAGER")

    // small config
    private var selectorThrottleTicks = 4L
    private var maxActiveTimedPerEntity = 6

    private fun stateFor(entity: PlatformLivingEntity): EntityTaskState =
        states.computeIfAbsent(entity.uuid) { EntityTaskState() }

    fun removeEntity(entity: PlatformLivingEntity) {
        states.remove(entity.uuid)
        SignalManager.clearEntity(entity.uuid)
        TriggerManager.clearEntity(entity.uuid)
    }

    fun onStopSignal(entity: PlatformLivingEntity, signal: String, extraData: Map<String, Any> = emptyMap()) {
        val st = states[entity.uuid] ?: return

        // Cancel entity-timed actions belonging to tasks that stop on this signal
        val itEnt = st.activeEntityTimed.listIterator()
        while (itEnt.hasNext()) {
            val a = itEnt.next()
            val task = CompiledTaskRegistry.get(a.taskId)
            if (task != null && task.stopSignals.contains(signal)) {
                // call onEnd so the timed action can cleanly end
                try {
                    a.compiled.onEnd(a.self, a.targetEntity, a.params)
                } catch (ex: Exception) {
                    LOGGER.debug("Exception while ending timed action ${a.compiled.id} for ${entity.uuid}: $ex")
                }
                itEnt.remove()

                if (task.lifecycle == TaskLifecycle.ONE_SHOT)
                    st.assignedTasks.remove(a.taskId)
                else
                    st.cooldownTicksRemaining[task.id] = task.defaultCooldownTicks
            }
        }

        // Cancel block-timed actions similarly
        val itBlock = st.activeBlockTimed.listIterator()
        while (itBlock.hasNext()) {
            val a = itBlock.next()
            val task = CompiledTaskRegistry.get(a.taskId)
            if (task != null && task.stopSignals.contains(signal)) {
                try {
                    a.compiled.onEnd(a.self, a.targetBlock, a.params)
                } catch (ex: Exception) {
                    LOGGER.debug("Exception while ending block timed action ${a.compiled.id} for ${entity.uuid}: $ex")
                }
                itBlock.remove()
                st.assignedTasks.remove(a.taskId)
            }
        }
    }

    fun onSignalReceived(entity: PlatformLivingEntity, signal: String, extraData: Map<String, Any> = emptyMap()) {
        val st = states[entity.uuid] ?: return
        val toActivate = mutableListOf<ResourceLocation>()

        val it = st.waitingTasks.entries.iterator()
        while (it.hasNext()) {
            val (taskId, remaining) = it.next()
            if (remaining.remove(signal)) {
                // merge any extraData into assignedTask params if desired:
                val prevParams = st.assignedTasks[taskId] ?: (0 to emptyMap())
                val merged = HashMap(prevParams.value)
                merged.putAll(extraData)
                st.assignedTasks[taskId] = prevParams.key to merged

                if (remaining.isEmpty()) {
                    toActivate += taskId
                    it.remove()
                }
                toActivate.forEach { tid ->
                    // ensure instant eligibility (or decide a small wait)
                    st.cooldownTicksRemaining[tid] = 0
                }
            }
        }
    }

    private fun setTaskCooldown(st: EntityTaskState, taskId: ResourceLocation, ticks: Int) {
        if (ticks <= 0) st.cooldownTicksRemaining.remove(taskId) else st.cooldownTicksRemaining[taskId] = ticks
    }
    private fun isTaskOnCooldown(st: EntityTaskState, taskId: ResourceLocation) =
        (st.cooldownTicksRemaining[taskId] ?: 0) > 0


    /** Assign a compiled task to an entity (store ID only). */
    fun assignTask(entity: PlatformLivingEntity, taskId: ResourceLocation, params: Map<String, Any> = emptyMap(), priority: Int) {
        val compiled = CompiledTaskRegistry.get(taskId) ?: error("No compiled task $taskId")
        val st = stateFor(entity)

        // store / merge params like before
        val existing = st.assignedTasks[taskId]
        if (existing == null) {
            st.assignedTasks[taskId] = priority to params
        } else {
            val merged = HashMap(existing.value)
            merged.putAll(params)
            st.assignedTasks[taskId] = priority to merged
        }

        // If this compiled task must wait for signals, mark it and subscribe
        if (compiled.waitForSignals.isNotEmpty()) {
            // copy set so we can mutate remaining signals
            val remaining = compiled.waitForSignals.toMutableSet()
            st.waitingTasks[taskId] = remaining
            // subscribe to each signal name (so SignalManager.fire will assign/notify)
            for (sig in compiled.waitForSignals) {
                SignalManager.subscribe(entity.uuid, sig, taskId, params, priority)
            }
            LOGGER.debug("Task $taskId for ${entity.uuid} will wait for signals=${compiled.waitForSignals}", ContextLogger.LogType.BASIC)
        } else {
            LOGGER.debug("Assigned task $taskId to ${entity.uuid} with params=$params", ContextLogger.LogType.BASIC)
        }
    }


    fun clearTasks(entity: PlatformLivingEntity) {
        states[entity.uuid]?.assignedTasks?.clear()
    }

    /** Called every server tick (or from per-entity tick) */
    fun tickEntity(entity: PlatformLivingEntity, worldTick: Long) {
        val st = states[entity.uuid] ?: return

        tickActiveTimed(entity, st)

        val keys = st.cooldownTicksRemaining.keys.toList()
        for (k in keys) {
            val v = st.cooldownTicksRemaining[k] ?: continue
            if (v > 1) st.cooldownTicksRemaining[k] = v - 1
            else st.cooldownTicksRemaining.remove(k) // remove zeros to keep map small
        }

        if (st.assignedTasks.isEmpty()) return

        val candidates = st.assignedTasks
            .mapNotNull { val task = CompiledTaskRegistry.get(it.key)
                if (task != null) task to it.value else null }
            .sortedWith(compareByDescending<Pair<CompiledTask, Pair<Int, Map<String, Any>>>> { it.value.key }
                .thenBy { Random.nextFloat() })

        val tasksToRemove = mutableListOf<ResourceLocation>()

        for (pair in candidates) {
            val task = pair.first
            val possibleCooldown = (pair.second.second["cooldown"] as? Number)?.toInt() ?:
                (pair.second.second["cooldownTicks"] as? Number)?.toInt() ?: task.defaultCooldownTicks
            val instance = TaskInstance(pair.key, pair.value.key, pair.value.value)

            when (tryRunTaskOnce(entity, instance, st, worldTick)) {
                TaskRunOutcome.NOT_RUN -> continue
                TaskRunOutcome.RAN_INSTANT -> {
                    st.cooldownTicksRemaining[task.id] = possibleCooldown.coerceAtLeast(0)
                    if (task.lifecycle == TaskLifecycle.ONE_SHOT) tasksToRemove += task.id
                    break
                }
                TaskRunOutcome.RAN_SCHEDULED -> {
                    st.cooldownTicksRemaining[task.id] = possibleCooldown.coerceAtLeast(0)
                    // for scheduled tasks: if ONE_SHOT and no other timers, mark for removal only after timers end
                    if (task.lifecycle == TaskLifecycle.ONE_SHOT && !hasActiveTimersForTask(st, task.id)) {
                        tasksToRemove += task.id
                    }
                    break
                }
                TaskRunOutcome.COMPLETED -> {
                    tasksToRemove += task.id
                    break
                }
            }
        }

        if (tasksToRemove.isNotEmpty()) {
            tasksToRemove.toSet().forEach {
                st.assignedTasks.remove(it)
                st.cooldownTicksRemaining.remove(it)
            }
        }
    }

    private fun hasActiveTimersForTask(st: EntityTaskState, taskId: ResourceLocation): Boolean {
        return st.activeEntityTimed.any { it.taskId == taskId } || st.activeBlockTimed.any { it.taskId == taskId }
    }

    enum class TaskRunOutcome { NOT_RUN, RAN_INSTANT, RAN_SCHEDULED, COMPLETED }

    private fun tryRunTaskOnce(entity: PlatformLivingEntity, instance: TaskInstance, st: EntityTaskState, worldTick: Long): TaskRunOutcome {
        // Walk the task steps implementing the same semantics as executeCompiledTask
        var working: AmountableResult<PlatformLivingEntity>? = null
        var workingBlock: AmountableResult<PlatformBlock<*>>? = null
        val ctx = SelectorContext.ofEntity(entity)
        val task = instance.compiled

        if (isTaskOnCooldown(st, task.id)) return TaskRunOutcome.NOT_RUN
        if (st.waitingTasks.containsKey(task.id)) {
            LOGGER.debug("Task ${task.id} is waiting on signals=${st.waitingTasks[task.id]} for entity ${entity.uuid}")
            return TaskRunOutcome.NOT_RUN
        }

        for (step in task.steps) {
            when (step) {
                is CompiledStep.EntitySelectorStep -> {
                    val last = st.lastSelectorTick[step.selector.id] ?: Long.MIN_VALUE
                    var result = st.lastSelectorResult[step.selector.id]

                    if (worldTick - last >= selectorThrottleTicks || result == null) {
                        result = step.selector.select(ctx, instance.parameters)

                        st.lastSelectorTick[step.selector.id] = worldTick
                        st.lastSelectorResult[step.selector.id] = result
                        LOGGER.debug("Task ${task.id} entity selector has gathered targets: ${result.size()}")
                    }

                    working = result as AmountableResult<PlatformLivingEntity>?

                    if (working == null) {
                        LOGGER.debug("Task ${task.id} entity selector ${step.selector.id} returned no targets — skipping")
                        return TaskRunOutcome.NOT_RUN
                    }
                }
                is CompiledStep.EntityConditionStep -> {
                    val dto = step.dto
                    val isForTarget = dto.isForTarget ?: true
                    if (!isForTarget) {
                        val necessary = step.compiled.filter { it.mustBeTrue }
                        val optional = step.compiled.filter { !it.mustBeTrue }
                        val necessaryOk = necessary.all { it.test(entity, instance.parameters) }
                        val optionalOk = optional.isEmpty() || optional.any { it.test(entity, instance.parameters) }
                        if (!necessaryOk || !optionalOk) return TaskRunOutcome.NOT_RUN // gate failed
                    } else {
                        // filter working set
                        if (working == null) return TaskRunOutcome.NOT_RUN
                        working = when (working.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> {
                                val t = working.getSingleResult()
                                if (t == null || !step.compiled.all { it.test(t, instance.parameters) }) null
                                else working
                            }
                            else -> {
                                val filtered = working.getResults().filter { ent -> step.compiled.all { it.test(ent, instance.parameters) } }
                                if (filtered.isEmpty()) null else AmountableResult(ResultType.MULTIPLE, filtered)
                            }
                        }
                        if (working == null) return TaskRunOutcome.NOT_RUN
                    }
                }
                is CompiledStep.EntityActionStep -> {
                    val dto = step.dto
                    val isOnTarget = dto.isOnTarget ?: true

                    // Instant actions
                    if (isOnTarget) {
                        if (working == null) return TaskRunOutcome.NOT_RUN
                        when (working.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> working.getSingleResult()?.let { t ->
                                step.compiledAction.forEach { it.run(entity, t, instance.parameters) }
                            }
                            else -> working.getResults().forEach { t ->
                                step.compiledAction.forEach { it.run(entity, t, instance.parameters) }
                            }
                        }
                    } else {
                        step.compiledAction.forEach { it.run(entity, null, instance.parameters) }
                    }

                    // Timed actions scheduling
                    // Avoid scheduling if entity already busy / exceeds max
                    if (st.activeEntityTimed.size >= maxActiveTimedPerEntity) {
                        // skip scheduling timed actions to avoid overcommit
                        continue
                    }

                    if (isOnTarget) {
                        if (working == null) continue
                        when (working.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> {
                                working.getSingleResult()?.let { t ->
                                    scheduleTimedList(st, entity, listOf(t), instance, step)
                                }
                            }
                            else -> {
                                scheduleTimedList(st, entity, working.getResults(), instance, step)
                            }
                        }
                    } else {
                        scheduleTimedList(st, entity, listOf(null), instance, step)
                    }
                }
                is CompiledStep.BlockSelectorStep -> {
                    val last = st.lastSelectorTick[step.selector.id] ?: Long.MIN_VALUE
                    var result = st.lastSelectorResult[step.selector.id]

                    if (worldTick - last >= selectorThrottleTicks || result == null) {
                        result = step.selector.select(ctx, instance.parameters)

                        st.lastSelectorTick[step.selector.id] = worldTick
                        st.lastSelectorResult[step.selector.id] = result
                        LOGGER.debug("Task ${task.id} block selector has gathered targets: ${result.size()}")
                    }

                    workingBlock = result as AmountableResult<PlatformBlock<*>>?

                    if (workingBlock == null) {
                        LOGGER.debug("Task ${task.id} selector ${step.selector.id} returned no targets — skipping")
                        return TaskRunOutcome.NOT_RUN
                    }
                }
                is CompiledStep.BlockConditionStep -> {
                    val dto = step.dto
                    val isForTarget = dto.isForTarget ?: true
                    if (!isForTarget) {

                    } else {
                        // filter working set
                        if (workingBlock == null) return TaskRunOutcome.NOT_RUN
                        workingBlock = when (workingBlock.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> {
                                val t = workingBlock.getSingleResult()
                                if (t == null || !step.compiledBlock.all { it.test(t, instance.parameters) }) null
                                else workingBlock
                            }
                            else -> {
                                val filtered = workingBlock.getResults().filter { ent -> step.compiledBlock.all { it.test(ent, instance.parameters) } }
                                if (filtered.isEmpty()) null else AmountableResult(ResultType.MULTIPLE, filtered)
                            }
                        }
                        if (working == null) return TaskRunOutcome.NOT_RUN
                    }
                }
                is CompiledStep.BlockActionStep -> {
                    val dto = step.dto
                    val isOnTarget = dto.isOnTarget ?: true

                    // Instant actions
                    if (isOnTarget) {
                        if (workingBlock == null) {
                            LOGGER.debug("Working block was null, cannot run intant.")
                            return TaskRunOutcome.NOT_RUN
                        }
                        when (workingBlock.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> workingBlock.getSingleResult()?.let { t ->
                                step.compiledBlockAction.forEach { it.run(entity, t, instance.parameters) }
                            }
                            else -> workingBlock.getResults().forEach { t ->
                                step.compiledBlockAction.forEach { it.run(entity, t, instance.parameters) }
                            }
                        }
                    }
                    else {

                    }

                    if (st.activeEntityTimed.size >= maxActiveTimedPerEntity) {
                        continue
                    }

                    if (isOnTarget) {
                        if (workingBlock == null) {
                            LOGGER.debug("Working block was null, cannot schedule.")
                            continue
                        }
                        when (workingBlock.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> {
                                workingBlock.getSingleResult()?.let { t ->
                                    LOGGER.debug("Scheduled task single for $t")
                                    scheduleTimedList(st, entity, listOf(t), instance, step)
                                }
                            }
                            else -> {
                                scheduleTimedList(st, entity, workingBlock.getResults(), instance, step)
                            }
                        }
                    }
                    else {
                        scheduleTimedList(st, entity, listOf(null), instance, step)
                    }
                }
            }
        }

        val hadTimed = st.activeEntityTimed.any { it.taskId == task.id } ||
                st.activeBlockTimed.any { it.taskId == task.id }

        return when {
            task.lifecycle == TaskLifecycle.ONE_SHOT && !hadTimed -> TaskRunOutcome.COMPLETED
            hadTimed -> TaskRunOutcome.RAN_SCHEDULED
            else -> TaskRunOutcome.RAN_INSTANT
        }
    }

    /** schedule timed actions for each target (list may contain null for owner-targeted ones) */
    private fun scheduleTimedList(
        st: EntityTaskState,
        owner: PlatformLivingEntity,
        targets: List<PlatformLivingEntity?>,
        instance: TaskInstance,
        step: CompiledStep.EntityActionStep
    ) {
        // step.dto.timedRefs aligns to compiledTimedAction list by index (we kept them matched on compile)
        val dtoTimedRefs = step.dto.entityTimedRefs
        val task = instance.compiled

        // for each target schedule each timedRef
        targets.forEach { target ->
            step.compiledTimedAction.forEachIndexed { idx, compiledTimed ->
                val ref = dtoTimedRefs.getOrNull(idx) ?: TimedRef(compiledTimed.id) // fallback
                // slot & runBlocking handling
                val slot = ref.slot
                if (!tryAcquireSlot(st, slot, task)) {
                    LOGGER.debug(
                        "Skipping timed ref $ref for task ${task.id}: " +
                                "slot $slot is owned by an equal/higher priority task"
                    )
                    return@forEachIndexed
                }

                // compute resolved duration and interval
                val ticksLeft = (instance.parameters[compiledTimed.id.toString() + ":duration"] as? Number)?.toInt() ?:
                    ref.defaultDuration ?: compiledTimed.defaultDuration
                val interval = (instance.parameters[compiledTimed.id.toString() + ":interval"] as? Number)?.toInt() ?:
                    ref.interval ?: compiledTimed.defaultInterval

                if (compiledTimed.onStart(owner, target, instance.parameters)) {
                    // create active timed
                    val active = ActiveTimedAction(compiledTimed, owner, target, instance.parameters,
                        ticksLeft, interval, 0, slot, task.id, compiledTimed.id)
                    st.activeEntityTimed.add(active)
                }
            }
        }
    }

    /** schedule timed actions for each target (list may contain null for owner-targeted ones) */
    private fun scheduleTimedList(
        st: EntityTaskState,
        owner: PlatformLivingEntity,
        targets: List<PlatformBlock<*>?>,
        instance: TaskInstance,
        step: CompiledStep.BlockActionStep
    ) {
        LOGGER.debug("Scheduling timed list for task ${instance.compiled.id} with owner ${owner.typeId} on ${targets.size} targets.")

        // step.dto.timedRefs aligns to compiledTimedAction list by index
        val dtoTimedRefs = step.dto.blockTimedActionRefs
        val task = instance.compiled

        targets.forEach { target ->
            if (target == null) {
                LOGGER.warn("Target is null, skipping.")
                return@forEach
            }

            step.compiledTimedAction.forEachIndexed { idx, compiledTimed ->
                val ref = dtoTimedRefs.getOrNull(idx) ?: TimedRef(compiledTimed.id) // fallback

                val slot = ref.slot
                if (!tryAcquireSlot(st, slot, task)) {
                    LOGGER.debug(
                        "Skipping timed ref $ref for task ${task.id}: " +
                                "slot $slot is owned by an equal/higher priority task"
                    )
                    return@forEachIndexed
                }


                val ticksLeft = (instance.parameters[compiledTimed.id.toString() + ":duration"] as? Number)?.toInt() ?:
                    ref.defaultDuration ?: compiledTimed.defaultDuration
                val interval = (instance.parameters[compiledTimed.id.toString() + ":interval"] as? Number)?.toInt() ?:
                    ref.interval ?: compiledTimed.defaultInterval

                LOGGER.debug("Scheduling timed action ${compiledTimed.id} for target ${target.location} with duration $ticksLeft and interval $interval.")

                if (compiledTimed.onStart(owner, target, instance.parameters)) {
                    val active = ActiveTimedBlockAction(
                        compiledTimed, owner, target, instance.parameters,
                        ticksLeft, interval, 0, slot, task.id, compiledTimed.id
                    )
                    st.activeBlockTimed.add(active)
                    LOGGER.debug("Added ActiveTimedBlockAction ${compiledTimed.id} for target ${target.location}, task ${task.id}, slot $slot.")
                } else {
                    LOGGER.warn("onStart returned false for timed action ${compiledTimed.id} on target ${target.location}. Skipping creation.")
                }
            }
        }
    }

    private fun tryAcquireSlot(
        st: EntityTaskState,
        slot: String?,
        newTask: CompiledTask
    ): Boolean {
        if (slot == null)
            return true

        val newPriority = st.assignedTasks[newTask.id]?.first ?: Int.MIN_VALUE

        val occupying = st.activeEntityTimed
            .filter { it.slot == slot }

        if (occupying.isEmpty())
            return true

        var acquired = false

        occupying.forEach { active ->
            val activePriority =
                st.assignedTasks[active.taskId]?.first ?: Int.MIN_VALUE

            if (newPriority > activePriority) {
                LOGGER.debug(
                    "Task ${newTask.id} (priority=$newPriority) is seizing slot $slot " +
                            "from task ${active.taskId} (priority=$activePriority) " +
                            "for entity ${active.self.uuid}"
                )

                // Cleanly terminate the old action.
                try {
                    active.end()
                } catch (ex: Exception) {
                    LOGGER.debug(
                        "Exception while preempting timed action ${active.compiled.id}: $ex"
                    )
                }

                st.activeEntityTimed.remove(active)
                acquired = true
            } else {
                LOGGER.debug(
                    "Task ${newTask.id} (priority=$newPriority) cannot acquire slot $slot; " +
                            "occupied by ${active.taskId} (priority=$activePriority)"
                )
            }
        }

        // If every occupant had higher/equal priority, we failed.
        return acquired || st.activeEntityTimed.none { it.slot == slot }
    }

    fun removeTask(entity: PlatformLivingEntity, taskId: ResourceLocation) {
        states[entity.uuid]?.assignedTasks?.remove(taskId)
        // optionally cancel active timers for that task:
        states[entity.uuid]?.let { st ->
            st.activeEntityTimed.removeIf { it.taskId == taskId } // call onEnd callbacks as needed
            st.activeBlockTimed.removeIf { it.taskId == taskId }
        }
    }

    private fun tickActiveTimed(entity: PlatformLivingEntity, st: EntityTaskState) {
        val all = mutableListOf<ActiveTimedBaseAction<out BaseCompiledTimedAction<*>, *>>()
        all.addAll(st.activeEntityTimed)
        all.addAll(st.activeBlockTimed)
        performOnIterator(st, entity, all.listIterator())
    }

    private fun performOnIterator(st: EntityTaskState, entity: PlatformLivingEntity, it: MutableListIterator<ActiveTimedBaseAction<out BaseCompiledTimedAction<*>, *>>) {
        val toRemove = mutableSetOf<ResourceLocation>()
        while (it.hasNext()) {
            val a = it.next()
            val task = CompiledTaskRegistry.get(a.taskId)
            if (a.intervalTicksLeft > 0) {
                a.intervalTicksLeft -= 1
            }
            else {
                a.intervalTicksLeft = a.interval - 1
                val keep = a.tick()
                if (!keep) {
                    doEndTheme(a, st, entity, it, task, toRemove)
                    continue
                }
            }
            if (a.ticksLeft != -10) {
                a.ticksLeft--

                if (a.ticksLeft <= 0) {
                    a.end()
                    doEndTheme(a, st, entity, it, task, toRemove)
                }
            }
        }
        st.activeEntityTimed.removeIf { it.taskId in toRemove }
        st.activeBlockTimed.removeIf { it.taskId in toRemove }
    }
    private fun doEndTheme(a: ActiveTimedBaseAction<out BaseCompiledTimedAction<*>, *>, st: EntityTaskState,
                           entity: PlatformLivingEntity, it: MutableListIterator<out ActiveTimedBaseAction<out BaseCompiledTimedAction<*>, *>>, task: CompiledTask?,
                           toRemove: MutableSet<ResourceLocation>) {
        a.end()
        it.remove()
        if (!hasActiveTimersForTask(st, a.taskId)) {
            if (task != null && task.lifecycle == TaskLifecycle.ONE_SHOT) {
                st.assignedTasks.remove(task.id)
            }
            if (task != null && task.lifecycle == TaskLifecycle.UNTIL_CONDITION
                && task.completionPredicate.invoke(entity, st)) {
                st.assignedTasks.remove(task.id)
                toRemove.add(task.id)
            }

        }
    }
}

/* TRIGGER BASED TASKS */

sealed class Trigger(val key: ResourceLocation) {
    data object Spawn : Trigger("on_spawn".core())
    data object DeSpawn : Trigger("on_despawn".core())
    data object Death : Trigger("on_death".core())
    /** Platform-specific trigger fire */
    data object Loaded : Trigger("on_loaded".core())
    /** Platform-specific trigger fire */
    data object SpawnOrLoaded : Trigger("on_spawn_or_loaded".core())
    /** Platform-specific server trigger fire */
    data object Tick : Trigger("on_tick".core())
    data object Attack : Trigger("on_attack".core())
    data object ApplyPotion : Trigger("on_potion_received".core())
    data object Damaged : Trigger("on_damaged".core())
    data object Attacked : Trigger("on_attacked".core())
    data object EnterCombat : Trigger("on_enter_combat".core())
    data object DropCombat : Trigger("on_drop_combat".core())
    /** Platform-specific trigger fire */
    data object TargetChanged : Trigger("on_target_changed".core())
    data object Interact : Trigger("on_interacted_with".core())
    /** Platform-specific trigger fire */
    data object PlayerKill : Trigger("on_killed_player".core())
    /** Platform-specific trigger fire */
    data object Teleport : Trigger("on_teleported".core())
    /** Platform-specific trigger fire */
    data object Shoot : Trigger("on_shoot".core())
    /** Platform-specific trigger fire */
    data object ProjectileHit : Trigger("on_projectile_hit_entity".core())
    /** Platform-specific trigger fire */
    data object ProjectileHitBlock : Trigger("on_projectile_hit_block".core())
    /** Platform-specific trigger fire */
    data object Tame : Trigger("on_tame".core())
    /** Platform-specific trigger fire */
    data object Breed : Trigger("on_breed".core())
    /** Platform-specific trigger fire */
    data object Trade : Trigger("on_trade".core())
    /** Platform-specific trigger fire */
    data object Bucketed : Trigger("on_milked_or_bucketed".core())
    /** Platform-specific trigger fire */
    data object HearSound : Trigger("on_hear_sound".core())
    /** Platform-specific trigger fire */
    data object Dismounted : Trigger("on_dismounted".core())
    /** Platform-specific trigger fire */
    data class WorldChanged(val worldName: String) : Trigger("on_change_world/$worldName".core())
    /** Platform-specific trigger fire */
    data class Signal(val signal: String) : Trigger("on_receive_signal/$signal".core())
    /** Platform-specific trigger fire */
    data class HurtBy(val sourceId: String) : Trigger("on_hurt_by/$sourceId".core())
    /** Platform-specific trigger such that platform can fire platform only triggers */
    data class Custom(val name: String, val data: Map<String, Any> = emptyMap()) : Trigger("custom/$name".core())
}
const val TRIGGER_ALL_KEY = "all"

object TriggerManager {
    // entityUuid -> triggerKey -> list of (taskId, params)
    private val subscriptions = ConcurrentHashMap<UUID, MutableMap<ResourceLocation, MutableList<Pair<ResourceLocation, Pair<Int, Map<String, Any>>>>>>()

    /** Subscribe task to a trigger for an entity. */
    fun subscribe(entityUuid: UUID, trigger: Trigger, taskId: ResourceLocation, params: Map<String, Any> = emptyMap(), priority: Int) {
        val map = subscriptions.computeIfAbsent(entityUuid) { mutableMapOf() }
        val list = map.computeIfAbsent(trigger.key) { mutableListOf() }
        list += taskId to (priority to params)
    }

    /** Unsubscribe a specific task for an entity (used on entity unload). */
    fun unsubscribe(entityUuid: UUID, taskId: ResourceLocation) {
        subscriptions[entityUuid]?.values?.forEach { it.removeIf { pair -> pair.first == taskId } }
    }

    /** Fire a trigger for a given entity; this will assign tasks to entity via EntityTaskManager. */
    fun fire(entity: PlatformLivingEntity, trigger: Trigger, extraData: Map<String, Any> = emptyMap()) {
        val map = subscriptions[entity.uuid] ?: return
        val list = map[trigger.key] ?: return

        // iterate snapshot so subscriptions can change from assignTask
        val snapshot = ArrayList(list)
        for ((taskId, params) in snapshot) {
            val merged = HashMap(params.value)
            merged.putAll(extraData)
            EntityTaskManager.assignTask(entity, taskId, merged, params.key)
        }
    }

    fun clearEntity(entityUuid: UUID) {
        subscriptions.remove(entityUuid)
    }
}

object Signals {
    const val OUT_OF_COMBAT = "out_of_combat"
    const val ENTER_COMBAT = "enter_combat"
    const val ALL = "all_signals"
}

object SignalManager {
    // entityUuid -> signalName -> list of (taskId, params)
    private val subscriptions = ConcurrentHashMap<UUID, MutableMap<String, MutableList<Pair<ResourceLocation, Pair<Int, Map<String, Any>>>>>>()

    private fun ensureMap(entityUuid: UUID) =
        subscriptions.computeIfAbsent(entityUuid) { ConcurrentHashMap() }

    /** subscribe a task to a signal for a particular entity (used when a CompiledTask requires a signal) */
    fun subscribe(entityUuid: UUID, signal: String, taskId: ResourceLocation, params: Map<String, Any> = emptyMap(), priority: Int) {
        val map = ensureMap(entityUuid)
        val list = map.computeIfAbsent(signal) { Collections.synchronizedList(
            mutableListOf<Pair<ResourceLocation, Pair<Int, Map<String, Any>>>>()) }
        list += (taskId to (priority to params))
    }

    /** unsubscribe all subscriptions for an entity (on unload) */
    fun clearEntity(entityUuid: UUID) {
        subscriptions.remove(entityUuid)
    }

    /**
     * Fire a signal for an entity. Any tasks subscribed (or tasks waiting for that signal) should be activated.
     * extraData merged into params passed to assignTask.
     */
    fun fire(entity: PlatformLivingEntity, signal: String, extraData: Map<String, Any> = emptyMap()) {
        // 1) assign tasks that were *subscribed* to this signal (these are trigger-like subscriptions)
        val map = subscriptions[entity.uuid] ?: emptyMap()
        val list = map[signal]?.toList() ?: emptyList()
        for ((taskId, pair) in list) {
            val params = pair.second
            val priority = pair.first

            val merged = HashMap(params)
            merged.putAll(extraData)
            EntityTaskManager.assignTask(entity, taskId, merged, priority)
        }

        EntityTaskManager.onSignalReceived(entity, signal, extraData)
        EntityTaskManager.onStopSignal(entity, signal, extraData)

        EntityTaskManager.onSignalReceived(entity, Signals.ALL, extraData)
        EntityTaskManager.onStopSignal(entity, Signals.ALL, extraData)
    }

    /** Unsubscribe a specific (task) subscription if needed */
    fun unsubscribe(entityUuid: UUID, signal: String, taskId: ResourceLocation) {
        subscriptions[entityUuid]?.get(signal)?.removeIf { it.first == taskId }
    }
}
