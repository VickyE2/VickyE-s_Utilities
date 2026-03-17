/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.entity

import kotlinx.serialization.Contextual
import org.vicky.platform.PlatformPlayer
import org.vicky.platform.entity.distpacher.EntityTaskState
import org.vicky.platform.utils.ResourceLocation
import org.vicky.platform.world.PlatformBlock
import org.vicky.platform.world.PlatformWorld
import org.vicky.utilities.ContextLogger.AsyncContextLogger
import org.vicky.utilities.ContextLogger.ContextLogger
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.random.Random

// ---------------------- runtime data / timed actions -----------------------
abstract class ActiveTimedBaseAction<C, T>(
    val compiled: C,
    val self: PlatformLivingEntity,
    val target: T?,
    var ticksLeft: Int,                // -1 = infinite
    val interval: Int = 1,
    var intervalTicksLeft: Int = 0,
    val slot: String? = null,
    val taskId: ResourceLocation,
    val timedId: ResourceLocation
) {
    abstract fun tick(): Boolean
    abstract fun end()
}
data class ActiveTimedAction(
    val compiledAction: CompiledTimedAction,
    val selfEntity: PlatformLivingEntity,
    val targetEntity: PlatformLivingEntity? = null,
    var ticks: Int,
    val actionInterval: Int = 1,
    var intervalTicks: Int = 0,
    val actionSlot: String? = null,
    val actionTaskId: ResourceLocation,
    val actionTimedId: ResourceLocation
) : ActiveTimedBaseAction<CompiledTimedAction, PlatformLivingEntity>(
    compiledAction,
    selfEntity,
    targetEntity,
    ticks,
    actionInterval,
    intervalTicks,
    actionSlot,
    actionTaskId,
    actionTimedId
) {
    override fun tick(): Boolean {
        return compiled.onTick(self, target)
    }
    override fun end() {
        compiled.onEnd(self, target)
    }
}
data class ActiveTimedBlockAction(
    val compiledAction: CompiledBlockTimedAction,
    val selfEntity: PlatformLivingEntity,
    val targetBlock: PlatformBlock<*>? = null,
    var ticks: Int,
    val actionInterval: Int = 1,
    var intervalTicks: Int = 0,
    val actionSlot: String? = null,
    val actionTaskId: ResourceLocation,
    val actionTimedId: ResourceLocation
) : ActiveTimedBaseAction<CompiledBlockTimedAction, PlatformBlock<*>>(
    compiledAction,
    selfEntity,
    targetBlock,
    ticks,
    actionInterval,
    intervalTicks,
    actionSlot,
    actionTaskId,
    actionTimedId
) {
    override fun tick(): Boolean {
        return compiled.onTick(self, target)
    }
    override fun end() {
        compiled.onEnd(self, target)
    }
}

/**
 * Generic-ish compiled runtime types. Entity and block variants kept separate
 * so we don't have to generify the entire engine at once.
 */
data class CompiledEntityAction(val id: ResourceLocation, val run: (PlatformLivingEntity, PlatformLivingEntity?) -> Boolean)
data class CompiledBlockAction(val id: ResourceLocation, val run: (PlatformLivingEntity, PlatformBlock<*>) -> Boolean)

data class CompiledCondition(val id: ResourceLocation, val test: (PlatformLivingEntity) -> Boolean, val mustBeTrue: Boolean = true)
data class CompiledBlockCondition(val id: ResourceLocation, val test: (PlatformBlock<*>) -> Boolean, val mustBeTrue: Boolean = true)

data class CompiledEntitySelector(val id: ResourceLocation, val select: (SelectorContext) -> AmountableResult<PlatformLivingEntity>)
data class CompiledBlockSelector(val id: ResourceLocation, val select: (SelectorContext) -> AmountableResult<PlatformBlock<*>>)

