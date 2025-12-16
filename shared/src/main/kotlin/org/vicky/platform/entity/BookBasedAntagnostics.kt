package org.vicky.platform.entity

import org.vicky.platform.utils.ResourceLocation
import org.vicky.platform.world.PlatformBlock
import org.vicky.platform.world.PlatformWorld
import org.vicky.utilities.ContextLogger.AsyncContextLogger
import org.vicky.utilities.ContextLogger.ContextLogger
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.random.Random

// ---------------------- runtime data / timed actions -----------------------

data class ActiveTimedAction(
    val compiled: CompiledTimedAction,
    val self: PlatformLivingEntity,
    val targetEntity: PlatformLivingEntity? = null,
    var ticksLeft: Int,                // -1 = infinite
    val interval: Int = 1,
    var intervalTicksLeft: Int = 0,
    val slot: String? = null
)
data class ActiveTimedBlockAction(
    val compiled: CompiledBlockTimedAction,
    val self: PlatformLivingEntity,
    val targetBlock: PlatformBlock<*>? = null,
    var ticksLeft: Int,                // -1 = infinite
    val interval: Int = 1,
    var intervalTicksLeft: Int = 0,
    val slot: String? = null
)

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

data class CompiledBlockTimedAction(
    val id: ResourceLocation,
    val defaultDuration: Int,
    val defaultInterval: Int,
    val onStart: (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean,
    val onTick: (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean,
    val onEnd: (PlatformLivingEntity, PlatformBlock<*>?) -> Unit
)

data class CompiledTimedAction(
    val id: ResourceLocation,
    val defaultDuration: Int,
    val defaultInterval: Int,
    val onStart: (PlatformLivingEntity, PlatformLivingEntity?) -> Boolean,
    val onTick: (PlatformLivingEntity, PlatformLivingEntity?) -> Boolean,
    val onEnd: (PlatformLivingEntity, PlatformLivingEntity?) -> Unit
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
            priority = dto.priority,
            steps = compiledSteps,
            cooldownTicks = dto.cooldownTicks,
            chance = dto.chance
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
        GlobalSpecRegistry.registerFilter(id, object : FilterFactory<PlatformEntity> {
            override fun compile(ref: FilterRef<PlatformEntity>): CompiledFilter<PlatformEntity> = CompiledFilter(id) { e, ctx -> lambda(e, ctx) }
        })
        return FilterRef(id)
    }
    return FilterRef(possibleId)
}

fun registerInlineFilter(possibleId: ResourceLocation, lambda: (PlatformBlock<*>, SelectorContext) -> Boolean, internal_marker: Int = -225): FilterRef<PlatformBlock<*>> {
    if (!GlobalSpecRegistry.hasFilterFactory(possibleId)) {
        val id = InlineIdGen.next("filter_block")
        GlobalSpecRegistry.registerFilter(id, object : FilterFactory<PlatformBlock<*>> {
            override fun compile(ref: FilterRef<PlatformBlock<*>>): CompiledFilter<PlatformBlock<*>> = CompiledFilter(id) { b, ctx -> lambda(b, ctx) }
        })
        return FilterRef(id)
    }
    return FilterRef(possibleId)
}

fun registerInlineAction(possibleId: ResourceLocation, lambda: (PlatformLivingEntity, PlatformLivingEntity?) -> Boolean): ActionRef {
    if (!GlobalSpecRegistry.hasActionFactory(possibleId)) {
        val id = InlineIdGen.next("action_entity")
        GlobalSpecRegistry.registerAction(id) { ref -> CompiledEntityAction(id, lambda) }
        return ActionRef(id)
    }
    return ActionRef(possibleId)
}

fun registerInlineBlockAction(possibleId: ResourceLocation, lambda: (PlatformLivingEntity, PlatformBlock<*>) -> Boolean, internal_marker: Int = -225): BlockActionRef {
    if (!GlobalSpecRegistry.hasBlockActionFactory(possibleId)) {
        val id = InlineIdGen.next("action_block")
        GlobalSpecRegistry.registerBlockAction(id, { ref -> CompiledBlockAction(id, lambda) })
        return BlockActionRef(id)
    }
    return BlockActionRef(possibleId)
}

fun registerInlineCondition(possibleId: ResourceLocation, lambda: (PlatformLivingEntity) -> Boolean, mustBeTrue: Boolean = true): ConditionRef {
    if (!GlobalSpecRegistry.hasConditionFactory(possibleId)) {
        val id = InlineIdGen.next("condition_entity")
        GlobalSpecRegistry.registerCondition(id, { ref -> CompiledCondition(id, { ent -> lambda(ent) }, mustBeTrue) })
        return ConditionRef(id, emptyMap(), mustBeTrue)
    }
    return ConditionRef(possibleId, emptyMap(), mustBeTrue)
}

fun registerInlineBlockCondition(possibleId: ResourceLocation, lambda: (PlatformBlock<*>) -> Boolean, mustBeTrue: Boolean = true, internal_marker: Int = -225): BlockConditionRef {
    if (!GlobalSpecRegistry.hasBlockConditionFactory(possibleId)) {
        val id = InlineIdGen.next("condition_block")
        GlobalSpecRegistry.registerBlockCondition(id, { ref -> CompiledBlockCondition(id, { b -> lambda(b) }, mustBeTrue) })
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
    SINGLE, MULTIPLE, RANDOM_SINGLE, RANDOM_AMOUNTED_MULTIPLE, AMOUNTED_MULTIPLE
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

                val yaw = Math.toDegrees(kotlin.math.atan2(-dx, dz)).toFloat()
                val pitch = Math.toDegrees(-kotlin.math.atan2(dy, distXZ)).toFloat()

                self.setRotation(yaw, pitch)
                return@lambda true
            }
        }
        else return@lambda false
    },
    { self, _ -> self.setRotation(0f, 0f) }
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

                val yaw = Math.toDegrees(kotlin.math.atan2(-dx, dz)).toFloat()
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
    val onStart: (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean,
    val onTick: (PlatformLivingEntity, PlatformBlock<*>?) -> Boolean,
    val onEnd: (PlatformLivingEntity, PlatformBlock<*>?) -> Unit
) {
    init {
        if (!GlobalSpecRegistry.hasBlockTimedActionFactory(id)) {
            GlobalSpecRegistry.registerBlockTimedAction(id, { ref ->
                // ref.duration / ref.interval will be applied by compiled action (same pattern as entity version)
                CompiledBlockTimedAction(id, ref.duration ?: 40, ref.interval ?: 1, onStart, onTick, onEnd)
            })
        }
    }

    fun id(): ResourceLocation = id
}

