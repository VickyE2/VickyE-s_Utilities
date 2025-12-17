package org.vicky.platform.entity

import de.pauleff.core.Tag
import org.vicky.platform.PlatformItem
import org.vicky.platform.PlatformPlayer
import org.vicky.platform.defaults.AABB
import org.vicky.platform.utils.Direction
import org.vicky.platform.utils.IntVec3
import org.vicky.platform.utils.ResourceLocation
import org.vicky.platform.utils.SoundCategory
import org.vicky.platform.utils.Vec3
import org.vicky.platform.world.PlatformLocation
import org.vicky.platform.world.PlatformWorld
import java.util.*

class ErrorOnMobProductionException : RuntimeException {
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}

interface PlatformEntityFactory {
    class RegisteredMobEntityEventHandler internal constructor(
        val handler: MobEntityEventHandler
    )

    fun spawnArrowAt(loc: PlatformLocation?): PlatformEntity?
    fun registerHandler(id: ResourceLocation, handler: MobEntityEventHandler): RegisteredMobEntityEventHandler
    fun getHandler(id: ResourceLocation): RegisteredMobEntityEventHandler?

    @Throws(ErrorOnMobProductionException::class)
    fun register(defaults: MobEntityDescriptor)
    fun spawn(world: PlatformWorld<*, *>, id: ResourceLocation, x: Double, y: Double, z: Double): PlatformLivingEntity
}

enum class DamageType {
    ARROW, BAD_RESPAWN_POINT, CACTUS, CAMPFIRE, CRAMMING, DRAGON_BREADTH, DROWN, DRY_OUT, ENDER_PEARL, EXPLOSION, FALL, FALLING_ANVIL,
    FALLING_BLOCK, FALLING_STALACTITE, FIREBALL, FIREWORKS, FLY_INTO_WALL, FREEZE, GENERIC, GENERIC_KILL, HOT_FLOOR, IN_FIRE, IN_WALL,
    INDIRECT_MAGIC, LAVA, LIGHTNING_BOLT, MACE_SMASH, MAGIC, MOB_ATTACK, MOB_ATTACK_NO_AGRO, MOB_PROJECTILE, ON_FIRE, OUT_OF_WORLD,
    OUTSIDE_BORDER, PLAYER_ATTACK, PLAYER_EXPLOSION, SONIC_BOOM, SPEAR, SPIT, STALAGMITE, STARVE, STING, SWEET_BERRY_BUSH, THORNS, THROWN,
    TRIDENT, UNATTRIBUTED_FIREBALL, WIND_CHARGE, WITHER, WITHER_SKULL
}

enum class MobCategory {
    NONE, MONSTER, CREATURE, AMBIENT, MISC
}

enum class SpawnCategory {
    CREATURE, MONSTER, WATER, AMBIENT, UNDERGROUND
}

enum class SpawnHeight {
    ON_GROUND,
    IN_AIR,
    UNDERGROUND,
    IN_WATER
}

enum class EquipmentSlot {
    BODY, CHEST, FEET, HAND, HEAD, LEGS, OFF_HAND, SADDLE
}

data class SoundId(val namespace: String, val path: String) {
    override fun toString() = "$namespace:$path"
}

data class MobSounds(
    val ambient: SoundDefinition? = null,
    val hurt: SoundDefinition? = null,
    val death: SoundDefinition? = null,
    val step: SoundDefinition? = null,
    val fall: SoundDefinition? = null,
    val attack: SoundDefinition? = null,
    val shoot: SoundDefinition? = null,
    val swim: SoundDefinition? = null,
    val flap: SoundDefinition? = null,
    val custom: Map<String, SoundDefinition> = emptyMap() // roar, cast-spell, etc.
)

data class AnimationDefinition(
    val idle: ResourceLocation,
    val walk: ResourceLocation,
    val hurt: ResourceLocation? = null,
    val step: ResourceLocation? = null,
    val fall: ResourceLocation? = null,
    val attack: ResourceLocation? = null,
    val shoot: ResourceLocation? = null,
    val swim: ResourceLocation? = null,
    val flap: ResourceLocation? = null,
    val custom: Map<String, ResourceLocation> = emptyMap()
)

data class SoundDefinition(
    val sound: SoundId,
    val volume: Float = 1f,
    val pitch: Float = 1f,
    val category: SoundCategory = SoundCategory.NEUTRAL
)