abstract class BaseCompiledTimedAction<I>(
    val id: ResourceLocation,
    val defaultDuration: Int,
    val defaultInterval: Int,
    val onStart: (PlatformLivingEntity, I?) -> Boolean,
    val onTick: (PlatformLivingEntity, I?) -> Boolean,
    val onEnd: (PlatformLivingEntity, I?) -> Unit
)
data class CompiledBlockTimedAction(
    private val timedId: ResourceLocation,
    private val timedDefaultDuration: Int,
    private val timedDefaultInterval: Int,
    private val timedOnStart: (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean,
    private val timedOnTick: (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean,
    private val timedOnEnd: (PlatformLivingEntity, PlatformBlock<*>?) -> Unit
) : BaseCompiledTimedAction<PlatformBlock<*>>(
    timedId, timedDefaultDuration, timedDefaultInterval, timedOnStart, timedOnTick, timedOnEnd
)
data class CompiledTimedAction(
    private val timedId: ResourceLocation,
    private val timedDefaultDuration: Int,
    private val timedDefaultInterval: Int,
    private val timedOnStart: (PlatformLivingEntity, PlatformLivingEntity?) -> Boolean,
    private val timedOnTick: (PlatformLivingEntity, PlatformLivingEntity?) -> Boolean,
    private val timedOnEnd: (PlatformLivingEntity, PlatformLivingEntity?) -> Unit
) : BaseCompiledTimedAction<PlatformLivingEntity>(
    timedId, timedDefaultDuration, timedDefaultInterval, timedOnStart, timedOnTick, timedOnEnd
)

data class CompiledVoidTaskStep(
    val id: ResourceLocation,
    val entityActionRefs: List<ActionRef> = emptyList(),
    val blockActionRefs: List<BlockActionRef> = emptyList(),
    val entityTimedRefs: List<TimedRef> = emptyList(),
    val blockTimedActionRefs: List<TimedRef> = emptyList(),
    val params: Map<String, String> = emptyMap()
)

data class CompiledConditionTaskStep(
    val id: ResourceLocation,
    val conditionRefs: List<ConditionRef> = emptyList(),
    val blockConditionRefs: List<BlockConditionRef> = emptyList(),
    val params: Map<String, String> = emptyMap()
)

// --- Reference DTOs (pure-data, serializable-friendly) ---
data class ActionRef(val id: ResourceLocation)
data class BlockActionRef(val id: ResourceLocation)
data class TimedRef(
    val id: ResourceLocation,
    val duration: Int? = null,
    val interval: Int? = null,
    val runBlocking: Boolean = true,
    val slot: String? = null
)
data class ConditionRef(val id: ResourceLocation, val params: Map<String, String> = emptyMap(), val mustBeTrue: Boolean = true)
data class BlockConditionRef(val id: ResourceLocation, val params: Map<String, String> = emptyMap(), val mustBeTrue: Boolean = true)
data class FilterRef<T>(val id: ResourceLocation, val params: Map<String, String> = emptyMap())

// --- Registry / Factory interfaces (minimal) ---
fun interface ActionFactory {
    fun compile(ref: ActionRef): CompiledEntityAction
}
fun interface BlockActionFactory {
    fun compile(ref: BlockActionRef): CompiledBlockAction
}
fun interface ConditionFactory {
    fun compile(ref: ConditionRef): CompiledCondition
}
fun interface BlockConditionFactory {
    fun compile(ref: BlockConditionRef): CompiledBlockCondition
}
fun interface FilterFactory<T> {
    fun compile(ref: FilterRef<T>): CompiledFilter<T>
}
fun interface TimedActionFactory {
    fun compile(ref: TimedRef): CompiledTimedAction
}
fun interface BlockTimedActionFactory {
    fun compile(ref: TimedRef): CompiledBlockTimedAction
}
data class CompiledFilter<T>(val id: ResourceLocation, val test: (T, SelectorContext) -> Boolean)

// ---------------------- Global registry -----------------------
object GlobalSpecRegistry {
    private val actionFactories = mutableMapOf<ResourceLocation, ActionFactory>()
    private val blockActionFactories = mutableMapOf<ResourceLocation, BlockActionFactory>()
    private val timedActionFactories = mutableMapOf<ResourceLocation, TimedActionFactory>()
    private val blockTimedActionFactories = mutableMapOf<ResourceLocation, BlockTimedActionFactory>()
    private val conditionFactories = mutableMapOf<ResourceLocation, ConditionFactory>()
    private val blockConditionFactories = mutableMapOf<ResourceLocation, BlockConditionFactory>()
    private val filterFactories = mutableMapOf<ResourceLocation, FilterFactory<*>>()

    // action
    fun registerAction(id: ResourceLocation, factory: ActionFactory) { actionFactories[id] = factory }
    fun compileAction(ref: ActionRef): CompiledEntityAction {
        val f = actionFactories[ref.id] ?: error("No ActionFactory registered for ${ref.id}")
        return f.compile(ref)
    }

    // block-action
    fun registerBlockAction(id: ResourceLocation, factory: BlockActionFactory) { blockActionFactories[id] = factory }
    fun compileBlockAction(ref: BlockActionRef): CompiledBlockAction {
        val f = blockActionFactories[ref.id] ?: error("No BlockActionFactory registered for ${ref.id}")
        return f.compile(ref)
    }

    // timed
    fun registerTimedAction(id: ResourceLocation, factory: TimedActionFactory) { timedActionFactories[id] = factory }
    fun compileTimedAction(ref: TimedRef): CompiledTimedAction {
        val f = timedActionFactories[ref.id] ?: error("No TimedActionFactory registered for ${ref.id}")
        return f.compile(ref)
    }

    fun registerBlockTimedAction(id: ResourceLocation, factory: BlockTimedActionFactory) { blockTimedActionFactories[id] = factory }
    fun compileBlockTimedAction(ref: TimedRef): CompiledBlockTimedAction {
        val f = blockTimedActionFactories[ref.id] ?: error("No BlockTimedActionFactory registered for ${ref.id}")
        return f.compile(ref)
    }

    // conditions
    fun registerCondition(id: ResourceLocation, factory: ConditionFactory) { conditionFactories[id] = factory }
    fun compileCondition(ref: ConditionRef): CompiledCondition {
        val f = conditionFactories[ref.id] ?: error("No ConditionFactory registered for ${ref.id}")
        return f.compile(ref)
    }

    fun registerBlockCondition(id: ResourceLocation, factory: BlockConditionFactory) { blockConditionFactories[id] = factory }
    fun compileBlockCondition(ref: BlockConditionRef): CompiledBlockCondition {
        val f = blockConditionFactories[ref.id] ?: error("No BlockConditionFactory registered for ${ref.id}")
        return f.compile(ref)
    }

    // filters (generic)
    fun <T> registerFilter(id: ResourceLocation, factory: FilterFactory<T>) { filterFactories[id] = factory }
    @Suppress("UNCHECKED_CAST")
    fun <T> compileFilter(ref: FilterRef<T>): CompiledFilter<T> {
        val f = filterFactories[ref.id] ?: error("No FilterFactory registered for ${ref.id}")
        return (f as FilterFactory<T>).compile(ref)
    }

    // existence queries
    fun hasFilterFactory(id: ResourceLocation): Boolean = filterFactories.containsKey(id)
    fun hasActionFactory(id: ResourceLocation): Boolean = actionFactories.containsKey(id)
    fun hasBlockActionFactory(id: ResourceLocation): Boolean = blockActionFactories.containsKey(id)
    fun hasTimedActionFactory(id: ResourceLocation): Boolean = timedActionFactories.containsKey(id)
    fun hasBlockTimedActionFactory(id: ResourceLocation): Boolean = blockTimedActionFactories.containsKey(id)
    fun hasConditionFactory(id: ResourceLocation): Boolean = conditionFactories.containsKey(id)
    fun hasBlockConditionFactory(id: ResourceLocation): Boolean = blockConditionFactories.containsKey(id)
}

// ---------------------- executor (entity-only path preserved) -----------------------
fun executeCompiledTask(compiled: CompiledTask, owner: PlatformLivingEntity) {
    var workingEntities: AmountableResult<PlatformLivingEntity>? = null
    var workingBlocks: AmountableResult<PlatformBlock<*>>? = null
    val ctx = SelectorContext.ofEntity(owner)

    for (step in compiled.steps) {
        when (step) {
            is CompiledStep.EntitySelectorStep -> {
                workingEntities = step.selector.select(ctx)
                workingBlocks = null
            }
            is CompiledStep.BlockSelectorStep -> {
                workingBlocks = step.selector.select(ctx)
                workingEntities = null
            }
            is CompiledStep.EntityConditionStep -> {
                val dto = step.dto
                val isForTarget = dto.params["isForTarget"]?.toBoolean() ?: true
                if (!isForTarget) {
                    val necessary = step.compiled.filter { it.mustBeTrue }
                    val optional = step.compiled.filter { !it.mustBeTrue }
                    val necessaryOk = necessary.all { it.test(owner) }
                    val optionalOk = if (optional.isEmpty()) true else optional.any { it.test(owner) }
                    if (!necessaryOk || !optionalOk) return
                } else {
                    if (workingEntities == null) continue
                    when (workingEntities.resultType) {
                        ResultType.SINGLE, ResultType.RANDOM_SINGLE -> {
                            val t = workingEntities.getSingleResult()
                            if (t == null || !step.compiled.all { it.test(t) }) workingEntities = null
                        }
                        else -> {
                            val filtered = workingEntities.getResults().filter { ent -> step.compiled.all { it.test(ent) } }
                            workingEntities = AmountableResult(ResultType.MULTIPLE, filtered)
                            if (workingEntities.getResults().isEmpty()) workingEntities = null
                        }
                    }
                }
            }
            is CompiledStep.BlockConditionStep -> {
                val dto = step.dto
                val isForTarget = dto.params["isForTarget"]?.toBoolean() ?: true
                if (!isForTarget) {

                } else {
                    if (workingBlocks == null) continue
                    when (workingBlocks.resultType) {
                        ResultType.SINGLE, ResultType.RANDOM_SINGLE -> {
                            val t = workingBlocks.getSingleResult()
                            if (t == null || !step.compiledBlock.all { it.test(t) }) workingBlocks = null
                        }
                        else -> {
                            val filtered = workingBlocks.getResults().filter { b -> step.compiledBlock.all { it.test(b) } }
                            workingBlocks = AmountableResult(ResultType.MULTIPLE, filtered)
                            if (workingBlocks.getResults().isEmpty()) workingBlocks = null
                        }
                    }
                }
            }
            is CompiledStep.EntityActionStep -> {
                val dto = step.dto
                val isOnTarget = dto.params["isOnTarget"]?.toBoolean() ?: true
                if (isOnTarget) {
                    if (workingEntities == null) continue
                    when (workingEntities.resultType) {
                        ResultType.SINGLE, ResultType.RANDOM_SINGLE -> workingEntities.getSingleResult()?.let { t ->
                            step.compiledAction.forEach { it.run(owner, t) }
                        }
                        else -> workingEntities.getResults().forEach { t ->
                            step.compiledAction.forEach { it.run(owner, t) }
                        }
                    }
                } else {
                    step.compiledAction.forEach { it.run(owner, null) }
                }
            }
            is CompiledStep.BlockActionStep -> {
                val dto = step.dto
                val isOnTarget = dto.params["isOnTarget"]?.toBoolean() ?: true
                if (isOnTarget) {
                    if (workingBlocks == null) continue
                    when (workingBlocks.resultType) {
                        ResultType.SINGLE, ResultType.RANDOM_SINGLE -> workingBlocks.getSingleResult()?.let { t ->
                            step.compiledBlockAction.forEach { it.run(owner, t) }
                        }
                        else -> workingBlocks.getResults().forEach { t ->
                            step.compiledBlockAction.forEach { it.run(owner, t) }
                        }
                    }
                } else {
                    // block actions that act on owner (rare) - call with a dummy block? or ignore
                    step.compiledBlockAction.forEach { it.run(owner, owner.world.getBlockAt(owner.location.x, owner.location.y, owner.location.z)) }
                }
            }
        }
    }
}

// --------------------- Compiler (DTO -> runtime) - minimal support ---------------------
// NOTE: this is a lightweight example; depending on how your DTOs encode block vs entity refs
// you will likely want to extend it.

object BookBasedAntagnosticCompiler {
    fun compile(dto: TaskSpecDTO): CompiledTask {
        val compiledSteps = mutableListOf<CompiledStep>()

        for (stepDto in dto.steps) {
            val stepId = rl(stepDto.id)

            when (stepDto.kind) {
                StepKind.SELECT -> {
                    val range = stepDto.params["range"]?.toFloat() ?: 16f
                    val rtype = stepDto.params["resultType"]?.let { ResultType.valueOf(it) } ?: ResultType.MULTIPLE
                    val amount = stepDto.params["amount"]?.toInt() ?: -1

                    // Here we assume SELECT steps refer to entity selectors.
                    // If your DTO needs to specify block selectors, add a 'mode' param (e.g. "targetMode":"block"|"entity")
                    val mode = stepDto.params["targetMode"] ?: "entity"

                    if (mode == "block") {
                        // compile block filter refs
                        val compiledBlockFilters = stepDto.filterRefs.map { fid ->
                            GlobalSpecRegistry.compileFilter<PlatformBlock<*>>(FilterRef(rl(fid)))
                        }
                        val selFn: (SelectorContext) -> AmountableResult<PlatformBlock<*>> = { ctx ->
                            // prefer an engine-provided block-in-sight selector; fallback to scanning
                            val hit = ctx.world.getBlockAt(ctx.originX, ctx.originY, ctx.originZ) // naive; replace with raycast/sample
                            val list = if (hit != null && compiledBlockFilters.all { it.test(hit, ctx) }) listOf(hit) else emptyList()
                            AmountableResult(ResultType.SINGLE, list)
                        }
                        compiledSteps += CompiledStep.BlockSelectorStep(CompiledBlockSelector(stepId, selFn))
                    } else {
                        // entity selector path (same as before)
                        val compiledFilters = stepDto.filterRefs.map { fid ->
                            GlobalSpecRegistry.compileFilter<PlatformLivingEntity>(FilterRef(rl(fid)))
                        }

                        val selFn: (SelectorContext) -> AmountableResult<PlatformLivingEntity> = { ctx ->
                            val base: List<PlatformLivingEntity> = try {
                                ctx.world.getLivingEntitiesWithin(ctx.originX, ctx.originY, ctx.originZ, range)
                            } catch (ex: Throwable) {
                                ctx.world.getEntitiesWithin(ctx.originX, ctx.originY, ctx.originZ, range)
                                    .filterIsInstance<PlatformLivingEntity>()
                            }
                            val filtered = base.filter { candidate -> compiledFilters.all { cf -> cf.test(candidate, ctx) } }
                            when (rtype) {
                                ResultType.SINGLE -> AmountableResult(ResultType.SINGLE, filtered.take(1))
                                ResultType.MULTIPLE -> AmountableResult(ResultType.MULTIPLE, filtered)
                                ResultType.RANDOM_MULTIPLE -> AmountableResult(ResultType.MULTIPLE, filtered.shuffled())
                                ResultType.RANDOM_SINGLE -> if (filtered.isEmpty()) AmountableResult(ResultType.RANDOM_SINGLE, emptyList()) else AmountableResult(ResultType.RANDOM_SINGLE, listOf(filtered[Random.nextInt(filtered.size)]))
                                ResultType.RANDOM_AMOUNTED_MULTIPLE -> AmountableResult(ResultType.RANDOM_AMOUNTED_MULTIPLE, filtered.shuffled().take(maxOf(0, amount)))
                                ResultType.AMOUNTED_MULTIPLE -> AmountableResult(ResultType.AMOUNTED_MULTIPLE, filtered.sortedBy { e ->
                                    val dx = e.location.x - ctx.originX
                                    val dy = e.location.y - ctx.originY
                                    val dz = e.location.z - ctx.originZ
                                    dx * dx + dy * dy + dz * dz
                                }.take(maxOf(0, amount)))
                            }
                        }
                        compiledSteps += CompiledStep.EntitySelectorStep(CompiledEntitySelector(stepId, selFn))
                    }
                }
                StepKind.CONDITION -> {
                    // by default treat as entity condition
                    val mode = stepDto.params["targetMode"] ?: "entity"
                    if (mode == "block") {
                        val dto = CompiledConditionTaskStep(stepId, blockConditionRefs =
                            stepDto.conditionRefs.map { BlockConditionRef(rl(it)) }, params = stepDto.params)
                        val compiledBlockConds = dto.blockConditionRefs.map { GlobalSpecRegistry.compileBlockCondition(it) }
                        compiledSteps += CompiledStep.BlockConditionStep(dto, compiledBlockConds)
                    } else {
                        val dto = CompiledConditionTaskStep(stepId, conditionRefs = stepDto.conditionRefs.map { ConditionRef(rl(it)) }, params = stepDto.params)
                        val compiledConditions = dto.conditionRefs.map { GlobalSpecRegistry.compileCondition(it) }
                        compiledSteps += CompiledStep.EntityConditionStep(dto, compiledConditions)
                    }
                }
                StepKind.ACTION -> {
                    val mode = stepDto.params["targetMode"] ?: "entity"
                    if (mode == "block") {
                        val dto = CompiledVoidTaskStep(stepId, blockActionRefs = stepDto.actionRefs.map { BlockActionRef(rl(it)) }, blockTimedActionRefs = stepDto.timedRefs.map { TimedRef(rl(it)) }, params = stepDto.params)
                        val compiledBlockActions = dto.blockActionRefs.map { GlobalSpecRegistry.compileBlockAction(it) }
                        val compiledBlockTimedActions = dto.blockTimedActionRefs.map { GlobalSpecRegistry.compileBlockTimedAction(it) }
                        compiledSteps += CompiledStep.BlockActionStep(dto, compiledBlockActions, compiledBlockTimedActions)
                    } else {
                        val dto = CompiledVoidTaskStep(stepId, entityActionRefs = stepDto.actionRefs.map { ActionRef(rl(it)) }, entityTimedRefs = stepDto.timedRefs.map { TimedRef(rl(it)) }, params = stepDto.params)
                        val compiledActions = dto.entityActionRefs.map { GlobalSpecRegistry.compileAction(it) }
                        val compiledTimeActions = dto.entityTimedRefs.map { GlobalSpecRegistry.compileTimedAction(it) }
                        compiledSteps += CompiledStep.EntityActionStep(dto, compiledActions, compiledTimeActions)
                    }
                }
            }
        }

        return CompiledTask(
            id = rl(dto.id),
            type = dto.type,
            lifecycle = dto.lifecycle,
            priority = dto.priority,
            chance = dto.chance,
            steps = compiledSteps,
            cooldownTicks = dto.cooldownTicks,
            completionPredicate = dto.completionPredicate,
            waitForSignals = dto.waitSignals,
            allSignalsRequired = dto.allSignalsRequired,
            stopSignals = dto.stopSignals,
            allStopSignalsRequired = dto.allStopSignalsRequired
        )
    }
}

// Inline helper id generator
object InlineIdGen {
    private var i = 0
    fun next(s: String) = rl("runtime", "${s}_${i++}")
}

// ----- inline registration helpers for entity and block things -----

fun registerInlineFilter(possibleId: ResourceLocation, lambda: (PlatformEntity, SelectorContext) -> Boolean): FilterRef<PlatformEntity> {
    if (!GlobalSpecRegistry.hasFilterFactory(possibleId)) {
        val id = InlineIdGen.next("filter_entity")
        GlobalSpecRegistry.registerFilter(id) { CompiledFilter(id) { e, ctx -> lambda(e, ctx) } }
        return FilterRef(id)
    }
    return FilterRef(possibleId)
}

fun registerInlineFilter(possibleId: ResourceLocation, lambda: (PlatformBlock<*>, SelectorContext) -> Boolean, internal_marker: Int = -225): FilterRef<PlatformBlock<*>> {
    if (!GlobalSpecRegistry.hasFilterFactory(possibleId)) {
        val id = InlineIdGen.next("filter_block")
        GlobalSpecRegistry.registerFilter(id) { CompiledFilter(id) { b, ctx -> lambda(b, ctx) } }
        return FilterRef(id)
    }
    return FilterRef(possibleId)
}

fun registerInlineAction(possibleId: ResourceLocation, lambda: (PlatformLivingEntity, PlatformLivingEntity?) -> Boolean): ActionRef {
    if (!GlobalSpecRegistry.hasActionFactory(possibleId)) {
        val id = InlineIdGen.next("action_entity")
        GlobalSpecRegistry.registerAction(id) { _ -> CompiledEntityAction(id, lambda) }
        return ActionRef(id)
    }
    return ActionRef(possibleId)
}

fun registerInlineBlockAction(possibleId: ResourceLocation, lambda: (PlatformLivingEntity, PlatformBlock<*>) -> Boolean, internal_marker: Int = -225): BlockActionRef {
    if (!GlobalSpecRegistry.hasBlockActionFactory(possibleId)) {
        val id = InlineIdGen.next("action_block")
        GlobalSpecRegistry.registerBlockAction(id, { _ -> CompiledBlockAction(id, lambda) })
        return BlockActionRef(id)
    }
    return BlockActionRef(possibleId)
}

fun registerInlineCondition(possibleId: ResourceLocation, lambda: (PlatformLivingEntity) -> Boolean, mustBeTrue: Boolean = true): ConditionRef {
    if (!GlobalSpecRegistry.hasConditionFactory(possibleId)) {
        val id = InlineIdGen.next("condition_entity")
        GlobalSpecRegistry.registerCondition(id, { _ -> CompiledCondition(id, { ent -> lambda(ent) }, mustBeTrue) })
        return ConditionRef(id, emptyMap(), mustBeTrue)
    }
    return ConditionRef(possibleId, emptyMap(), mustBeTrue)
}

fun registerInlineBlockCondition(possibleId: ResourceLocation, lambda: (PlatformBlock<*>) -> Boolean, mustBeTrue: Boolean = true, internal_marker: Int = -225): BlockConditionRef {
    if (!GlobalSpecRegistry.hasBlockConditionFactory(possibleId)) {
        val id = InlineIdGen.next("condition_block")
        GlobalSpecRegistry.registerBlockCondition(id, { _ -> CompiledBlockCondition(id, { b -> lambda(b) }, mustBeTrue) })
        return BlockConditionRef(id, emptyMap(), mustBeTrue)
    }
    return BlockConditionRef(possibleId, emptyMap(), mustBeTrue)
}

fun registerInlineBlockTimedAction(
    possibleId: ResourceLocation?,
    start: (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean,
    tick: (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean,
    end: (PlatformLivingEntity, PlatformBlock<*>?) -> Unit,
    defaultDuration: Int = 40,
    defaultInterval: Int = 1,
    runBlocking: Boolean = true
): TimedRef {
    val id = possibleId?.takeIf { GlobalSpecRegistry.hasBlockTimedActionFactory(it) } ?: InlineIdGen.next("block_timed_action")
    if (!GlobalSpecRegistry.hasBlockTimedActionFactory(id)) {
        GlobalSpecRegistry.registerBlockTimedAction(id, { ref ->
            CompiledBlockTimedAction(id, defaultDuration, defaultInterval, start, tick, end)
        })
    }
    return TimedRef(id, duration = null, interval = null, runBlocking = runBlocking, slot = null)
}

fun registerInlineTimedAction(
    possibleId: ResourceLocation?,
    start: (PlatformLivingEntity, PlatformLivingEntity?) -> Boolean,
    tick: (PlatformLivingEntity, PlatformLivingEntity?) -> Boolean,
    end: (PlatformLivingEntity, PlatformLivingEntity?) -> Unit,
    defaultDuration: Int = -1,
    defaultInterval: Int = 1,
    runBlocking: Boolean = true, internal_marker: Int = -225
): TimedRef {
    val id = possibleId?.takeIf { GlobalSpecRegistry.hasTimedActionFactory(it) } ?: InlineIdGen.next("timed_action")
    if (!GlobalSpecRegistry.hasTimedActionFactory(id)) {
        GlobalSpecRegistry.registerTimedAction(id) { ref ->
            CompiledTimedAction(id, defaultDuration, defaultInterval, start, tick, end)
        }
    }
    return TimedRef(id, duration = null, interval = null, runBlocking = runBlocking, slot = null)
}

// --------------------- helper + types: ResultType, SelectorContext, Spec interfaces ---------------------

enum class ResultType {
    SINGLE, MULTIPLE, RANDOM_SINGLE, RANDOM_MULTIPLE,  RANDOM_AMOUNTED_MULTIPLE, AMOUNTED_MULTIPLE
}

interface Spec {
    fun id(): ResourceLocation
}

data class SelectorContext(
    val world: PlatformWorld<*, *>,
    val originX: Double,
    val originY: Double,
    val originZ: Double,
    val self: PlatformEntity? = null,
    val tick: Long = 0
) {
    companion object {
        fun ofEntity(self: PlatformEntity): SelectorContext {
            return SelectorContext(self.world, self.location.x, self.location.y, self.location.z, self)
        }
        fun ofBlock(b: PlatformBlock<*>): SelectorContext {
            return SelectorContext(b.location.world, b.location.x, b.location.y, b.location.z, null)
        }
    }
}

interface FilterSpec<T> : Spec {
    fun isValid(obj: T, ctx: SelectorContext): Boolean
}

interface ConditionSpec<T> : Spec {
    fun isNecessary(): Boolean
    fun isTrue(obj: T): Boolean
}

interface ActionSpec<T, Y> : Spec {
    fun perform(self: T, target: Y?): Boolean
}

interface TimedActionSpec<T, Y> : Spec {
    fun onStart(self: T, target: Y?): Boolean
    fun onTick(self: T, target: Y?): Boolean
    fun onEnd(self: T, target: Y?)
}

object SetTargetToLookAt : EntityTimedActionSpec<PlatformLivingEntity>(
    rl("core", "set_look_at_target"),
    { _, _ -> true },
    lambda@{ self, target ->
        if (target != null) {
            if (self !is PlatformLivingEntity) return@lambda false
            else {
                val from = self.location
                val to = target.location

                val dx = to.x - from.x
                val dy = to.y - from.y
                val dz = to.z - from.z

                val distXZ = kotlin.math.sqrt(dx * dx + dz * dz)

                // Correct yaw: positive X is -90, positive Z is 0
                val yaw = Math.toDegrees(kotlin.math.atan2(dz, dx)).toFloat() - 90f
                // Correct pitch: negative when looking up
                val pitch = Math.toDegrees(-kotlin.math.atan2(dy, distXZ)).toFloat()

                self.setRotation(yaw, pitch)
                return@lambda true
            }
        }
        else return@lambda false
    },
    { self, _ -> self.setRotation(0f, 0f) }
)

class SayToTarget(private val message: String) : EntityActionSpec<PlatformLivingEntity>(
    rl("core", "say_to_target"),
    run@{ self, player ->
        if (player == null) return@run false
        if (!player.isPlayer) return@run false
        player as PlatformPlayer
        player.sendMessage(self, message)
        true
    }
)

class SayToAllPlayersInWorld(private val message: String) : EntityActionSpec<PlatformLivingEntity>(
    rl("core", "say_to_target"),
    run@{ self, _ ->
        self.world.players.forEach {
            it.sendMessage(self, message)
        }
        true
    }
)

class SayToAttacker(private val message: String) : EntityActionSpec<PlatformLivingEntity>(
    rl("core", "say_to_target"),
    run@{ self, _ ->
        if (self !is PlatformLivingEntity) return@run false
        val attacker = self.getLastAttacker()
        if (attacker is PlatformPlayer) {
            attacker.sendMessage(self, message)
            return@run true
        }
        false
    }
)

object LookAtAttacker : EntityTimedActionSpec<PlatformLivingEntity>(
    rl("core", "set_look_at_target"),
    { _, _ -> true },
    lambda@{ self, _ ->
        if (self !is PlatformLivingEntity) return@lambda false
        else {
            val target = self.getLastAttacker()
            if (target != null) {
                val from = self.location
                val to = target.location

                val dx = to.x - from.x
                val dy = to.y - from.y
                val dz = to.z - from.z

                val distXZ = kotlin.math.sqrt(dx * dx + dz * dz)

                // Correct yaw: positive X is -90, positive Z is 0
                val yaw = Math.toDegrees(kotlin.math.atan2(dz, dx)).toFloat() - 90f
                // Correct pitch: negative when looking up
                val pitch = Math.toDegrees(-kotlin.math.atan2(dy, distXZ)).toFloat()

                self.setRotation(yaw, pitch)
                return@lambda true
            }
            return@lambda false
        }
    },
    { self, _ -> self.setRotation(0f, 0f) }
)

/** Cone filter: only entities within a cone in front of the source pass. */
class ConeFilter(
    private val angle: Float
) : EntityFilterSpec(
    rl("core", "cone_angle_based_filter"),
    lambda@{ obj, ctx ->
        val self = ctx.self ?: return@lambda true
        val dir = self.lookDirection.dir
        val toTarget = obj.location.subtract(self.location).normalize()
        return@lambda dir.dot(toTarget) >= cos(toRadians(angle / 2.0))
    }
)

/** Accept only player entities */
object PlayersOnly : EntityFilterSpec(
    rl("core", "player_only_filter"),
    { obj, _ -> obj.isPlayer }
)

/** Accept only entities that are not dead. */
object AliveEntitiesOnly : EntityFilterSpec(
    rl("core", "alive_entities_only_filter"),
    { obj, _ -> !obj.isDead }
)

/** Accept only living entities. */
object LivingEntitiesOnly : EntityFilterSpec(
    rl("core", "living_type_entities_only_filter"),
    { obj, _ -> obj is PlatformLivingEntity }
)

// typed spec for timed block actions (registers factory on construction)
open class BlockTimedActionSpec(
    val id: ResourceLocation,
    private val onStart: (PlatformLivingEntity, PlatformBlock<*>?, ContextLogger) -> Boolean,
    private val onTick: (PlatformLivingEntity, PlatformBlock<*>?, ContextLogger) -> Boolean,
    private val onEnd: (PlatformLivingEntity, PlatformBlock<*>?, ContextLogger) -> Unit
) {
    companion object val LOGGER = ContextLogger(ContextLogger.ContextType.MINI_FEATURE, "BLOCK-TIMED-DEBUGGER")
    init {
        if (!GlobalSpecRegistry.hasBlockTimedActionFactory(id)) {
            GlobalSpecRegistry.registerBlockTimedAction(id, { ref ->
                // ref.duration / ref.interval will be applied by compiled action (same pattern as entity version)
                CompiledBlockTimedAction(id, ref.duration ?: 40, ref.interval ?: 1, { s, b -> onStart.invoke(s, b, LOGGER) }, { s, b -> onTick.invoke(s, b, LOGGER) }, { s, b -> onEnd.invoke(s, b, LOGGER) })
            })
        }
    }

    fun onStart(): (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean = { s, b -> onStart.invoke(s, b, LOGGER) }
    fun onTick(): (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean = { s, b -> onTick.invoke(s, b, LOGGER) }
    fun onEnd(): (PlatformLivingEntity, PlatformBlock<*>?) -> Unit = { s, b -> onEnd.invoke(s, b, LOGGER) }

    fun id(): ResourceLocation = id
}

object WalkToBlock : BlockTimedActionSpec(
    rl("core", "walk_to_block"),
    onStart = start@{ self, block, logger ->
        if (block == null) return@start false
        self.getNavigator()?.let {
            val path = it.createPath(block.blockPos, self.lookDistance.toInt())
            logger.debug("start - Path created: $path")
            logger.debug("start - Path is null: ${path == null}")
            logger.debug("start - Navigator target: ${it.getTargetPos()}")
            if (path == null) return@start false
            it.moveTo(path, self.getSpeed().toDouble())
            return@start true
        }
        logger.warn("start - Entity ${self.typeId} is not a path based entity and cannot walk-to-block")
        false
    },
    onTick = onTick@{ self, _, logger ->
        val nav = self.getNavigator() ?: return@onTick false
        logger.debug("tick - isDone: ${nav.isDone()}")
        logger.debug("tick - isInProgress: ${nav.isInProgress()}")
        logger.debug("tick - isStuck: ${nav.isStuck()}")
        logger.debug("tick - Entity ${self.typeId} is navigating to block ${nav.getPath()}")
        !nav.isDone()
    },
    onEnd = { self, _, logger ->
        logger.debug("end - Entity ${self.typeId} has finished navigating or has been terminated.")
        self.getNavigator()?.stop()
    }
)


/**
 * A lambda-backed condition spec.
 * `mustBeTrue` indicates whether this condition is required (AND) or optional (OR) when combined.
 */
open class EntityConditionSpec(
    val id: ResourceLocation,
    val validator: (PlatformEntity) -> Boolean,
    val mustBeTrue: Boolean
) : ConditionSpec<PlatformEntity> {
    init {
        GlobalSpecRegistry.registerCondition(
            id,
            getConditionFactory()
        )
    }

    override fun id(): ResourceLocation = id
    override fun isNecessary(): Boolean = mustBeTrue
    override fun isTrue(obj: PlatformEntity): Boolean =
        validator.invoke(obj)
    open fun getConditionFactory(): ConditionFactory {
        return ConditionFactory { ref ->
            CompiledCondition(
                ref.id, validator,
                ref.mustBeTrue
            )
        }
    }
}

/**
 * A lambda-backed condition spec.
 * `mustBeTrue` indicates whether this condition is required (AND) or optional (OR) when combined.
 */
open class BlockConditionSpec(
    val id: ResourceLocation,
    private val validator: (PlatformBlock<*>) -> Boolean,
    private val mustBeTrue: Boolean
) : ConditionSpec<PlatformBlock<*>> {
    init {
        GlobalSpecRegistry.registerBlockCondition(
            id,
            getConditionFactory()
        )
    }

    override fun id(): ResourceLocation = id
    override fun isNecessary(): Boolean = mustBeTrue
    override fun isTrue(obj: PlatformBlock<*>): Boolean =
        validator.invoke(obj)
    open fun getConditionFactory(): BlockConditionFactory {
        return BlockConditionFactory { ref ->
            CompiledBlockCondition(
                ref.id, validator,
                ref.mustBeTrue
            )
        }
    }
}

// SelectorSpec kept generic-by-intent, but we create concrete Entity/Block subclasses below.
abstract class SelectorSpec<X : Any, T : FilterSpec<X>>(val id: ResourceLocation, val resultType: ResultType, val range: Double, val filters: List<T>) : Spec {
    abstract fun findCandidates(ctx: SelectorContext): List<X>
    open fun get(ctx: SelectorContext): AmountableResult<X> {
        val base = findCandidates(ctx)
        val filtered = base.filter { x -> filters.all { it.isValid(x, ctx) } }
        return AmountableResult(resultType, filtered)
    }
    override fun id(): ResourceLocation = id
}

interface ValidatorBasedFilterSpec<T> {
    fun validator(): (T, SelectorContext) -> Boolean
}
interface EntityObjectableFilterSpec<T> : ValidatorBasedFilterSpec<T>, FilterSpec<T>

// --------------------- lambda-backed filter / action / condition wrappers ---------------------

open class EntityFilterSpec(val id: ResourceLocation, private val validator: (PlatformEntity, SelectorContext) -> Boolean)
    : EntityObjectableFilterSpec<PlatformEntity> {
    init {
        if (!GlobalSpecRegistry.hasFilterFactory(id))
            GlobalSpecRegistry.registerFilter(id,
                FilterFactory<PlatformEntity> { ref -> CompiledFilter(ref.id) { e, ctx -> validator(e, ctx) } })
    }
    override fun id(): ResourceLocation = id
    override fun isValid(obj: PlatformEntity, ctx: SelectorContext): Boolean = validator(obj, ctx)
    override fun validator(): (PlatformEntity, SelectorContext) -> Boolean = validator
}

open class BlockFilterSpec(val id: ResourceLocation, private val validator: (PlatformBlock<*>, SelectorContext, ContextLogger) -> Boolean)
    : EntityObjectableFilterSpec<PlatformBlock<*>> {
    companion object val LOGGER = ContextLogger(ContextLogger.ContextType.MINI_FEATURE, "SELECTION-DEBUG")
    init {
        if (!GlobalSpecRegistry.hasFilterFactory(id))
            GlobalSpecRegistry.registerFilter(id
            ) { ref -> CompiledFilter(ref.id) { b, ctx -> validator(b, ctx, LOGGER) } }
    }
    override fun id(): ResourceLocation = id
    override fun isValid(obj: PlatformBlock<*>, ctx: SelectorContext): Boolean = validator(obj, ctx, LOGGER)
    override fun validator(): (PlatformBlock<*>, SelectorContext) -> Boolean = { p, s -> validator.invoke(p, s, LOGGER) }
}

open class EntityActionSpec<T : PlatformEntity>(val id: ResourceLocation, val runner: (PlatformEntity, PlatformEntity?) -> Boolean) : ActionSpec<T, T?> {
    init {
        if (!GlobalSpecRegistry.hasActionFactory(id))
            GlobalSpecRegistry.registerAction(id, { ref -> CompiledEntityAction(ref.id, runner) })
    }
    override fun id(): ResourceLocation = id
    override fun perform(self: T, target: T?): Boolean = runner(self, target)
}

open class BlockActionSpec(val id: ResourceLocation, val runner: (PlatformLivingEntity, PlatformBlock<*>) -> Boolean) {
    init {
        if (!GlobalSpecRegistry.hasBlockActionFactory(id))
            GlobalSpecRegistry.registerBlockAction(id, { ref -> CompiledBlockAction(ref.id, runner) })
    }
    fun perform(self: PlatformLivingEntity, target: PlatformBlock<*>): Boolean = runner(self, target)
}

open class EntityTimedActionSpec<T: PlatformLivingEntity>(
    val id: ResourceLocation,
    val onStartV: (PlatformEntity, PlatformEntity?) -> Boolean,
    val runner: (PlatformEntity, PlatformEntity?) -> Boolean,
    val onEndV: (PlatformEntity, PlatformEntity?) -> Unit
) : TimedActionSpec<PlatformEntity, PlatformEntity?> {
    init {
        if (!GlobalSpecRegistry.hasTimedActionFactory(id))
            GlobalSpecRegistry.registerTimedAction(id) { ref ->
                CompiledTimedAction(
                    id, ref.duration ?: 40, ref.interval ?: 1,
                    { s, e -> onStart(s, e) },
                    { s, e -> runner(s, e) },
                    { s, e -> onEnd(s, e) }
                )
            }
    }
    override fun onStart(self: PlatformEntity, target: PlatformEntity?): Boolean = onStartV(self, target)
    override fun onTick(self: PlatformEntity, target: PlatformEntity?): Boolean = runner(self, target)
    override fun onEnd(self: PlatformEntity, target: PlatformEntity?) { onEndV(self, target) }
    override fun id(): ResourceLocation = id
}

// --------------------- Block selectors ---------------------
class BlockInRadiusSelector(
    range: Double,
    filters: List<BlockFilterSpec>
) : SelectorSpec<PlatformBlock<*>, BlockFilterSpec>(
    id = rl("core", "block_in_sight"),
    resultType = ResultType.SINGLE,
    range = range,
    filters = filters
) {
    companion object val LOGGER = ContextLogger(ContextLogger.ContextType.MINI_FEATURE, "SELECTION-DEBUG")
    override fun findCandidates(ctx: SelectorContext): List<PlatformBlock<*>> {
        val world = ctx.world
        val blocks = mutableListOf<PlatformBlock<*>>()

        var index = 0
        for (x in (ctx.originX - range).toInt()..(ctx.originX + range).toInt())
            for (z in (ctx.originZ - range).toInt()..(ctx.originZ + range).toInt())
                for (y in world.getMaxMinimumY()..world.getHighestBlockYAt(x, z)) {
                    val block = world.getBlockAt(x, y, z)
                    blocks += block
                    index++
                }

        return blocks
    }
}

// --------------------- Block Filters ------------------------
object BlockIsWalkableFilter : BlockFilterSpec(
    rl("core", "block_is_walkable_filter"),
    { block, _, LOGGER ->
        block.material.isSolid && !block.material.isAir
    }
)
object BlockIsHighest : BlockFilterSpec(
    rl("core", "block_is_walkable_filter"),
    { block, ctx, LOGGER ->
        val range = 0.5
        val result =
            abs(ctx.world.getHighestBlockYAt(block.location.x, block.location.z) -
                    floor(block.location.y).toInt()) < range
        result
    }
)

// --------------------- Amountable result ---------------------

data class AmountableResult<T>(val resultType: ResultType, private val result: List<T>) {
    fun getSingleResult(): T? {
        return when (resultType) {
            ResultType.SINGLE -> result.firstOrNull()
            ResultType.RANDOM_SINGLE -> result.randomOrNull()
            else -> throw IllegalStateException("Trying to access single result from multiple-result container.")
        }
    }
    fun getResults(): List<T> {
        return when (resultType) {
            ResultType.MULTIPLE -> result
            ResultType.RANDOM_MULTIPLE -> result.shuffled()
            else -> throw IllegalStateException("Trying to access multiple results from single-result container.")
        }
    }
    fun getAmountedResults(amount: Int): List<T> {
        return when (resultType) {
            ResultType.AMOUNTED_MULTIPLE -> result.take(amount)
            ResultType.RANDOM_AMOUNTED_MULTIPLE -> result.shuffled().take(amount)
            else -> throw IllegalStateException("Trying to access multiple results from single-result container.")
        }
    }

    override fun toString(): String {
        return "AmountableResult(resultType=$resultType, result=$result)"
    }
}

// --------------------- Compiled step sealed class ---------------------

sealed class CompiledStep {
    data class EntitySelectorStep(val selector: CompiledEntitySelector) : CompiledStep()
    data class BlockSelectorStep(val selector: CompiledBlockSelector) : CompiledStep()

    data class EntityConditionStep(val dto: CompiledConditionTaskStep, val compiled: List<CompiledCondition>) : CompiledStep()
    data class BlockConditionStep(val dto: CompiledConditionTaskStep, val compiledBlock: List<CompiledBlockCondition>) : CompiledStep()

    data class EntityActionStep(val dto: CompiledVoidTaskStep, val compiledAction: List<CompiledEntityAction>, val compiledTimedAction: List<CompiledTimedAction>) : CompiledStep()
    data class BlockActionStep(val dto: CompiledVoidTaskStep, val compiledBlockAction: List<CompiledBlockAction>, val compiledTimedAction: List<CompiledBlockTimedAction>) : CompiledStep()
}

// --------------------- DTOs for serialization ---------------------

@kotlinx.serialization.Serializable
enum class StepKind { SELECT, CONDITION, ACTION }

@kotlinx.serialization.Serializable
data class StepDTO(
    val id: String,
    val kind: StepKind,
    val params: Map<String, String> = emptyMap(),
    val actionRefs: List<String> = emptyList(),
    val timedRefs: List<String> = emptyList(),
    val conditionRefs: List<String> = emptyList(),
    val filterRefs: List<String> = emptyList()
)

@kotlinx.serialization.Serializable
data class TaskSpecDTO(
    val id: String,
    val type: TaskType,
    val lifecycle: TaskLifecycle,
    val priority: Int = 0,
    val chance: Double = 0.5,
    val steps: List<StepDTO> = emptyList(),
    val cooldownTicks: Int = -1,
    val completionPredicate: (PlatformEntity, @Contextual EntityTaskState) -> Boolean,
    val waitSignals: Set<String>,
    val allSignalsRequired: Boolean,
    val stopSignals: Set<String>,
    val allStopSignalsRequired: Boolean
)

enum class TaskType { CONDITIONED, RANDOM }
enum class TaskLifecycle { ONE_SHOT, REPEATING, UNTIL_CONDITION }

// --------------------- TaskBuilder with entity / block modes ---------------------

class TaskBuilder private constructor(val ordinal: PlatformLivingEntity, val id: ResourceLocation, val type: TaskType, val lifecycle: TaskLifecycle, val priority: Int, val chance: Double = 0.5) {
    private val steps = mutableListOf<ParentableTaskStep>()
    private var cooldownTicks: Int = -1
    private var LOGGER: AsyncContextLogger = AsyncContextLogger(ContextLogger.ContextType.SUB_SYSTEM, "TASK-RUNNER-[$id]")

    private enum class Mode { ENTITY, BLOCK }
    private var mode: Mode = Mode.ENTITY
    private var completionPredicate: ((PlatformEntity, EntityTaskState) -> Boolean) = { _, _ -> true }
    private val waitSignals = mutableSetOf<String>()
    private val stopSignals = mutableSetOf<String>()
    private var allSignalsRequired = false
    private var allStopSignalsRequired = false

    companion object {
        private fun of(ordinal: PlatformLivingEntity, id: ResourceLocation, type: TaskType, lifecycle: TaskLifecycle, priority: Int, chance: Double = 0.5): TaskBuilder =
            TaskBuilder(ordinal, id, type, lifecycle, priority, chance)
        @JvmOverloads
        fun random(ordinal: PlatformLivingEntity, id: ResourceLocation, lifecycle: TaskLifecycle = TaskLifecycle.ONE_SHOT, priority: Int = 0, chance: Double = 0.5) =
            of(ordinal, id, TaskType.RANDOM, lifecycle, priority, chance)
        @JvmOverloads
        fun conditioned(ordinal: PlatformLivingEntity, id: ResourceLocation, lifecycle: TaskLifecycle = TaskLifecycle.UNTIL_CONDITION, priority: Int = 0) =
            of(ordinal, id, TaskType.CONDITIONED, lifecycle, priority)
    }

    fun completionPredicate(sP: (PlatformEntity, EntityTaskState) -> Boolean): TaskBuilder {
        this.completionPredicate = sP
        return this
    }

    fun untilReceive(signal: String): TaskBuilder {
        waitSignals += signal
        return this
    }

    fun runUntilReceive(signal: String): TaskBuilder {
        stopSignals += signal
        return this
    }
    fun allSignalsRequired(): TaskBuilder {
        allSignalsRequired = true
        return this
    }
    fun allStopSignalsRequired(): TaskBuilder {
        allStopSignalsRequired = true
        return this
    }

    // mode switches
    fun entityMode(): TaskBuilder { mode = Mode.ENTITY; return this }
    fun blockMode(): TaskBuilder { mode = Mode.BLOCK; return this }

    // selection entry-points - entity or block explicit helpers
    fun withRange(id: ResourceLocation, range: Double = ordinal.lookDistance): EntitySelectionTaskStep {
        entityMode()
        return EntitySelectionTaskStep(id, range, this)
    }
    fun withBlockRange(id: ResourceLocation, range: Double = ordinal.lookDistance): BlockSelectionTaskStep {
        blockMode()
        return BlockSelectionTaskStep(id, range, this)
    }

    fun performOnTarget(id: ResourceLocation): ActionTaskStep { entityMode(); return ActionTaskStep(id, this) }
    fun performOnSelf(id: ResourceLocation): ActionTaskStep { entityMode(); return ActionTaskStep(id, this, false) }

    fun performOnBlockTarget(id: ResourceLocation): BlockActionTaskStep { blockMode(); return BlockActionTaskStep(id, this) }

    fun cooldownTicks(ticks: Int): TaskBuilder { this.cooldownTicks = ticks; return this }
    fun isPossibleOnSelf(id: ResourceLocation): ConditionTaskStep { entityMode(); return ConditionTaskStep(id, this, false) }
    fun isPossibleOnTarget(id: ResourceLocation): ConditionTaskStep { entityMode(); return ConditionTaskStep(id, this) }

    fun isPossibleOnBlockTarget(id: ResourceLocation): BlockConditionTaskStep { blockMode(); return BlockConditionTaskStep(id, this) }

    fun addInLine(child: ParentableTaskStep) { this.steps.add(child) }

    // main build -> compiles steps to runtime compiled steps
    fun build(): CompiledTask {
        val compiledSteps = mutableListOf<CompiledStep>()

        for (step in steps) {
            when (step) {
                is EntitySelectionTaskStep -> compiledSteps += CompiledStep.EntitySelectorStep(step.compile())
                is BlockSelectionTaskStep -> compiledSteps += CompiledStep.BlockSelectorStep(step.compile())
                is ConditionTaskStep -> {
                    val dto = step.compile()
                    val compiledConds = dto.conditionRefs.map { GlobalSpecRegistry.compileCondition(it) }
                    compiledSteps += CompiledStep.EntityConditionStep(dto, compiledConds)
                }
                is BlockConditionTaskStep -> {
                    val dto = step.compile()
                    val compiledBlockConds = dto.blockConditionRefs.map { GlobalSpecRegistry.compileBlockCondition(it) }
                    compiledSteps += CompiledStep.BlockConditionStep(dto, compiledBlockConds)
                }
                is ActionTaskStep -> {
                    val dto = step.compile()
                    val compiledActions = dto.entityActionRefs.map { GlobalSpecRegistry.compileAction(it) }
                    val compiledTimedActions = dto.entityTimedRefs.map { GlobalSpecRegistry.compileTimedAction(it) }
                    compiledSteps += CompiledStep.EntityActionStep(dto, compiledActions, compiledTimedActions)
                }
                is BlockActionTaskStep -> {
                    val dto = step.compile()
                    val compiledBlockActions = dto.blockActionRefs.map { GlobalSpecRegistry.compileBlockAction(it) }
                    val compiledTimedBlockActions = dto.blockTimedActionRefs.map { GlobalSpecRegistry.compileBlockTimedAction(it) }
                    compiledSteps += CompiledStep.BlockActionStep(dto, compiledBlockActions, compiledTimedBlockActions)
                }
                else -> LOGGER.debug("While building task: $id, the step $step is not a supported type")
            }
        }

        return CompiledTask(id = this.id, type = this.type, priority = this.priority, steps = compiledSteps.toList(), chance = this.chance,
            completionPredicate = this.completionPredicate, lifecycle = this.lifecycle, cooldownTicks = this.cooldownTicks, waitForSignals = waitSignals, allSignalsRequired = allSignalsRequired,
            stopSignals = this.stopSignals, allStopSignalsRequired = this.allStopSignalsRequired)
    }

    // toDto left as-is for entity selection only in this snippet - extend DTO to mark block vs entity when serializing
    fun toDto(): TaskSpecDTO {
        val stepDtos = steps.mapNotNull { step ->
            when (step) {
                is EntitySelectionTaskStep -> {
                    val params = step.params().mapValues { (_, v) -> v.toString() }
                    val filterRefIds = step.filtersRef.map { it.id.toString() }
                    StepDTO(id = step.id().toString(), kind = StepKind.SELECT, params = params, filterRefs = filterRefIds)
                }
                is BlockSelectionTaskStep -> {
                    val params = step.params().mapValues { (_, v) -> v.toString() } + mapOf("targetMode" to "block")
                    val filterRefIds = step.filtersRef.map { it.id.toString() }
                    StepDTO(id = step.id().toString(), kind = StepKind.SELECT, params = params, filterRefs = filterRefIds)
                }
                is ConditionTaskStep -> {
                    val params = mapOf("isForTarget" to step.isForTarget().toString())
                    val condIds = step.conditionsRef.map { it.id.toString() }
                    StepDTO(id = step.id().toString(), kind = StepKind.CONDITION, params = params, conditionRefs = condIds)
                }
                is BlockConditionTaskStep -> {
                    val params = mapOf("isForTarget" to step.isForTarget().toString(), "targetMode" to "block")
                    val condIds = step.blockRefs.map { it.id.toString() }
                    StepDTO(id = step.id().toString(), kind = StepKind.CONDITION, params = params, conditionRefs = condIds)
                }
                is ActionTaskStep -> {
                    val params = mapOf("isOnTarget" to step.isOnTarget().toString())
                    val actionIds = step.actionsRef.map { it.id.toString() }
                    StepDTO(id = step.id().toString(), kind = StepKind.ACTION, params = params, actionRefs = actionIds)
                }
                is BlockActionTaskStep -> {
                    val params = mapOf("isOnTarget" to step.isOnTarget().toString(), "targetMode" to "block")
                    val actionIds = step.blockActionsRef.map { it.id.toString() }
                    StepDTO(id = step.id().toString(), kind = StepKind.ACTION, params = params, actionRefs = actionIds)
                }
                else -> null
            }
        }

        return TaskSpecDTO(
            id = this.id.toString(),
            type = this.type,
            lifecycle = this.lifecycle,
            priority = this.priority,
            steps = stepDtos,
            cooldownTicks = this.cooldownTicks,
            completionPredicate = this.completionPredicate,
            waitSignals = this.waitSignals,
            allSignalsRequired = this.allSignalsRequired,
            stopSignals = this.stopSignals,
            allStopSignalsRequired = this.allStopSignalsRequired
        )
    }

    // --------------------- step interfaces ---------------------
    enum class SpecType { ACTION, SELECT, CONDITION }
    interface ParentableTaskStep { fun id(): ResourceLocation; fun type(): SpecType; fun end(): TaskBuilder }
    interface VoidTaskStep : ParentableTaskStep { fun isOnTarget(): Boolean; fun compile(): CompiledVoidTaskStep }
    interface ConditionalTaskStep : ParentableTaskStep { fun isForTarget(): Boolean; fun compile(): CompiledConditionTaskStep }
    interface ReturningTaskStep<T> : ParentableTaskStep { fun resultType(): Class<T>; fun params(): Map<String, Any>; fun compile(): Any } // compile returns selector variant

    // --------------------- action / condition / selection step implementations ---------------------

    // entity action step (unchanged)
    class ActionTaskStep(private val id: ResourceLocation, private val parent: TaskBuilder, private val onTarget: Boolean = true): VoidTaskStep {
        internal val actionsRef: MutableList<ActionRef> = mutableListOf()
        internal val entityActionRefs: MutableList<ActionRef> = actionsRef
        internal val timedRef: MutableList<TimedRef> = mutableListOf()

        fun usingActionRef(ref: ActionRef): ActionTaskStep { actionsRef += ref; return this }
        fun usingTimedActionRef(ref: TimedRef): ActionTaskStep { timedRef += ref; return this }

        fun doing(action: EntityActionSpec<PlatformLivingEntity>): ActionTaskStep {
            val ref = registerInlineAction(action.id()) { self, target ->
                if (target != null) action.perform(self, target) else false
            }
            actionsRef += ref
            return this
        }

        fun doingTimed(action: EntityTimedActionSpec<PlatformLivingEntity>, durationOverride: Int? = null, intervalOverride: Int? = null, runBlocking: Boolean = true, slot: String? = null): ActionTaskStep {
            val ref = registerInlineTimedAction(
                possibleId = action.id(),
                start = { s, e -> action.onStart(s, e) },
                tick  = { s, e -> action.onTick(s, e) },
                end   = { s, e -> action.onEnd(s, e) },
                defaultDuration = 40,
                defaultInterval = 1,
                runBlocking = runBlocking
            )
            timedRef += ref.copy(duration = durationOverride, interval = intervalOverride, runBlocking = runBlocking, slot = slot)
            return this
        }

        override fun type(): SpecType = SpecType.ACTION
        override fun end(): TaskBuilder { this.parent.addInLine(this); return parent }
        override fun id(): ResourceLocation = id
        override fun isOnTarget(): Boolean = onTarget
        override fun compile(): CompiledVoidTaskStep = CompiledVoidTaskStep(id, entityActionRefs.toList(), emptyList(), timedRef, emptyList(), mapOf("isOnTarget" to onTarget.toString()))
    }

    // block action step
    class BlockActionTaskStep(private val id: ResourceLocation, private val parent: TaskBuilder, private val onTarget: Boolean = true): VoidTaskStep {
        internal val blockActionsRef: MutableList<BlockActionRef> = mutableListOf()
        internal val blockTimedActionRefs: MutableList<TimedRef> = mutableListOf()

        fun usingBlockActionRef(ref: BlockActionRef): BlockActionTaskStep { blockActionsRef += ref; return this }
        fun usingBlockTimedActionRef(ref: TimedRef): BlockActionTaskStep { blockTimedActionRefs += ref; return this }


        fun doingBlock(action: BlockActionSpec): BlockActionTaskStep {
            val ref = registerInlineBlockAction(action.id, { self, block -> action.perform(self, block) })
            blockActionsRef += ref
            return this
        }
        fun doingTimedBlock(action: BlockTimedActionSpec, durationOverride: Int? = null, intervalOverride: Int? = null,
                            runBlocking: Boolean = true, slot: String? = null): BlockActionTaskStep {
            val ref = registerInlineBlockTimedAction(
                possibleId = action.id(),
                start = { s, b -> action.onStart().invoke(s, b) },
                tick  = { s, b -> action.onTick().invoke(s, b) },
                end   = { s, b -> action.onEnd().invoke(s, b) },
                defaultDuration = 40,
                defaultInterval = 1,
                runBlocking = runBlocking
            )
            blockTimedActionRefs += ref.copy(duration = durationOverride, interval = intervalOverride, runBlocking = runBlocking, slot = slot)
            return this
        }

        override fun type(): SpecType = SpecType.ACTION
        override fun end(): TaskBuilder { this.parent.addInLine(this); return parent }
        override fun id(): ResourceLocation = id
        override fun isOnTarget(): Boolean = onTarget
        override fun compile(): CompiledVoidTaskStep = CompiledVoidTaskStep(id, entityActionRefs = emptyList(),
            blockActionRefs = blockActionsRef.toList(), blockTimedActionRefs = blockTimedActionRefs, params = mapOf("isOnTarget" to onTarget.toString()))
    }

    // entity condition step
    class ConditionTaskStep(private val id: ResourceLocation, private val parent: TaskBuilder, private val onTarget: Boolean = true): ConditionalTaskStep {
        internal val conditionsRef: MutableList<ConditionRef> = mutableListOf()
        fun possibly(condition: EntityConditionSpec): ConditionTaskStep {
            val ref = registerInlineCondition(condition.id, { ent -> condition.isTrue(ent) }, condition.isNecessary())
            conditionsRef += ref
            return this
        }
        fun possiblyRef(ref: ConditionRef): ConditionTaskStep { conditionsRef += ref; return this }
        override fun type(): SpecType = SpecType.CONDITION
        override fun end(): TaskBuilder { this.parent.addInLine(this); return parent }
        override fun id(): ResourceLocation = id
        override fun isForTarget(): Boolean = onTarget
        override fun compile(): CompiledConditionTaskStep = CompiledConditionTaskStep(id, conditionRefs = conditionsRef.toList(), params = mapOf("isForTarget" to onTarget.toString()))
    }

    // block condition step
    class BlockConditionTaskStep(private val id: ResourceLocation, private val parent: TaskBuilder, private val onTarget: Boolean = true): ConditionalTaskStep {
        internal val blockRefs: MutableList<BlockConditionRef> = mutableListOf()
        fun possibly(condition: BlockConditionSpec): BlockConditionTaskStep {
            val ref = registerInlineBlockCondition(condition.id, { b -> condition.isTrue(b) }, condition.isNecessary())
            blockRefs += ref
            return this
        }
        fun possiblyRef(ref: BlockConditionRef): BlockConditionTaskStep { blockRefs += ref; return this }
        override fun type(): SpecType = SpecType.CONDITION
        override fun end(): TaskBuilder { this.parent.addInLine(this); return parent }
        override fun id(): ResourceLocation = id
        override fun isForTarget(): Boolean = onTarget
        override fun compile(): CompiledConditionTaskStep = CompiledConditionTaskStep(id, conditionRefs = emptyList(), blockConditionRefs = blockRefs.toList(), params = mapOf("isForTarget" to onTarget.toString()))
    }

    // entity selection step
    class EntitySelectionTaskStep(private val id: ResourceLocation, private val range: Double, private val parent: TaskBuilder): ReturningTaskStep<PlatformLivingEntity> {
        internal val filtersRef: MutableList<FilterRef<PlatformEntity>> = mutableListOf()
        companion object val LOGGER = ContextLogger(ContextLogger.ContextType.MINI_FEATURE, "ENTITY-SELECTION-DEBUG")

        var resultType: ResultType = ResultType.SINGLE
        var resultAmount: Int = 1

        fun filter(filter: EntityObjectableFilterSpec<PlatformEntity>): EntitySelectionTaskStep {
            val ref = registerInlineFilter(filter.id(), filter.validator())
            filtersRef += ref
            return this
        }
        fun filterRef(ref: FilterRef<PlatformEntity>): EntitySelectionTaskStep { filtersRef += ref; return this }

        fun withSingleResult(): TaskBuilder { this.resultType = ResultType.SINGLE; return end() }
        fun withRandomSingleResult(): TaskBuilder { this.resultType = ResultType.RANDOM_SINGLE; return end() }
        fun withMultipleResult(): TaskBuilder { this.resultType = ResultType.MULTIPLE; return end() }
        fun withRandomMultipleResult(): TaskBuilder { this.resultType = ResultType.RANDOM_MULTIPLE; return end() }
        fun withAmountedMultipleResult(maxAmount: Int): TaskBuilder { this.resultType = ResultType.AMOUNTED_MULTIPLE; this.resultAmount = maxAmount; return end() }
        fun withAmountedRandomMultipleResult(maxAmount: Int): TaskBuilder { this.resultType = ResultType.RANDOM_AMOUNTED_MULTIPLE; this.resultAmount = maxAmount; return end() }

        override fun type(): SpecType = SpecType.SELECT
        override fun end(): TaskBuilder { this.parent.addInLine(this); return parent }
        override fun params(): Map<String, Any> = mapOf("range" to range, "type" to resultType.name, "amount" to resultAmount)

        override fun compile(): CompiledEntitySelector {
            val compiledFilters = filtersRef.map { GlobalSpecRegistry.compileFilter(it) }
            val selFn: (SelectorContext) -> AmountableResult<PlatformLivingEntity> = { ctx ->
                val base = ctx.world.getLivingEntitiesWithin(ctx.originX, ctx.originY, ctx.originZ, range.toFloat())
                LOGGER.debug("the entities retuned were of size: ${base.size}")
                val filtered = base.filter { candidate -> compiledFilters.all { it.test(candidate, ctx) } }
                LOGGER.debug("the entities retuned after filtering were of size: ${base.size}")
                when (resultType) {
                    ResultType.SINGLE -> AmountableResult(ResultType.SINGLE, filtered.take(1))
                    ResultType.RANDOM_MULTIPLE -> AmountableResult(ResultType.MULTIPLE, filtered.shuffled())
                    ResultType.MULTIPLE -> AmountableResult(ResultType.MULTIPLE, filtered)
                    ResultType.RANDOM_SINGLE -> if (filtered.isEmpty()) AmountableResult(ResultType.RANDOM_SINGLE, emptyList()) else AmountableResult(ResultType.RANDOM_SINGLE, listOf(filtered[Random.nextInt(filtered.size)]))
                    ResultType.RANDOM_AMOUNTED_MULTIPLE -> AmountableResult(ResultType.RANDOM_AMOUNTED_MULTIPLE, filtered.shuffled().take(maxOf(1, resultAmount)))
                    ResultType.AMOUNTED_MULTIPLE -> AmountableResult(ResultType.AMOUNTED_MULTIPLE, filtered.sortedBy { e ->
                        val dx = e.location.x - ctx.originX; val dy = e.location.y - ctx.originY; val dz = e.location.z - ctx.originZ
                        dx * dx + dy * dy + dz * dz
                    }.take(maxOf(1, resultAmount)))
                }
            }
            return CompiledEntitySelector(id, selFn)
        }
        override fun id(): ResourceLocation = id
        override fun resultType(): Class<PlatformLivingEntity> = PlatformLivingEntity::class.java
    }

    // block selection step
    class BlockSelectionTaskStep(private val id: ResourceLocation, private val range: Double, private val parent: TaskBuilder): ReturningTaskStep<PlatformBlock<*>> {
        internal val filtersRef: MutableList<FilterRef<PlatformBlock<*>>> = mutableListOf()
        var resultType: ResultType = ResultType.SINGLE
        var resultAmount: Int = -1

        fun filter(filter: BlockFilterSpec): BlockSelectionTaskStep {
            val ref = registerInlineFilter(filter.id, filter.validator())
            filtersRef += ref
            return this
        }
        fun filterRef(ref: FilterRef<PlatformBlock<*>>): BlockSelectionTaskStep { filtersRef += ref; return this }

        fun withFirstSingleResult(): TaskBuilder { this.resultType = ResultType.SINGLE; return end() }
        fun withRandomSingleResult(): TaskBuilder { this.resultType = ResultType.RANDOM_SINGLE; return end() }
        fun withMultipleResult(): TaskBuilder { this.resultType = ResultType.MULTIPLE; return end() }
        fun withShuffledMultipleResult(maxAmount: Int): TaskBuilder { this.resultType = ResultType.RANDOM_AMOUNTED_MULTIPLE; this.resultAmount = maxAmount; return end() }

        override fun type(): SpecType = SpecType.SELECT
        override fun end(): TaskBuilder { this.parent.addInLine(this); return parent }
        override fun params(): Map<String, Any> = mapOf("range" to range, "type" to resultType.name, "amount" to resultAmount)

        override fun compile(): CompiledBlockSelector {
            val selFn: (SelectorContext) -> AmountableResult<PlatformBlock<*>> = { ctx ->
                val selector = BlockInRadiusSelector(
                    range,
                    filtersRef.map { ref ->
                        val compiled = GlobalSpecRegistry.compileFilter(ref)
                        BlockFilterSpec(ref.id) { block, ctx, _ -> compiled.test(block, ctx) }
                    }
                )
                val result = selector.get(ctx)
                result
            }
            return CompiledBlockSelector(id, selFn)
        }
        override fun id(): ResourceLocation = id
        override fun resultType(): Class<PlatformBlock<*>> = PlatformBlock::class.java
    }
}

/**
 * A compiled task: runtime-only, produced by TaskBuilder.build().
 * This is what the scheduler will execute each tick.
 */
data class CompiledTask(
    val id: ResourceLocation,
    val type: TaskType,
    val lifecycle: TaskLifecycle,
    val priority: Int,
    val chance: Double,
    val steps: List<CompiledStep>,
    val cooldownTicks: Int = -1,
    val completionPredicate: (PlatformEntity, EntityTaskState) -> Boolean,
    val waitForSignals: Set<String>,
    val allSignalsRequired: Boolean,
    val stopSignals: Set<String>,
    val allStopSignalsRequired: Boolean
)