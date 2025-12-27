package org.vicky.gradle

import kotlinx.serialization.json.Json
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.vicky.gradle.blockbench.*
import java.io.File
import javax.inject.Inject

abstract class BlockbenchExtension @Inject constructor(objects: ObjectFactory) {
    val modid: Property<String> = objects.property(String::class.java)
    val outDir: Property<String> = objects.property(String::class.java)
}

class BlockbenchDissolverPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("bbDissolver", BlockbenchExtension::class.java)
        val extension = project.extensions.getByType(BlockbenchExtension::class.java)
        val outDirProvider = extension.outDir
            .orElse(extension.modid.map { "assets/$it" })
            .orElse("assets/default_modid")

        val convertBlockbench = project.tasks.register("convertBlockbenchToGecko") {
            group = "blockbench"
            description = "Convert BB models in resources/blockbench to GeckoLib format"

            doLast {

                println("Output directory will be: ${outDirProvider.get()}")
                val resourcesDir = File(project.projectDir, "src/main/resources/blockbench")
                if (!resourcesDir.exists()) {
                    println("No BlockBench folder found at $resourcesDir")
                    return@doLast
                }

                val outputBase = File(project.buildDir, outDirProvider.get())
                outputBase.mkdirs()
                val json = Json {
                    prettyPrint = true
                }

                // Iterate over all .bbmodel files
                resourcesDir.walkTopDown().filter { it.isFile && it.extension == "bbmodel" }.forEach { bbFile ->
                    println("Processing ${bbFile.name}")
                    val bbModel = BBConverter.loadBBModel(bbFile)
                        ?: return@forEach println("Failed to load ${bbFile.name}")

                    // Resolve textures
                    val resolvedTextures = bbModel.resolveTextures()
                    resolvedTextures.forEachIndexed { index, resolved ->
                        val texFolder = File(outputBase, "textures/entity")
                        texFolder.mkdirs()
                        val name = bbModel.textures.getOrNull(index)?.name ?: "texture$index.png"
                        val texFile = File(texFolder, name)
                        javax.imageio.ImageIO.write(resolved.image, "png", texFile)
                        // write mcmeta if animated
                        resolved.mcmeta?.let { mcMeta ->
                            val metaFile = File(texFolder, "$name.mcmeta")
                            metaFile.writeText(json.encodeToString(McMeta.serializer(), mcMeta))
                        }
                    }

                    // Save geometry
                    val geo = bbModel.geoGeom()
                    val geoFolder = File(outputBase, "geo")
                    geoFolder.mkdirs()
                    val geoFile = File(geoFolder, "${bbModel.model_identifier}.geo.json")
                    geoFile.writeText(json.encodeToString(GeoGeometry.serializer(), geo))

                    // Save animations
                    val anim = bbModel.geoAnim()
                    val animFolder = File(outputBase, "animations")
                    animFolder.mkdirs()
                    val animFile = File(animFolder, "${bbModel.model_identifier}.animation.json")
                    animFile.writeText(json.encodeToString(
                        GeoAnimation.serializer(), anim
                    ))
                }

                println("BlockBench conversion completed! Output in: $outputBase")
            }
        }

        // Optionally, hook into processResources so generated assets are included in the JAR
        project.tasks.named("processResources") {
            dependsOn(convertBlockbench)
            doLast {
                val generatedAssets = File(project.buildDir, outDirProvider.get())
                if (generatedAssets.exists()) {
                    project.copy {
                        from(generatedAssets)
                        into(File(project.buildDir, "resources/main/assets"))
                    }
                }
            }
        }
    }
}