data class AntagonisticDamageSource(
    val causingEntity: PlatformEntity?,
    val directEntity: PlatformEntity?,
    val damageLocation: PlatformLocation,
    val sourceLocation: PlatformLocation,
    val isIndirect: Boolean,
    val scalesWithDifficulty: Boolean,
    val damageType: DamageType
)

interface PlatformEntity {
    val uuid: UUID
    val typeId: ResourceLocation

    val world: PlatformWorld<*, *>
    val location: PlatformLocation
    fun teleport(location: PlatformLocation)

    fun remove()
    val isDead: Boolean

    var velocity: Vec3?

    fun setRotation(yaw: Float, pitch: Float)
    val yaw: Float
    val pitch: Float
    val eyeHeight: Float
    val lookDirection: Direction
    fun getEyeLocation(): Vec3

    fun setGravity(enabled: Boolean)
    fun setInvisible(invisible: Boolean)
    fun setInvulnerable(invulnerable: Boolean)

    fun setCustomName(name: String)
    val customName: Optional<String>
    fun <T> setPersistentData(key: String, value: Tag<T>)
    fun getPersistentData(key: String): Tag<*>?

    fun interact(interacter: PlatformPlayer)

    var boundingBox: AABB
    val handle: Any
    val isPlayer: Boolean
}

interface PlatformLivingEntity : PlatformEntity {
    var health: Float
    var absorption: Float
    fun getMaxHealth(): Float
    fun getMaxAbsorption(): Float
    var lookDistance: Double

    fun hurt(amount: Float, source: AntagonisticDamageSource)
    fun die(source: AntagonisticDamageSource)
    fun heal(amount: Float)

    fun hasLineOfSight(target: PlatformEntity): Boolean
    fun setAttributeBaseValue(key: String, value: Double)
    fun getAttributeBaseValue(key: String): Double?
    fun getAttributeValue(key: String): Double?
    fun getAttributes(): Map<String, Double>

    fun increaseAirSupply(value: Int)
    fun decreaseAirSupply(value: Int)
    var airSupply: Int

    fun setSpeed(value: Float)
    fun getSpeed(): Float

    val isOnGround: Boolean
    val isInWater: Boolean

    fun getNavigator(): PathNavigator?

    fun getLastAttacker(): PlatformLivingEntity?
    fun getLastHurtByMob(): PlatformLivingEntity?
    fun setLastHurtByMob(mob: PlatformLivingEntity?)
    fun setLastHurtByPlayer(mob: PlatformPlayer?)
    fun getLastHurtMob(): PlatformLivingEntity?
    fun setLastHurtMob(mob: PlatformLivingEntity?)

    fun getOffhandItem(): PlatformItem?
    fun getMainHandItem(): PlatformItem?
    fun setItemSlot(slot: EquipmentSlot, item: PlatformItem)
    fun getItemBySlot(slot: EquipmentSlot): PlatformItem?
    fun hasItemInSlot(slot: EquipmentSlot): Boolean
    fun isHolding(item: PlatformItem): Boolean

    val isAffectedByPotions: Boolean
    val isCurrentlyGlowing: Boolean
    val isPushable: Boolean
    val isPickable: Boolean
    val isSensitiveToWater: Boolean
    val isInvertedHealAndHarm: Boolean
    val isBaby: Boolean
    val isOnFire: Boolean
    val isSprinting: Boolean
    val isSneaking: Boolean

    val shouldDropExperience: Boolean

    val canBreatheUnderwater: Boolean
    val canDisableShield: Boolean
    val canChangeDimensions: Boolean
    val canBeSeenByAnyone: Boolean
    val canFreeze: Boolean
}

data class MobEntityDescriptor(
    val dataMap: Map<String, Any>,
    val mobDetails: MobDefaults,
    val physicalProps: MobEntityPhysicalProperties,
    val ai: MobEntityAIBasedGoals,
    val eventHandler: PlatformEntityFactory.RegisteredMobEntityEventHandler
)

