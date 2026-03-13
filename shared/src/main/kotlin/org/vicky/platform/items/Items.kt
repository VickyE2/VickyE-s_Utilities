package org.vicky.platform.items

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.vicky.platform.PlatformItemStack
import org.vicky.platform.PlatformPlayer
import org.vicky.platform.PlatformPlugin
import org.vicky.platform.entity.PlatformEffectInstance
import org.vicky.platform.entity.PlatformLivingEntity
import org.vicky.platform.entity.minecraft
import org.vicky.platform.entity.pair
import org.vicky.platform.entity.rli
import org.vicky.platform.utils.ResourceLocation
import org.vicky.platform.world.PlatformMaterial
import org.vicky.utilities.ContextLogger.ContextLogger
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

class ItemProductionError : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

class DescriptorNotRegisteredException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?): super(cause)
}

data class ItemDescriptor(
    val displayName: Component,
    val lore: List<Component> = emptyList(),
    val baseNbt: Map<String, Any> = emptyMap(),
    val handler: ItemEventsHandler = DefaultItemEventsHandler,
    val physicalProps: ItemPhysicalProperties = ItemPhysicalProperties(),
    val foodProps: FoodProperties? = null
)
data class FoodProperties(
    val nutrition: Int = 0,
    val saturationModifier: Float = 0f,
    val isMeat: Boolean = false,
    val canAlwaysEat: Boolean = false,
    val fastFood: Boolean = false,
    val effects: List<FoodEffect> = emptyList()
)
data class ItemPhysicalProperties(
    val glint: Boolean = false,
    /** This is mostly used on non modded platforms */
    val customModelData: Int? = null,
    /** This is mostly used on non modded platforms */
    val baseMaterial: ResourceLocation = "paper".minecraft(),
    val fireResistant: Boolean = false,
    val stackable: Boolean = false,
    val maxStackSize: Int = 64,
    val durability: Int? = null,
    val rarity: Rarity = Rarity.COMMON
)
data class FoodEffect(
    val effect: ResourceLocation,
    val duration: Int,
    val amplifier: Int = 0,
    val probability: Float = 1f
)
enum class Rarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC
}

class ItemPhysicalPropertiesBuilder {
    private var glint: Boolean = false
    private var customModelData: Int? = null
    private var baseMaterial: ResourceLocation = "paper".minecraft()
    private var fireResistant: Boolean = false
    private var stackable: Boolean = false
    private var maxStackSize: Int = if (stackable) 1 else 64
    private var durability: Int? = null
    private var rarity: Rarity = Rarity.COMMON

    fun customModelData(customModelData: Int) {
        this.customModelData = customModelData
    }

    fun rarity(rarity: Rarity) {
        this.rarity = rarity
    }

    fun maxStackSize(maxStackSize: Int) {
        this.maxStackSize = maxStackSize
    }

    fun durability(durability: Int) {
        this.durability = durability
    }

    fun glint() {
        glint = true
    }

    fun fireResistant() {
        fireResistant = true
    }

    fun stackable() {
        stackable = true
    }

    fun material(string: String) {
        baseMaterial = try {
            ResourceLocation.from(string)
        } catch (e: Exception) {
            return
        }
    }
    fun material(namespace: String, path: String) {
        baseMaterial = try {
            ResourceLocation.from(namespace, path)
        } catch (e: Exception) { return }
    }
    fun material(rl: ResourceLocation) {
        baseMaterial = rl
    }

    fun build(): ItemPhysicalProperties = ItemPhysicalProperties(
        glint, customModelData, baseMaterial,
        fireResistant, stackable, maxStackSize, durability, rarity
    )
}
class FoodPropertiesBuilder {
    private var nutrition: Int = 0
    private var saturationModifier: Float = 0f
    private var isMeat: Boolean = false
    private var canAlwaysEat: Boolean = false
    private var fastFood: Boolean = false
    private var effects: List<FoodEffect> = emptyList()

    fun effects(effects: List<FoodEffect>) {
        this.effects = effects
    }

    fun addEffects(effects: List<FoodEffect>) {
        this.effects += effects
    }

