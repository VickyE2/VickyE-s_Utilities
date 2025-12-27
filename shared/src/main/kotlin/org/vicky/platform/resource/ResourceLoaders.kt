/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.resource

import org.vicky.platform.utils.ResourceLocation
import java.io.InputStream

interface ResourceLoader {
    fun exists(id: ResourceLocation): Boolean
    fun open(id: ResourceLocation): InputStream
}

class ClasspathResourceLoader(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
) : ResourceLoader {

    override fun exists(id: ResourceLocation): Boolean {
        return classLoader.getResource(toPath(id)) != null
    }

    override fun open(id: ResourceLocation): InputStream {
        return classLoader.getResourceAsStream(toPath(id))
            ?: error("Resource not found: $id")
    }

    private fun toPath(id: ResourceLocation): String {
        return "assets/${id.namespace}/${id.path}"
    }
}
