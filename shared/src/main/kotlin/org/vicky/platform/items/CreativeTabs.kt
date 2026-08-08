package org.vicky.platform.items

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.vicky.platform.PlatformPlugin
import org.vicky.platform.entity.rli
import org.vicky.platform.item.PlatformItemStack
import org.vicky.platform.utils.ResourceLocation
import org.vicky.utilities.ContextLogger.ContextLogger
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegisterCreativeTab(
    val namespace: String = "core",
    val path: String
)

object Tabs {
    @JvmField
    @field:RegisterCreativeTab(path = "test_tab")
    val testTab = CreativeTabDescriptor(
        "A yellow test tab".colorComponent(NamedTextColor.YELLOW)
    ) {
        PlatformPlugin.itemFactory().create("core" rli "test_item")
    }
}

data class CreativeTabDescriptor(
    val title: Component,
    val icon: () -> PlatformItemStack
)

abstract class PlatformCreativeTabRegistry {
    protected val logger =
        ContextLogger(
            ContextLogger.ContextType.REGISTRY,
            "creative-tabs"
        )

    protected val descriptors =
        ConcurrentHashMap<ResourceLocation, CreativeTabDescriptor>()

    fun registerTab(
        id: ResourceLocation,
        descriptor: CreativeTabDescriptor
    ) {
        val previous = descriptors.putIfAbsent(id, descriptor)

        if (previous != null) {
            error("Duplicate creative tab $id")
        }
    }

    fun getDescriptor(
        id: ResourceLocation
    ): CreativeTabDescriptor? =
        descriptors[id]

    fun getAllDescriptors(): Collection<CreativeTabDescriptor> =
        descriptors.values

    /**
     * Platform implementation turns the descriptor into the
     * actual Minecraft creative tab.
     */
    protected abstract fun registerPlatformTab(
        id: ResourceLocation,
        descriptor: CreativeTabDescriptor
    )

    protected fun scanAndRegister(
        vararg classes: Class<*>
    ) {
        for (cls in classes) {
            for (field in cls.declaredFields) {
                try {
                    val annotation =
                        field.getAnnotation(
                            RegisterCreativeTab::class.java
                        ) ?: continue

                    if (!Modifier.isStatic(field.modifiers)) {
                        continue
                    }

                    field.isAccessible = true

                    val value = field.get(null)

                    if (value !is CreativeTabDescriptor) {
                        continue
                    }

                    val id = ResourceLocation.from(
                        annotation.namespace,
                        annotation.path
                    )

                    registerTab(id, value)
                    registerPlatformTab(id, value)

                } catch (t: Throwable) {
                    logger.severe(
                        "Failed to register creative tab from ${cls.name}: ${t.message}"
                    )
                }
            }
        }
    }
}