class MobDefaults(
    val mobKey: ResourceLocation,                        // unique ID
    val displayName: String,                   // name shown to players
    val category: MobCategory = MobCategory.NONE,

    // --- Model / Appearance ---
    val modelId: ResourceLocation? = null,               // e.g., GeckoLib model, ItemsAdder model, etc.
    val scale: Double = 1.0,
    val baby: Boolean = false,

    // --- Stats ---
    val maxHealth: Double = 20.0,
    val maxAbsorption: Double = 0.0,
    val baseArmor: Double = 0.0,
    val baseArmorToughness: Double = 0.0,
    val knockbackResistance: Double = 0.0,
    val movementSpeed: Double = 0.25,
    val swimSpeed: Double = 1.0,
    val flySpeed: Double = 0.8,
    val jumpStrength: Double = 0.8,
    val attackDamage: Double = 2.0,
    val attackSpeed: Double = 0.7,
    val attackKnockback: Double = 1.0,
    val followRange: Double = 16.0,
    val luck: Double = 0.0,

    // --- Combat Behavior ---
    val aggressive: Boolean = true,
    val canDrown: Boolean = true,
    val canBurn: Boolean = true,
    val canRespawn: Boolean = false,
    val allowFriendlyFire: Boolean = true,

    // --- Dimensions ---
    val boundingBox: AABB = AABB(0.6, 0.6, 1.95),

    // --- Sounds ---
    val sounds: MobSounds = MobSounds(),

    // --- Drops ---
    val drops: List<DropEntry> = emptyList(),

    // --- Boss Settings ---
    // val boss: BossSettings? = null,

    // --- Spawn Settings (optional) ---
    val spawn: MobSpawnSettings? = null,

    // --- State Machine / Animations ---
    val animations: AnimationDefinition,

    // --- Misc flags ---
    val persistent: Boolean = true,
    val takesFallDamage: Boolean = true,
    val isImmuneToFire: Boolean = false,
    val isImmuneToFreeze: Boolean = false,
    val waterCreature: Boolean = false,
    val flyingCreature: Boolean = false,

    // --- Extra metadata for plugins ---
    val metadata: Map<String, Any> = emptyMap()
)

data class MobSpawnSettings(
    val mobId: ResourceLocation,
    val category: SpawnCategory = SpawnCategory.CREATURE,
    val weight: Int = 10,
    val minGroupSize: Int = 1,
    val maxGroupSize: Int = 4,

    val conditions: List<SpawnCondition> = emptyList(),
    val modifiers: List<SpawnModifier> = emptyList(),
    val tags: Set<String> = emptySet(),

    val spawnHeight: SpawnHeight = SpawnHeight.ON_GROUND,
    val lightLevel: IntRange = 0..15,
    val allowedBiomes: Set<String> = emptySet(),   // platform-agnostic biome identifiers
    val prohibitedBiomes: Set<String> = emptySet(),

    val maxPerChunk: Int = 8,
    val maxGlobal: Int = 200,
)

fun interface SpawnCondition {
    fun canSpawn(ctx: SpawnContext): Boolean
}

fun interface SpawnModifier {
    fun apply(entity: PlatformLivingEntity, ctx: SpawnContext)
}

data class SpawnContext(
    val x: Double,
    val y: Double,
    val z: Double,
    val biome: String,
    val lightLevel: Int,
    val worldTime: Long,
    val platformWorld: PlatformWorld<*, *>,   // raw handle if needed
)


data class DropEntry(
    val weight: Int,
    val items: List<PlatformItem>
)

data class MobEntityPhysicalProperties(
    val hitBox: AABB,
    val eggColors: Pair<Int, Int>,
    val eyeHeight: Float,
    val isFireImmune: Boolean,
    val isPushable: Boolean,
    val noGravity: Boolean,

)

data class MobEntityAIBasedGoals(
    val goals: Map<ProducerIntendedTask, Map<String, Any>> = mapOf()
)

enum class EventResult {
    PASS,      // let other handlers + default behavior proceed
    CONSUME,   // stop further handlers and stop default behavior
    CANCEL     // stop further handlers but still allow some fallback (optional semantic)
}

interface AbstractPath {
    fun getCurrent(): AbstractPathNode?
    fun getEnd(): AbstractPathNode?
    fun isFinished(): Boolean
    fun advance()
    fun length(): Int
    fun target(): IntVec3
}

interface AbstractPathNode {
    val x: Int
    val y: Int
    val z: Int

    fun distanceTo(other: AbstractPathNode?): Float
    fun asVec(): IntVec3 = IntVec3.of(x, y, z)
}

interface PathNavigator {
    // Returns true if there's a path to follow
    fun canUpdatePath(): Boolean

    // Sets the speed at which the entity moves along the path
    fun setSpeed(speed: Double)

    // Returns the current path object
    fun getPath(): AbstractPath?

    // Checks if the entity has completed its path
    fun isDone(): Boolean

    // Checks if the entity is still navigating
    fun isInProgress(): Boolean

    // Moves the entity along its current path
    fun tick()

    fun getTargetPos(): IntVec3?

    // Creates a path to an entity
    fun createPath(targetEntity: PlatformEntity, distance: Int): AbstractPath?

