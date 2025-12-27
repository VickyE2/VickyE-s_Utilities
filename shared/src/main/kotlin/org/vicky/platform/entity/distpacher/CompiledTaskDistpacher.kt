/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.entity.distpacher

import org.vicky.platform.entity.*
import org.vicky.platform.utils.ResourceLocation
import org.vicky.platform.world.PlatformBlock
import org.vicky.utilities.ContextLogger.AsyncContextLogger
import org.vicky.utilities.ContextLogger.ContextLogger
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
    var cooldownTicksRemaining: Int = 0
    val activeEntityTimed = ArrayList<ActiveTimedAction>(4) // small pre-sized list
    val activeBlockTimed = ArrayList<ActiveTimedBlockAction>(4) // small pre-sized list
    val lastSelectorTick = mutableMapOf<ResourceLocation, Long>() // throttle per-selector per-entity
    val assignedTasks = ArrayList<ResourceLocation>(4) // list of task IDs assigned to this entity
}

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
    }

    /** Assign a compiled task to an entity (store ID only). */
    fun assignTask(entity: PlatformLivingEntity, taskId: ResourceLocation) {
        CompiledTaskRegistry.get(taskId) ?: error("No compiled task $taskId")
        val st = stateFor(entity)
        if (!st.assignedTasks.contains(taskId)) {
            st.assignedTasks += taskId
            LOGGER.print("Assigned task $taskId to ${entity.uuid}", ContextLogger.LogType.BASIC)
        }
    }

    fun clearTasks(entity: PlatformLivingEntity) {
        states[entity.uuid]?.assignedTasks?.clear()
    }

    /** Called every server tick (or from per-entity tick) */
    fun tickEntity(entity: PlatformLivingEntity, worldTick: Long) {
        val st = states[entity.uuid] ?: return
        LOGGER.debug("tickEntity: ${entity.uuid} assigned=${st.assignedTasks.size} " +
                "cooldown=${st.cooldownTicksRemaining}")

        tickActiveTimed(entity, st)

        if (st.cooldownTicksRemaining > 0) {
            st.cooldownTicksRemaining--
            return
        }
        if (st.assignedTasks.isEmpty()) return

        val candidates = st.assignedTasks
            .mapNotNull { CompiledTaskRegistry.get(it) }
            .sortedWith(compareByDescending<CompiledTask> { it.priority }.thenBy { Random.nextFloat() })

        val tasksToRemove = mutableListOf<ResourceLocation>()

        for (task in candidates) {
            when (tryRunTaskOnce(entity, task, st, worldTick)) {
                TaskRunOutcome.NOT_RUN -> continue
                TaskRunOutcome.RAN_INSTANT -> {
                    st.cooldownTicksRemaining = task.cooldownTicks.coerceAtLeast(0)
                    if (task.lifecycle == TaskLifecycle.ONE_SHOT) tasksToRemove += task.id
                    break
                }
                TaskRunOutcome.RAN_SCHEDULED -> {
                    st.cooldownTicksRemaining = task.cooldownTicks.coerceAtLeast(0)
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
            st.assignedTasks.removeAll(tasksToRemove.toSet())
        }
    }

    private fun hasActiveTimersForTask(st: EntityTaskState, taskId: ResourceLocation): Boolean {
        return st.activeEntityTimed.any { it.taskId == taskId } || st.activeBlockTimed.any { it.taskId == taskId }
    }

    enum class TaskRunOutcome { NOT_RUN, RAN_INSTANT, RAN_SCHEDULED, COMPLETED }

    private fun tryRunTaskOnce(entity: PlatformLivingEntity, task: CompiledTask, st: EntityTaskState, worldTick: Long): TaskRunOutcome {
        // Walk the task steps implementing the same semantics as executeCompiledTask
        var working: AmountableResult<PlatformLivingEntity>? = null
        var workingBlock: AmountableResult<PlatformBlock<*>>? = null
        val ctx = SelectorContext.ofEntity(entity)

        for (step in task.steps) {
            when (step) {
                is CompiledStep.EntitySelectorStep -> {
                    val last = st.lastSelectorTick[step.selector.id] ?: -Long.MAX_VALUE
                    if (worldTick - last < selectorThrottleTicks) {
                        if (working == null) {
                            LOGGER.debug("Task ${task.id} entity selector ${step.selector.id} returned no targets — skipping")
                            return TaskRunOutcome.NOT_RUN
                        }
                    } else {
                        st.lastSelectorTick[step.selector.id] = worldTick
                        working = step.selector.select(ctx)
                    }
                }
                is CompiledStep.EntityConditionStep -> {
                    val dto = step.dto
                    val isForTarget = dto.params["isForTarget"]?.toBoolean() ?: true
                    if (!isForTarget) {
                        val necessary = step.compiled.filter { it.mustBeTrue }
                        val optional = step.compiled.filter { !it.mustBeTrue }
                        val necessaryOk = necessary.all { it.test(entity) }
                        val optionalOk = optional.isEmpty() || optional.any { it.test(entity) }
                        if (!necessaryOk || !optionalOk) return TaskRunOutcome.NOT_RUN // gate failed
                    } else {
                        // filter working set
                        if (working == null) return TaskRunOutcome.NOT_RUN
                        working = when (working.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> {
                                val t = working.getSingleResult()
                                if (t == null || !step.compiled.all { it.test(t) }) null
                                else working
                            }
                            else -> {
                                val filtered = working.getResults().filter { ent -> step.compiled.all { it.test(ent) } }
                                if (filtered.isEmpty()) null else AmountableResult(ResultType.MULTIPLE, filtered)
                            }
                        }
                        if (working == null) return TaskRunOutcome.NOT_RUN
                    }
                }
                is CompiledStep.EntityActionStep -> {
                    val dto = step.dto
                    val isOnTarget = dto.params["isOnTarget"]?.toBoolean() ?: true

                    // Instant actions
                    if (isOnTarget) {
                        if (working == null) return TaskRunOutcome.NOT_RUN
                        when (working.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> working.getSingleResult()?.let { t ->
                                step.compiledAction.forEach { it.run(entity, t) }
                            }
                            else -> working.getResults().forEach { t ->
                                step.compiledAction.forEach { it.run(entity, t) }
                            }
                        }
                    } else {
                        step.compiledAction.forEach { it.run(entity, null) }
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
                                    scheduleTimedList(st, entity, listOf(t), task, step)
                                }
                            }
                            else -> {
                                scheduleTimedList(st, entity, working.getResults(), task, step)
                            }
                        }
                    } else {
                        scheduleTimedList(st, entity, listOf(null), task, step)
                    }
                }
                is CompiledStep.BlockSelectorStep -> {
                    // Throttle selectors: per-entity per-selector last-run time
                    val last = st.lastSelectorTick[step.selector.id] ?: -Long.MAX_VALUE
                    if (worldTick - last < selectorThrottleTicks) {
                        if (workingBlock == null) {
                            LOGGER.debug("Task ${task.id} block selector ${step.selector.id} returned no targets — skipping")
                            return TaskRunOutcome.NOT_RUN
                        }
                    } else {
                        st.lastSelectorTick[step.selector.id] = worldTick
                        workingBlock = step.selector.select(ctx)
                    }
                }
                is CompiledStep.BlockConditionStep -> {
                    val dto = step.dto
                    val isForTarget = dto.params["isForTarget"]?.toBoolean() ?: true
                    if (!isForTarget) {

                    } else {
                        // filter working set
                        if (workingBlock == null) return TaskRunOutcome.NOT_RUN
                        workingBlock = when (workingBlock.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> {
                                val t = workingBlock.getSingleResult()
                                if (t == null || !step.compiledBlock.all { it.test(t) }) null
                                else workingBlock
                            }
                            else -> {
                                val filtered = workingBlock.getResults().filter { ent -> step.compiledBlock.all { it.test(ent) } }
                                if (filtered.isEmpty()) null else AmountableResult(ResultType.MULTIPLE, filtered)
                            }
                        }
                        if (working == null) return TaskRunOutcome.NOT_RUN
                    }
                }
                is CompiledStep.BlockActionStep -> {
                    val dto = step.dto
                    val isOnTarget = dto.params["isOnTarget"]?.toBoolean() ?: true

                    // Instant actions
                    if (isOnTarget) {
                        if (workingBlock == null) return TaskRunOutcome.NOT_RUN
                        when (workingBlock.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> workingBlock.getSingleResult()?.let { t ->
                                step.compiledBlockAction.forEach { it.run(entity, t) }
                            }
                            else -> workingBlock.getResults().forEach { t ->
                                step.compiledBlockAction.forEach { it.run(entity, t) }
                            }
                        }
                    } else {

                    }

                    if (st.activeEntityTimed.size >= maxActiveTimedPerEntity) {
                        continue
                    }

                    if (isOnTarget) {
                        if (workingBlock == null) continue
                        when (workingBlock.resultType) {
                            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> {
                                workingBlock.getSingleResult()?.let { t ->
                                    scheduleTimedList(st, entity, listOf(t), task, step)
                                }
                            }
                            else -> {
                                scheduleTimedList(st, entity, workingBlock.getResults(), task, step)
                            }
                        }
                    } else {
                        scheduleTimedList(st, entity, listOf(null), task, step)
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
        task: CompiledTask,
        step: CompiledStep.EntityActionStep
    ) {
        // step.dto.timedRefs aligns to compiledTimedAction list by index (we kept them matched on compile)
        val dtoTimedRefs = step.dto.entityTimedRefs
        val ct = step.compiledTimedAction

        // for each target schedule each timedRef
        targets.forEach { target ->
            ct.forEachIndexed { idx, compiledTimed ->
                val ref = dtoTimedRefs.getOrNull(idx) ?: TimedRef(compiledTimed.id) // fallback
                // slot & runBlocking handling
                val slot = ref.slot
                if (slot != null && st.activeEntityTimed.any { it.slot == slot }) {
                    LOGGER.debug("Skipping timed ref $ref for task ${task.id} because slot $slot is busy for entity ${owner.uuid}")
                    return@forEachIndexed
                }

                // compute resolved duration and interval
                val ticksLeft = ref.duration ?: compiledTimed.defaultDuration
                val interval = ref.interval ?: compiledTimed.defaultInterval

                if (compiledTimed.onStart(owner, target)) {
                    // create active timed
                    val active = ActiveTimedAction(compiledTimed, owner, target,
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
        task: CompiledTask,
        step: CompiledStep.BlockActionStep
    ) {
        // step.dto.timedRefs aligns to compiledTimedAction list by index (we kept them matched on compile)
        val dtoTimedRefs = step.dto.entityTimedRefs
        val ct = step

        // for each target schedule each timedRef
        targets.forEach { target ->
            ct.compiledTimedAction.forEachIndexed { idx, compiledTimed ->
                val ref = dtoTimedRefs.getOrNull(idx) ?: TimedRef(compiledTimed.id) // fallback
                // slot & runBlocking handling
                val slot = ref.slot
                if (slot != null && st.activeEntityTimed.any { it.slot == slot }) {
                    // slot busy -> skip or refresh; choose policy: skip
                    return@forEachIndexed
                }

                // compute resolved duration and interval
                val ticksLeft = ref.duration ?: compiledTimed.defaultDuration
                val interval = ref.interval ?: compiledTimed.defaultInterval

                // call onStart on main thread
                if (compiledTimed.onStart(owner, target)) {
                    // create active timed
                    val active = ActiveTimedBlockAction(compiledTimed, owner, target,
                        ticksLeft, interval, 0, slot, task.id, compiledTimed.id)
                    st.activeBlockTimed.add(active)
                }
            }
        }
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
        run {
            val it = st.activeEntityTimed.listIterator()
            while (it.hasNext()) {
                val a = it.next()
                if (a.intervalTicksLeft > 0) {
                    a.intervalTicksLeft -= 1
                } else {
                    a.intervalTicksLeft = a.interval - 1
                    val keep = a.compiled.onTick(a.self, a.targetEntity)
                    if (!keep) {
                        a.compiled.onEnd(a.self, a.targetEntity)
                        val finishedTaskId = a.taskId
                        it.remove()
                        if (!hasActiveTimersForTask(st, finishedTaskId)) {
                            // no more active timers for this task
                            val task = CompiledTaskRegistry.get(finishedTaskId)
                            if (task != null && task.lifecycle == TaskLifecycle.ONE_SHOT) {
                                st.assignedTasks.remove(finishedTaskId)
                            }
                            if (task != null && task.lifecycle == TaskLifecycle.UNTIL_CONDITION
                                && task.completionPredicate.invoke(entity, st)) {
                                st.assignedTasks.remove(task.id)
                                st.activeEntityTimed.removeIf { it.taskId == task.id }
                            }

                        }

                        continue
                    }
                }
                if (a.ticksLeft > 0) {
                    a.ticksLeft -= 1
                    if (a.ticksLeft == 0) {
                        a.compiled.onEnd(a.self, a.targetEntity)
                        val finishedTaskId = a.taskId
                        it.remove()
                        if (!hasActiveTimersForTask(st, finishedTaskId)) {
                            // no more active timers for this task
                            val task = CompiledTaskRegistry.get(finishedTaskId)
                            if (task != null && task.lifecycle == TaskLifecycle.ONE_SHOT) {
                                st.assignedTasks.remove(finishedTaskId)
                            }
                            if (task != null && task.lifecycle == TaskLifecycle.UNTIL_CONDITION
                                && task.completionPredicate.invoke(entity, st)) {
                                st.assignedTasks.remove(task.id)
                                st.activeEntityTimed.removeIf { it.taskId == task.id }
                            }
                        }

                    }
                }
            }
        }
        run {
            val it = st.activeBlockTimed.listIterator()
            while (it.hasNext()) {
                val a = it.next()
                if (a.intervalTicksLeft > 0) {
                    a.intervalTicksLeft -= 1
                } else {
                    a.intervalTicksLeft = a.interval - 1
                    val keep = a.compiled.onTick(a.self, a.targetBlock)
                    if (!keep) {
                        a.compiled.onEnd(a.self, a.targetBlock)
                        val finishedTaskId = a.taskId
                        it.remove()
                        if (!hasActiveTimersForTask(st, finishedTaskId)) {
                            // no more active timers for this task
                            val task = CompiledTaskRegistry.get(finishedTaskId)
                            if (task != null && task.lifecycle == TaskLifecycle.ONE_SHOT) {
                                st.assignedTasks.remove(finishedTaskId)
                            }
                            if (task != null && task.lifecycle == TaskLifecycle.UNTIL_CONDITION
                                && task.completionPredicate.invoke(entity, st)) {
                                st.assignedTasks.remove(task.id)
                                st.activeEntityTimed.removeIf { it.taskId == task.id }
                            }
                        }

                        continue
                    }
                }
                if (a.ticksLeft > 0) {
                    a.ticksLeft -= 1
                    if (a.ticksLeft == 0) {
                        a.compiled.onEnd(a.self, a.targetBlock)
                        val finishedTaskId = a.taskId
                        it.remove()
                        if (!hasActiveTimersForTask(st, finishedTaskId)) {
                            // no more active timers for this task
                            val task = CompiledTaskRegistry.get(finishedTaskId)
                            if (task != null && task.lifecycle == TaskLifecycle.ONE_SHOT) {
                                st.assignedTasks.remove(finishedTaskId)
                            }
                            if (task != null && task.lifecycle == TaskLifecycle.UNTIL_CONDITION
                                && task.completionPredicate.invoke(entity, st)) {
                                st.assignedTasks.remove(task.id)
                                st.activeEntityTimed.removeIf { it.taskId == task.id }
                            }
                        }

                    }
                }
            }
        }
    }
}
