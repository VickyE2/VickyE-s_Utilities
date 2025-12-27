package org.vicky.gradle

import kotlinx.serialization.json.Json
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.vicky.gradle.blockbench.*
import java.io.File
import javax.inject.Inject

@kotlinx.serialization.Serializable
data class BbCache(
    val hashes: MutableMap<String, String> = mutableMapOf()
)

abstract class BlockbenchExtension @Inject constructor(objects: ObjectFactory) {
    val modid: Property<String> = objects.property(String::class.java)
    val outDir: Property<String> = objects.property(String::class.java)
    val bbdirectory: Property<String> = objects.property(String::class.java)
    val runEveryBuild: Property<Boolean> = objects.property(Boolean::class.java)
}

class BlockbenchDissolverPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("bbDissolver", BlockbenchExtension::class.java)
        val extension = project.extensions.getByType(BlockbenchExtension::class.java)
        val outDirProvider = extension.outDir
            .orElse(extension.modid.map { "assets/$it" })
            .orElse("assets/default_modid")
        val bbDirectoryProvider = extension.bbdirectory
            .orElse("blockbench")
        val runEveryBuildProvider = extension.runEveryBuild
            .orElse(false)

        val convertBlockbench = project.tasks.register("convertBlockbenchToGecko") {
            group = "blockbench"
            description = "Convert BB models in resources/blockbench to GeckoLib format"

            doLast {
                val resourcesDir = File(project.projectDir, "src/main/resources/${bbDirectoryProvider.get()}")
                if (!resourcesDir.exists()) {
                    log("No BlockBench folder found at $resourcesDir", true)
                    return@doLast
                }

                val outputBase = File(project.buildDir, outDirProvider.get())
                outputBase.mkdirs()
                val json = Json {
                    prettyPrint = true
                }
                log("Output directory will be: ${outputBase.path.toString().replace(Regex("^([A-Z]:)\\\\.*?(?=\\\\build)"), "$1\\***")}")


                val cacheDir = File(project.buildDir, "bb-dissolver")
                val cacheFile = File(cacheDir, "cache.json")
                cacheDir.mkdirs()

                val cache = if (cacheFile.exists()) {
                    json.decodeFromString(BbCache.serializer(), cacheFile.readText())
                } else {
                    BbCache()
                }

                if (runEveryBuildProvider.get() == true) {
                    log("Currently the conversion system is set to run everytime `processResources` task is called. this is really resource intensive for larger file amount and should only be done if you know what you're doing")
                }

                // Iterate over all .bbmodel files
                resourcesDir.walkTopDown().filter { it.isFile && it.extension == "bbmodel" }.forEach { bbFile ->
                    val fileKey = bbFile.relativeTo(resourcesDir).path
                    val currentHash = sha256(bbFile)
                    val previousHash = cache.hashes[fileKey]

                    if (previousHash == currentHash && !runEveryBuildProvider.get()) {
                        log("Skipping ${bbFile.name} (unchanged)")
                        return@forEach
                    }

                    cache.hashes[fileKey] = currentHash
                    log("Processing ${bbFile.name}")

                    val bbModel = BBConverter.loadBBModel(bbFile)
                        ?: run {
                            log("Failed to load ${bbFile.name}", true)
                            return@forEach
                        }

                    try {
                        /* ---------- STAGE EVERYTHING ---------- */

                        val resolvedTextures = bbModel.resolveTextures()
                        val geo = bbModel.geoGeom()
                        val anim = bbModel.geoAnim()

                        /* ---------- COMMIT TO DISK ---------- */

                        // Textures
                        val texFolder = File(outputBase, "textures/entity").apply { mkdirs() }
                        resolvedTextures.forEachIndexed { index, resolved ->
                            val name = bbModel.textures.getOrNull(index)?.name ?: "texture$index.png"
                            val texFile = File(texFolder, name)
                            javax.imageio.ImageIO.write(resolved.image, "png", texFile)

                            resolved.mcmeta?.let {
                                File(texFolder, "$name.mcmeta")
                                    .writeText(json.encodeToString(McMeta.serializer(), it))
                            }
                        }

                        // Geometry
                        val geoFolder = File(outputBase, "geo").apply { mkdirs() }
                        File(geoFolder, "${bbModel.model_identifier}.geo.json")
                            .writeText(json.encodeToString(GeoGeometry.serializer(), geo))

                        // Animations
                        val animFolder = File(outputBase, "animations").apply { mkdirs() }
                        File(animFolder, "${bbModel.model_identifier}.animation.json")
                            .writeText(json.encodeToString(GeoAnimation.serializer(), anim))

                    }
                    catch (e: Exception) {
                        log("An error occurred while processing ${bbModel.model_identifier}", true)
                        e.printStackTrace()
                    }
                }

                cacheFile.writeText(
                    json.encodeToString(BbCache.serializer(), cache)
                )
                log("BlockBench conversion completed!")
            }
        }

        // Optionally, hook into processResources so generated assets are included in the JAR
        project.tasks.named("processResources") {
            dependsOn(convertBlockbench)
            doLast {
                val generatedAssets = File(project.buildDir, outDirProvider.get())
                if (generatedAssets.exists()) {
                    project.copy {
                        from(generatedAssets.parentFile)
                        into(File(project.buildDir, "resources/main/assets"))
                    }
                }
            }
        }
    }
}

fun sha256(file: File): String {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(8_192)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
fun log(message: String, isError: Boolean = false) {
    println("[BB-DISSOLVER] ${if (isError) "[\u001B[0;31mERROR\u001B[0m]\u001B[0;31m" else "[\u001B[0;34mINFO\u001B[0m]\u001B[0;34m"} $message \u001B[0m")
}