object WalkToBlock : BlockTimedActionSpec(
    rl("core", "walk_to_block"),
    onStart = start@{ self, block ->
        if (block == null) return@start false
        self.getNavigator()?.let {
            val path = it.createPath(block.blockPos, self.lookDistance.toInt()) ?: return@start false
            it.moveTo(path, self.getSpeed().toDouble())
            return@start true
        }
        false
    },
    onTick = onTick@{ self, _ ->
        val nav = self.getNavigator() ?: return@onTick false
        nav.tick()
        !nav.isDone()
    },
    onEnd = { self, _ ->
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

open class BlockFilterSpec(val id: ResourceLocation, private val validator: (PlatformBlock<*>, SelectorContext) -> Boolean)
    : EntityObjectableFilterSpec<PlatformBlock<*>> {
    init {
        if (!GlobalSpecRegistry.hasFilterFactory(id))
            GlobalSpecRegistry.registerFilter(id,
                FilterFactory<PlatformBlock<*>> { ref -> CompiledFilter(ref.id) { b, ctx -> validator(b, ctx) } })
    }
    override fun id(): ResourceLocation = id
    override fun isValid(obj: PlatformBlock<*>, ctx: SelectorContext): Boolean = validator(obj, ctx)
    override fun validator(): (PlatformBlock<*>, SelectorContext) -> Boolean = validator
}

open class EntityActionSpec<T : PlatformEntity>(val id: ResourceLocation, val runner: (PlatformEntity, PlatformEntity?) -> Boolean) : ActionSpec<T, T?> {
    init {
        if (!GlobalSpecRegistry.hasActionFactory(id))
            GlobalSpecRegistry.registerAction(id, { ref -> CompiledEntityAction(ref.id, runner) })
    }
    override fun id(): ResourceLocation = id
    override fun perform(self: T, target: T?): Boolean = runner(self as PlatformEntity, target)
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

class BlockInSightSelector(
    range: Double,
    private val step: Double = 0.5,
    filters: List<BlockFilterSpec>
) : SelectorSpec<PlatformBlock<*>, BlockFilterSpec>(
    id = rl("core", "block_in_sight"),
    resultType = ResultType.SINGLE,
    range = range,
    filters = filters
) {
    override fun findCandidates(ctx: SelectorContext): List<PlatformBlock<*>> {
        val self = ctx.self ?: return emptyList()
        val world = ctx.world
        // prefer engine raycast
        val hit = try {
            world.raycastBlock(self.getEyeLocation(), self.lookDirection.dir, range.toFloat())
        } catch (ex: Throwable) {
            sampleAlongLook(self, world, range, step)
        }
        return if (hit != null) listOf(hit) else emptyList()
    }

    private fun sampleAlongLook(self: PlatformEntity, world: PlatformWorld<*, *>, range: Double, step: Double): PlatformBlock<*>? {
        val origin = self.getEyeLocation()
        val dir = self.lookDirection.dir.normalize()
        val steps = 1.coerceAtLeast((range / step).toInt())
        var i = 1
        while (i <= steps) {
            val dist = i * step
            val x = origin.x + dir.x * dist
            val y = origin.y + dir.y * dist
            val z = origin.z + dir.z * dist
            val block = try { world.getBlockAt(x, y, z) } catch (e: Throwable) { null }
            if (block != null && block.isSolid) {
                val ctx = SelectorContext(world, origin.x, origin.y, origin.z, self)
                val passes = filters.all { it.isValid(block, ctx) }
                if (passes) return block
            }
            i++
        }
        return null
    }
}

// --------------------- Block Filters ------------------------
object BlockIsWalkableFilter : BlockFilterSpec(
    rl("core", "block_is_walkable_filter"),
    { block, ctx -> block.material.isSolid && !block.material.isAir }
)
object BlockIsHighest : BlockFilterSpec(
    rl("core", "block_is_walkable_filter"),
    { block, ctx -> ctx.world.getHighestBlockYAt(block.location.x, block.location.z) == block.location.y.toInt() }
)

// --------------------- Amountable result ---------------------

data class AmountableResult<T>(val resultType: ResultType, private val result: List<T>) {
    fun getSingleResult(): T? {
        return when (resultType) {
            ResultType.SINGLE, ResultType.RANDOM_SINGLE -> result.firstOrNull()
            else -> throw IllegalStateException("Trying to access single result from multiple-result container.")
        }
    }
    fun getResults(): List<T> {
        return when (resultType) {
            ResultType.MULTIPLE, ResultType.AMOUNTED_MULTIPLE, ResultType.RANDOM_AMOUNTED_MULTIPLE -> result
            else -> throw IllegalStateException("Trying to access multiple results from single-result container.")
        }
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
    val priority: Int = 0,
    val chance: Double = 0.5,
    val steps: List<StepDTO> = emptyList(),
    val cooldownTicks: Int = -1
)

enum class TaskType { CONDITIONED, RANDOM }

// --------------------- TaskBuilder with entity / block modes ---------------------

class TaskBuilder private constructor(val ordinal: PlatformLivingEntity, val id: ResourceLocation, val type: TaskType, val priority: Int, val chance: Double = 0.5) {
    private val steps = mutableListOf<ParentableTaskStep>()
    private var cooldownTicks: Int = -1
    private var LOGGER: AsyncContextLogger = AsyncContextLogger(ContextLogger.ContextType.SUB_SYSTEM, "TASK-RUNNER-[$id]")

    private enum class Mode { ENTITY, BLOCK }
    private var mode: Mode = Mode.ENTITY

    companion object {
        private fun of(ordinal: PlatformLivingEntity, id: ResourceLocation, type: TaskType, priority: Int, chance: Double = 0.5): TaskBuilder =
            TaskBuilder(ordinal, id, type, priority, chance)
        @JvmOverloads
        fun random(ordinal: PlatformLivingEntity, id: ResourceLocation, priority: Int = 0, chance: Double = 0.5) =
            of(ordinal, id, TaskType.RANDOM, priority, chance)
        @JvmOverloads
        fun conditioned(ordinal: PlatformLivingEntity, id: ResourceLocation, priority: Int = 0) =
            of(ordinal, id, TaskType.CONDITIONED, priority)
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

        return CompiledTask(id = this.id, type = this.type, priority = this.priority, steps = compiledSteps.toList(), chance = this.chance, cooldownTicks = this.cooldownTicks)
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

        return TaskSpecDTO(id = this.id.toString(), type = this.type, priority = this.priority, steps = stepDtos, cooldownTicks = this.cooldownTicks)
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
                start = { s, b -> action.onStart(s, b) },
                tick  = { s, b -> action.onTick(s, b) },
                end   = { s, b -> action.onEnd(s, b) },
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
        var resultType: ResultType = ResultType.SINGLE
        var resultAmount: Int = -1

        fun filter(filter: EntityObjectableFilterSpec<PlatformEntity>): EntitySelectionTaskStep {
            val ref = registerInlineFilter(filter.id(), filter.validator())
            filtersRef += ref
            return this
        }
        fun filterRef(ref: FilterRef<PlatformEntity>): EntitySelectionTaskStep { filtersRef += ref; return this }

        fun withSingleResult(): TaskBuilder { this.resultType = ResultType.SINGLE; return end() }
        fun withRandomSingleResult(): TaskBuilder { this.resultType = ResultType.RANDOM_SINGLE; return end() }
        fun withMultipleResult(): TaskBuilder { this.resultType = ResultType.MULTIPLE; return end() }
        fun withRandomMultipleResult(maxAmount: Int): TaskBuilder { this.resultType = ResultType.RANDOM_AMOUNTED_MULTIPLE; this.resultAmount = maxAmount; return end() }

        override fun type(): SpecType = SpecType.SELECT
        override fun end(): TaskBuilder { this.parent.addInLine(this); return parent }
        override fun params(): Map<String, Any> = mapOf("range" to range, "type" to resultType.name, "amount" to resultAmount)

        override fun compile(): CompiledEntitySelector {
            val compiledFilters = filtersRef.map { GlobalSpecRegistry.compileFilter(it) }
            val selFn: (SelectorContext) -> AmountableResult<PlatformLivingEntity> = { ctx ->
                val base = ctx.world.getLivingEntitiesWithin(ctx.originX, ctx.originY, ctx.originZ, range.toFloat())
                val filtered = base.filter { candidate -> compiledFilters.all { it.test(candidate, ctx) } }
                when (resultType) {
                    ResultType.SINGLE -> AmountableResult(ResultType.SINGLE, filtered.take(1))
                    ResultType.MULTIPLE -> AmountableResult(ResultType.MULTIPLE, filtered)
                    ResultType.RANDOM_SINGLE -> if (filtered.isEmpty()) AmountableResult(ResultType.RANDOM_SINGLE, emptyList()) else AmountableResult(ResultType.RANDOM_SINGLE, listOf(filtered[Random.nextInt(filtered.size)]))
                    ResultType.RANDOM_AMOUNTED_MULTIPLE -> AmountableResult(ResultType.RANDOM_AMOUNTED_MULTIPLE, filtered.shuffled().take(maxOf(0, resultAmount)))
                    ResultType.AMOUNTED_MULTIPLE -> AmountableResult(ResultType.AMOUNTED_MULTIPLE, filtered.sortedBy { e ->
                        val dx = e.location.x - ctx.originX; val dy = e.location.y - ctx.originY; val dz = e.location.z - ctx.originZ
                        dx * dx + dy * dy + dz * dz
                    }.take(maxOf(0, resultAmount)))
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

        fun withSingleResult(): TaskBuilder { this.resultType = ResultType.SINGLE; return end() }
        fun withRandomSingleResult(): TaskBuilder { this.resultType = ResultType.RANDOM_SINGLE; return end() }
        fun withMultipleResult(): TaskBuilder { this.resultType = ResultType.MULTIPLE; return end() }
        fun withRandomMultipleResult(maxAmount: Int): TaskBuilder { this.resultType = ResultType.RANDOM_AMOUNTED_MULTIPLE; this.resultAmount = maxAmount; return end() }

        override fun type(): SpecType = SpecType.SELECT
        override fun end(): TaskBuilder { this.parent.addInLine(this); return parent }
        override fun params(): Map<String, Any> = mapOf("range" to range, "type" to resultType.name, "amount" to resultAmount)

        override fun compile(): CompiledBlockSelector {
            val selFn: (SelectorContext) -> AmountableResult<PlatformBlock<*>> = { ctx ->
                val selector = BlockInSightSelector(
                    range, 0.5,
                    filtersRef.map { ref ->
                        val compiled = GlobalSpecRegistry.compileFilter(ref)
                        BlockFilterSpec(ref.id) { block, ctx -> compiled.test(block, ctx) }
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
    val priority: Int,
    val chance: Double,
    val steps: List<CompiledStep>,
    val cooldownTicks: Int = -1
)