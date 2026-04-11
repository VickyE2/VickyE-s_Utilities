package org.vicky.platform

import org.vicky.platform.entity.*
import org.vicky.platform.items.ItemDescriptor
import org.vicky.platform.items.PlatformItemFactory
import org.vicky.platform.items.RegisterItem
import org.vicky.utilities.Pair
import java.lang.reflect.Modifier

interface PlatformClassProvider {
    /** All classes owned by this plugin/mod */
    fun getOwnClasses(): Collection<Class<*>>

    /** Classes from other plugins/mods that opt-in */
    fun getForeignClasses(): Collection<Class<*>>
}

fun registerAllAnnotatedThings(
    classProvider: PlatformClassProvider,
    itemFactory: PlatformItemFactory,
    effectRegistry: PlatformEffectBridge<*>,
    mobRegistry: PlatformEntityFactory
) {
    val classes = classProvider.getOwnClasses() + classProvider.getForeignClasses()

    // ITEMS
    AnnotationScanner.scanFields(classes, RegisterItem::class.java) { value, ann ->
        if (value is ItemDescriptor) {
            itemFactory.registerItem(
                ann.namespace rli ann.path,
                value
            )
        }
    }

    // EFFECTS (priority aware)
    val effects = mutableListOf<Pair<Int, EffectDescriptor>>()
    AnnotationScanner.scanFields(classes, RegisterEffect::class.java) { value, ann ->
        if (value is EffectDescriptor) {
            effects += ann.priority to value
        }
    }
    effects.sortedByDescending { it.first }
        .forEach { (_, effect) -> effectRegistry.registerEffect(effect) }

    // MOBS
    AnnotationScanner.scanFields(classes, RegisterMob::class.java) { value, _ ->
        if (value is MobEntityDescriptor) {
            mobRegistry.register(value)
        }
    }
}


object AnnotationScanner {

    fun <A : Annotation> scanFields(
        classes: Iterable<Class<*>>,
        annotation: Class<A>,
        consumer: (fieldValue: Any, annotation: A) -> Unit
    ) {
        for (cls in classes) {
            for (field in cls.declaredFields) {
                if (!field.isAnnotationPresent(annotation)) continue
                if (!Modifier.isStatic(field.modifiers)) continue

                try {
                    field.isAccessible = true
                    val value = field.get(null) ?: continue
                    val ann = field.getAnnotation(annotation)
                    consumer(value, ann)
                } catch (_: Throwable) {
                    // platform should log if it cares
                }
            }
        }
    }
}