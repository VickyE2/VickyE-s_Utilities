package org.vicky.platform.entity.distpacher

import org.vicky.platform.entity.*
import org.vicky.platform.utils.ResourceLocation
import org.vicky.platform.world.PlatformBlock
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.random.Random

/** Small helper — platform must provide a main-thread executor/callback.
 * Replace with server scheduler (Bukkit.runTask, Fabric/Minecraft scheduler, etc.) */
var runOnMainThread: (Runnable) -> Unit = { r -> r.run() }

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
        if (!st.assignedTasks.contains(taskId)) st.assignedTasks += taskId
    }

    fun clearTasks(entity: PlatformLivingEntity) {
        states[entity.uuid]?.assignedTasks?.clear()
    }

    /** Called every server tick (or from per-entity tick) */
    fun tickEntity(entity: PlatformLivingEntity, worldTick: Long) {
        val st = states[entity.uuid] ?: return

        // tick cooldowns + active timed actions
        if (st.cooldownTicksRemaining > 0) st.cooldownTicksRemaining--
        tickActiveTimed(entity, st)

        // simple short-circuit: if cooldown active skip running tasks
        if (st.cooldownTicksRemaining > 0) return

        // gather candidates (pre-compiled)
        if (st.assignedTasks.isEmpty()) return

        // sort tasks by priority and optionally randomness/weight
        val candidates = st.assignedTasks
            .mapNotNull { CompiledTaskRegistry.get(it) }
            .sortedWith(compareByDescending<CompiledTask> { it.priority }.thenBy { Random.nextFloat() })

        // Evaluate tasks in order until one runs (or multiple if desired)
        for (task in candidates) {
            // task-level cheap checks: type (CONDITIONED vs RANDOM)
            when (task.type) {
                TaskType.CONDITIONED -> {
                    val ran = tryRunTaskOnce(entity, task, st, worldTick)
                    if (ran) {
                        // use cooldown to avoid re-run too often (task decides cooldownTicks)
                        st.cooldownTicksRemaining = task.cooldownTicks.coerceAtLeast(0)
                        break // treat as exclusive; comment out break if you want multiple tasks per tick
                    }
                }
                TaskType.RANDOM -> {
                    // RANDOM tasks: probabilistic run chance, or weighted selection
                    // Here we do a simple 1/(priority+1) chance example: (you can customize)
                    val chance = task.chance
                    if (Random.nextFloat() < chance) {
                        val ran = tryRunTaskOnce(entity, task, st, worldTick)
                        if (ran) {
                            st.cooldownTicksRemaining = task.cooldownTicks.coerceAtLeast(0)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun tryRunTaskOnce(entity: PlatformLivingEntity, task: CompiledTask, st: EntityTaskState, worldTick: Long): Boolean {
        // Walk the task steps implementing the same semantics as executeCompiledTask
        var working: AmountableResult<PlatformLivingEntity>? = null
        var workingBlock: AmountableResult<PlatformBlock<*>>? = null
        val ctx = SelectorContext.ofEntity(entity)

        for (step in task.steps) {
            when (step) {
                is CompiledStep.EntitySelectorStep -> {
                    // Throttle selectors: per-entity per-selector last-run time
                    val last = st.lastSelectorTick[step.selector.id] ?: -Long.MAX_VALUE
                    if (worldTick - last < selectorThrottleTicks) {
                        // reuse previous working if any; else run lightweight fallback (skip selection)
                        working = working ?: AmountableResult(ResultType.MULTIPLE, emptyList())
                    } else {
                        st.lastSelectorTick[step.selector.id] = worldTick
                        // Option: if selector is expensive, run async and process when ready.
                        // Here we treat selectors as sync. If you want async, see notes below.
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
                        if (!necessaryOk || !optionalOk) return false // gate failed
                    } else {
                        // filter working set
                        if (working == null) return false
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
                        if (working == null) return false
                    }
                }
                is CompiledStep.EntityActionStep -> {
                    val dto = step.dto
                    val isOnTarget = dto.params["isOnTarget"]?.toBoolean() ?: true

                    // Instant actions
                    if (isOnTarget) {
                        if (working == null) return false
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
                        // reuse previous working if any; else run lightweight fallback (skip selection)
                        workingBlock = workingBlock ?: AmountableResult(ResultType.MULTIPLE, emptyList())
                    } else {
                        st.lastSelectorTick[step.selector.id] = worldTick
                        // Option: if selector is expensive, run async and process when ready.
                        // Here we treat selectors as sync. If you want async, see notes below.
                        workingBlock = step.selector.select(ctx)
                    }
                }
                is CompiledStep.BlockConditionStep -> {
                    val dto = step.dto
                    val isForTarget = dto.params["isForTarget"]?.toBoolean() ?: true
                    if (!isForTarget) {

                    } else {
                        // filter working set
                        if (workingBlock == null) return false
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
                        if (working == null) return false
                    }
                }
                is CompiledStep.BlockActionStep -> {
                    val dto = step.dto
                    val isOnTarget = dto.params["isOnTarget"]?.toBoolean() ?: true

                    // Instant actions
                    if (isOnTarget) {
                        if (workingBlock == null) return false
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
        // If we reached here and didn't return false, the task "ran" (did something)
        return true
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
                    // slot busy -> skip or refresh; choose policy: skip
                    return@forEachIndexed
                }

                // compute resolved duration and interval
                val ticksLeft = ref.duration ?: compiledTimed.defaultDuration
                val interval = ref.interval ?: compiledTimed.defaultInterval

                // create active timed
                val active = ActiveTimedAction(compiledTimed, owner, target, ticksLeft, interval, 0, slot)
                st.activeEntityTimed.add(active)
                // call onStart on main thread
                compiledTimed.onStart(owner, target)
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

                // create active timed
                val active = ActiveTimedBlockAction(compiledTimed, owner, target, ticksLeft, interval, 0, slot)
                st.activeBlockTimed.add(active)
                // call onStart on main thread
                compiledTimed.onStart(owner, target)
            }
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
                        it.remove()
                        continue
                    }
                }
                if (a.ticksLeft > 0) {
                    a.ticksLeft -= 1
                    if (a.ticksLeft == 0) {
                        a.compiled.onEnd(a.self, a.targetEntity)
                        it.remove()
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
                        it.remove()
                        continue
                    }
                }
                if (a.ticksLeft > 0) {
                    a.ticksLeft -= 1
                    if (a.ticksLeft == 0) {
                        a.compiled.onEnd(a.self, a.targetBlock)
                        it.remove()
                    }
                }
            }
        }
    }
}