    // Creates a path to a specified coordinate
    fun createPath(targetEntity: IntVec3, distance: Int): AbstractPath?

    // Creates a path to the list of positions
    fun createPath(positions: Set<IntVec3>, distance: Int): AbstractPath?

    // Starts following the given path
    fun moveTo(path: AbstractPath, speed: Double)

    // Starts following the given path
    fun moveTo(path: PlatformEntity?, speed: Double)

    // Clears the current path
    fun stop()

    // Returns whether the pathfinding has been stuck
    fun isStuck(): Boolean

    // Sets pathfinding options such as avoiding water or roads
    var canFloat: Boolean
}

interface MobEntityEventHandler {
    fun onEnterCombat(self: PlatformLivingEntity) { }
    fun onLeaveCombat(self: PlatformLivingEntity) { }
    fun onTick(self: PlatformLivingEntity): EventResult = EventResult.PASS
    fun onSpawn(self: PlatformLivingEntity): EventResult = EventResult.PASS
    fun onInteract(self: PlatformLivingEntity, interacter: PlatformLivingEntity): EventResult = EventResult.PASS
    fun onAttacked(self: PlatformLivingEntity, attacker: PlatformLivingEntity): EventResult = EventResult.PASS
    fun onAttack(self: PlatformLivingEntity, victim: PlatformLivingEntity): EventResult = EventResult.PASS
    fun onApplyPotion(self: PlatformLivingEntity, effect: RegisteredUniversalEffect): EventResult = EventResult.PASS
    fun onHurt(self: PlatformLivingEntity, source: AntagonisticDamageSource, amount: Float): EventResult = EventResult.PASS
    fun onDeath(self: PlatformLivingEntity, source: AntagonisticDamageSource): EventResult = EventResult.PASS
}

fun mob(
    key: ResourceLocation,
    handler: PlatformEntityFactory.RegisteredMobEntityEventHandler,
    block: MobEntityDescriptorBuilder.() -> Unit
): MobEntityDescriptor {
    return MobEntityDescriptorBuilder(key, handler).apply(block).build()
}

class MobEntityDescriptorBuilder(
    private val mobKey: ResourceLocation,
    private val handler: PlatformEntityFactory.RegisteredMobEntityEventHandler
) {
    private val dataMap = mutableMapOf<String, Any>()

    private var defaults: MobDefaults? = null
    private var physical = MobEntityPhysicalProperties(
        AABB(0.6, 0.6, 1.95),
        0xFFFFFF to 0xAAAAAA,
        0.3f,
        false,
        true,
        false
    )
    private val goals = mutableMapOf<ProducerIntendedTask, Map<String, Any>>()

    fun metadata(key: String, value: Any) {
        dataMap[key] = value
    }

    fun defaults(displayName: String, block: MobDefaultsBuilder.() -> Unit) {
        defaults = MobDefaultsBuilder(mobKey, displayName).apply(block).build()
    }

    fun physical(block: PhysicalBuilder.() -> Unit) {
        physical = PhysicalBuilder().apply(block).build()
    }

    fun ai(block: AIGoalsBuilder.() -> Unit) {
        goals += AIGoalsBuilder().apply(block).build()
    }

    fun build(): MobEntityDescriptor {
        val defs = defaults ?: error("MobDefaults must be defined for mob '$mobKey'")
        return MobEntityDescriptor(
            dataMap = dataMap,
            mobDetails = defs,
            physicalProps = physical,
            ai = MobEntityAIBasedGoals(goals),
            eventHandler = handler
        )
    }
}

class PhysicalBuilder {
    private var hitBox: AABB = AABB(0.6, 0.6, 1.95)
    var eggColors: Pair<Int, Int> = 0xFFFFFF to 0xAAAAAA;
    var eyeHeight: Float  = 0.3f;
    var isFireImmune: Boolean = false;
    var isPushable: Boolean = true;
    var noGravity: Boolean = false;

    fun hitBox(width: Double, depth: Double, height: Double) {
        hitBox = AABB(width, depth, height)
    }

    fun hitBox(box: AABB) {
        hitBox = box
    }

    fun build(): MobEntityPhysicalProperties =
        MobEntityPhysicalProperties(hitBox, eggColors, eyeHeight, isFireImmune, isPushable, noGravity)
}