    fun addEffect(effect: FoodEffect) {
        this.effects += effect
    }

    fun fastFood() {
        this.fastFood = true
    }

    fun canAlwaysEat() {
        this.canAlwaysEat = true
    }

    fun isMeat() {
        this.isMeat = true
    }

    fun saturationModifier(saturationModifier: Float) {
        this.saturationModifier = saturationModifier
    }

    fun nutrition(nutrition: Int) {
        this.nutrition = nutrition
    }

    fun build(): FoodProperties = FoodProperties(
        nutrition, saturationModifier, isMeat, canAlwaysEat, fastFood, effects
    )
}

enum class InteractionHand {
    MAIN_HAND,
    OFF_HAND
}

interface ItemEventsHandler {
    fun onInteract(self: PlatformItemStack, hand: InteractionHand, user: PlatformLivingEntity)
    fun whileInHand(self: PlatformItemStack, hand: InteractionHand, user: PlatformLivingEntity)
    fun whenInInventory(self: PlatformItemStack, user: PlatformLivingEntity)
    fun whenInHotBar(self: PlatformItemStack, user: PlatformLivingEntity)
    fun onDropped(self: PlatformItemStack, dropper: PlatformLivingEntity)
    fun onPickedUp(self: PlatformItemStack, user: PlatformLivingEntity)
    fun onPickedUpByPlayer(self: PlatformItemStack, user: PlatformPlayer) {
        onPickedUp(self, user)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RegisterItem(
    val namespace: String = "core",
    val path: String,
)

object DefaultItemEventsHandler : ItemEventsHandler {
    override fun onInteract(self: PlatformItemStack, hand: InteractionHand, user: PlatformLivingEntity) {}
    override fun whileInHand(self: PlatformItemStack, hand: InteractionHand, user: PlatformLivingEntity) {}
    override fun whenInInventory(self: PlatformItemStack, user: PlatformLivingEntity) {}
    override fun whenInHotBar(self: PlatformItemStack, user: PlatformLivingEntity) {}
    override fun onDropped(self: PlatformItemStack, dropper: PlatformLivingEntity) {}
    override fun onPickedUp(self: PlatformItemStack, user: PlatformLivingEntity) {}
}
class BuilderBasedItemEventsHandler(
    private val onInteract: (PlatformItemStack, InteractionHand, PlatformLivingEntity) -> Unit = { _, _, _ -> },
    private val whileInHand: (PlatformItemStack, InteractionHand, PlatformLivingEntity) -> Unit = { _, _, _ -> },
    private val whenInInventory: (PlatformItemStack, PlatformLivingEntity) -> Unit = { _, _ -> },
    private val whenInHotBar: (PlatformItemStack, PlatformLivingEntity) -> Unit = { _, _ -> },
    private val onDropped: (PlatformItemStack, PlatformLivingEntity) -> Unit = { _, _ -> },
    private val onPickedUp: (PlatformItemStack, PlatformLivingEntity) -> Unit = { _, _ -> },
    private val onPickedUpByPlayer: (PlatformItemStack, PlatformPlayer) -> Unit = { _, _ -> }
) : ItemEventsHandler {
    override fun onInteract(self: PlatformItemStack, hand: InteractionHand, user: PlatformLivingEntity) {
        onInteract.invoke(self, hand, user)
    }
    override fun whileInHand(self: PlatformItemStack, hand: InteractionHand, user: PlatformLivingEntity) {
        whileInHand.invoke(self, hand, user)
    }
    override fun whenInInventory(self: PlatformItemStack, user: PlatformLivingEntity) {
        whenInInventory.invoke(self, user)
    }
    override fun whenInHotBar(self: PlatformItemStack, user: PlatformLivingEntity) {
        whenInHotBar.invoke(self, user)
    }
    override fun onDropped(self: PlatformItemStack, dropper: PlatformLivingEntity) {
        onDropped.invoke(self, dropper)
    }
    override fun onPickedUp(self: PlatformItemStack, user: PlatformLivingEntity) {
        onPickedUp.invoke(self, user)
    }
    override fun onPickedUpByPlayer(self: PlatformItemStack, user: PlatformPlayer) {
        onPickedUpByPlayer.invoke(self, user)
    }
}
class BuilderBasedItemEventsHandlerBuilder {
    var onInteract: (PlatformItemStack, InteractionHand, PlatformLivingEntity) -> Unit = { _, _, _ -> }
    var whileInHand: (PlatformItemStack, InteractionHand, PlatformLivingEntity) -> Unit = { _, _, _ -> }
    var whenInInventory: (PlatformItemStack, PlatformLivingEntity) -> Unit = { _, _ -> }
    var whenInHotBar: (PlatformItemStack, PlatformLivingEntity) -> Unit = { _, _ -> }
    var onDropped: (PlatformItemStack, PlatformLivingEntity) -> Unit = { _, _ -> }
    var onPickedUp: (PlatformItemStack, PlatformLivingEntity) -> Unit = { _, _ -> }
    var onPickedUpByPlayer: (PlatformItemStack, PlatformPlayer) -> Unit = { _, _ -> }

    fun build(): BuilderBasedItemEventsHandler {
        return BuilderBasedItemEventsHandler(
            onInteract, whileInHand, whenInInventory, whenInHotBar, onDropped, onPickedUp, onPickedUpByPlayer
        )
    }
}
class ItemDescriptorBuilder(private val displayName: Component) {
    private val lore: MutableList<Component> = mutableListOf()
    private val baseNbt: MutableMap<String, Any> = mutableMapOf()
    private var handler: ItemEventsHandler = DefaultItemEventsHandler
    private var physicalProps: ItemPhysicalProperties = ItemPhysicalProperties()
    private var foodProps: FoodProperties? = null

    fun lore(component: Component) {
        lore += component
    }
    fun lore(string: String) {
        lore += string.textComponent()
    }

    fun String.nbt(value: Any) {
        baseNbt[this] = value
    }

    fun handler(itty: BuilderBasedItemEventsHandlerBuilder.() -> Unit) {
        handler = BuilderBasedItemEventsHandlerBuilder().apply(itty).build()
    }

    fun physicalProps(itty: ItemPhysicalPropertiesBuilder.() -> Unit) {
        physicalProps = ItemPhysicalPropertiesBuilder().apply(itty).build()
    }

    fun foodProps(itty: FoodPropertiesBuilder.() -> Unit) {
        foodProps = FoodPropertiesBuilder().apply(itty).build()
    }

    fun build(): ItemDescriptor = ItemDescriptor(
        displayName,
        lore, baseNbt, handler, physicalProps, foodProps
    )
}

fun item(displayName: Component, descriptor: ItemDescriptorBuilder.() -> Unit): ItemDescriptor =
    ItemDescriptorBuilder(displayName).apply(descriptor).build()
fun item(displayName: String, descriptor: ItemDescriptorBuilder.() -> Unit): ItemDescriptor =
    item(displayName.textComponent(), descriptor)

fun String.textComponent(): Component = Component.text(this)
fun String.colorComponent(color: TextColor): Component = Component.text(this).color(color)

object Items {
    @JvmStatic
    @RegisterItem(path = "test_item")
    val testItem = item(
        "A test Item".colorComponent(NamedTextColor.GOLD)
    ) {
        lore(Component.text("This is simply a text item. nothing more or less... or is it...")
            .color(NamedTextColor.DARK_BLUE))

        physicalProps {
            glint()
            fireResistant()
        }

        handler {
            onInteract = { self, _, user ->
                if (user.isPlayer && !self.hasNbt("has_used")) {
                    user as PlatformPlayer
                    user.sendMessage("This is a simple text and if you see this... it works lmao. it should only work on the first click")
                    self.applyNbt("has_used" pair true)
                }
            }
        }
    }
}

abstract class PlatformItemFactory : InternalPlatformItemFactory {
    protected val logger = ContextLogger(ContextLogger.ContextType.REGISTRY, "item-factory")
    protected val descriptors = ConcurrentHashMap<ResourceLocation, ItemDescriptor>()
    @Throws(ItemProductionError::class)
    override fun registerItem(id: ResourceLocation, descriptor: ItemDescriptor) {
        val prev = descriptors.putIfAbsent(id, descriptor)
        if (prev != null) error("Duplicate item ${id}")
    }
    override fun getDescriptor(id: ResourceLocation): ItemDescriptor? = descriptors[id]
    override fun create(id: ResourceLocation, overrides: Map<String, Any>): PlatformItemStack? {
        val desc = descriptors[id] ?: return null
        return try {
            val stack = buildStackFromDescriptor(desc, overrides)
            applyOverrides(stack, overrides)
            stack
        } catch (t: Throwable) {
            logger.severe("Error while trying to create item ${id}: ${t.message}")
            null
        }
    }
    protected fun applyOverrides(stack: PlatformItemStack, overrides: Map<String, Any>) {
        try {
            overrides["count"]?.let { count ->
                val c = (count as Number).toInt()
                stack.count = c
            }

            overrides["displayName"]?.let { name ->
                if (name is Component)
                    stack.name = name
                else if (name as? String != null)
                    stack.name = name.textComponent()
            }

            // merge baseNbt overrides if any (adapter can provide a method setNbt(Map) or applyNbt)
            overrides["nbt"]?.let { nbt ->
                if (nbt is Map<*, *>) {
                    stack.applyNbt(nbt as Map<String, Any>)
                }
            }
        } catch (t: Throwable) {
            logger.severe("Error while trying to apply overrides to item: ${t.message}")
        }
    }

    /**
     * Build a platform stack directly from a descriptor instance.
     * Platform implementations must implement this and must NOT call create(id) or fromRegisteredDescriptor.
     */
    protected abstract fun buildStackFromDescriptor(descriptor: ItemDescriptor, overrides: Map<String, Any> = emptyMap()): PlatformItemStack

    /**
     * Very small runtime scanner for fields annotated with @RegisterItem.
     *
     * Notes:
     * - This scans the provided classes' declared fields and registers any static fields whose value is an ItemDescriptor.
     * - On Kotlin `object` classes the field may be synthesized differently — if the annotation doesn't show up on the field,
     *   prefer calling `ItemRegistry.registerItem(YourObject.yourDescriptor)` directly from your platform init.
     */
    protected fun scanAndRegister(vararg classes: Class<*>) {
        for (cls in classes) {
            for (f in cls.declaredFields) {
                try {
                    if (!f.isAnnotationPresent(RegisterItem::class.java)) continue
                    if (!Modifier.isStatic(f.modifiers)) {
                        // we only support static fields via reflection here
                        continue
                    }
                    f.isAccessible = true
                    val valObj = f.get(null)
                    if (valObj is ItemDescriptor) {
                        val an = f.getAnnotation(RegisterItem::class.java)
                        registerItem(
                            an.namespace rli an.path,
                            valObj
                        )
                    }
                } catch (t: Throwable) {
                    // log registration error — keep scanning
                }
            }
        }
    }
}

internal interface InternalPlatformItemFactory {
    @Throws(ItemProductionError::class)
    fun registerItem(id: ResourceLocation, descriptor: ItemDescriptor)
    fun getDescriptor(id: ResourceLocation): ItemDescriptor?
    fun create(id: ResourceLocation, overrides: Map<String, Any> = emptyMap()): PlatformItemStack?

    /**
     * Should not call any internal api like create as it will cause a stack overflow
     */
    fun fromMaterial(material: PlatformMaterial?): PlatformItemStack?
    /**
     * Should not call any internal api like create as it will cause a stack overflow
     */
    fun fromDescriptor(descriptor: ItemDescriptor): PlatformItemStack
    /**
     * Should not call any internal api like create as it will cause a stack overflow
     */
    @Throws(DescriptorNotRegisteredException::class)
    fun fromRegisteredDescriptor(descriptor: ResourceLocation): PlatformItemStack

    fun materialOf(rl: ResourceLocation): PlatformMaterial?
}