class MobDefaultsBuilder(
    private val mobKey: ResourceLocation,
    private val displayName: String
) {
    var category: MobCategory = MobCategory.NONE

    var modelId: ResourceLocation? = null
    var scale: Double = 1.0
    var baby: Boolean = false

    var maxHealth: Double = 20.0
    var armor: Double = 0.0
    var movementSpeed: Double = 0.25
    var attackDamage: Double = 2.0
    var followRange: Double = 16.0

    var aggressive: Boolean = false
    var persistent: Boolean = false
    var immuneToFire: Boolean = false
    var immuneToFreeze: Boolean = false

    private var sounds: MobSounds = MobSounds()
    private var animations: AnimationDefinition? = null
    private var spawn: MobSpawnSettings? = null
    private val drops = mutableListOf<DropEntry>()
    private val metadata = mutableMapOf<String, Any>()

    fun sounds(block: MobSoundsBuilder.() -> Unit) {
        sounds = MobSoundsBuilder().apply(block).build()
    }

    fun animations(
        idle: ResourceLocation,
        walk: ResourceLocation,
        block: AnimationBuilder.() -> Unit = {}
    ) {
        animations = AnimationBuilder(idle, walk).apply(block).build()
    }

    fun spawn(block: SpawnSettingsBuilder.() -> Unit) {
        spawn = SpawnSettingsBuilder(mobKey).apply(block).build()
    }

    fun drop(weight: Int, vararg items: PlatformItem) {
        drops += DropEntry(weight, items.toList())
    }

    fun meta(key: String, value: Any) {
        metadata[key] = value
    }

    fun build(): MobDefaults =
        MobDefaults(
            mobKey = mobKey,
            displayName = displayName,
            category = category,
            modelId = modelId,
            scale = scale,
            baby = baby,
            maxHealth = maxHealth,
            baseArmor = armor,
            movementSpeed = movementSpeed,
            attackDamage = attackDamage,
            followRange = followRange,
            aggressive = aggressive,
            persistent = persistent,
            isImmuneToFire = immuneToFire,
            isImmuneToFreeze = immuneToFreeze,
            sounds = sounds,
            animations = animations
                ?: error("Animations must be defined for mob '$mobKey'"),
            spawn = spawn,
            drops = drops,
            metadata = metadata
        )
}

class MobSoundsBuilder {
    var ambient: SoundDefinition? = null
    var hurt: SoundDefinition? = null
    var death: SoundDefinition? = null
    private val custom = mutableMapOf<String, SoundDefinition>()

    fun custom(name: String, sound: SoundDefinition) {
        custom[name] = sound
    }

    fun build(): MobSounds =
        MobSounds(
            ambient = ambient,
            hurt = hurt,
            death = death,
            custom = custom
        )
}

class AnimationBuilder(
    private val idle: ResourceLocation,
    private val walk: ResourceLocation
) {
    var hurt: ResourceLocation? = null
    var attack: ResourceLocation? = null
    private val custom = mutableMapOf<String, ResourceLocation>()

    fun custom(name: String, anim: ResourceLocation) {
        custom[name] = anim
    }

    fun build(): AnimationDefinition =
        AnimationDefinition(
            idle = idle,
            walk = walk,
            hurt = hurt,
            attack = attack,
            custom = custom
        )
}

class SpawnSettingsBuilder(private val mobId: ResourceLocation) {
    var category: SpawnCategory = SpawnCategory.CREATURE
    var weight: Int = 10
    var groupSize: IntRange = 1..4
    var height: SpawnHeight = SpawnHeight.ON_GROUND
    var light: IntRange = 0..15

    private val conditions = mutableListOf<SpawnCondition>()
    private val modifiers = mutableListOf<SpawnModifier>()
    private val tags = mutableSetOf<String>()

    fun condition(cond: SpawnCondition) {
        conditions += cond
    }

    fun modifier(mod: SpawnModifier) {
        modifiers += mod
    }

    fun tag(tag: String) {
        tags += tag
    }

    fun build(): MobSpawnSettings =
        MobSpawnSettings(
            mobId = mobId,
            category = category,
            weight = weight,
            minGroupSize = groupSize.first,
            maxGroupSize = groupSize.last,
            spawnHeight = height,
            lightLevel = light,
            conditions = conditions,
            modifiers = modifiers,
            tags = tags
        )
}

class AIGoalsBuilder {
    private val goals = mutableMapOf<ProducerIntendedTask, Map<String, Any>>()

    fun goal(task: ProducerIntendedTask, params: Map<String, Any>) {
        goals[task] = params
    }

    fun build(): Map<ProducerIntendedTask, Map<String, Any>> = goals
}
