package org.vicky.gradle.blockbench

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO

object GeoAnimatedDataSerializer : KSerializer<GeoAnimatedData> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("GeoAnimatedData") {
            element<String>("lerp_mode", isOptional = true)
            element("pre", GeoTransformVector.serializer().descriptor, isOptional = true)
            element("post", GeoTransformVector.serializer().descriptor, isOptional = true)
            element("vector", GeoTransformVector.serializer().descriptor, isOptional = true)
        }

    override fun deserialize(decoder: Decoder): GeoAnimatedData {
        val input = decoder as? JsonDecoder
            ?: error("GeoAnimatedData can only be deserialized from JSON")

        val obj = input.decodeJsonElement().jsonObject

        // --- CASE B: shorthand { vector: [...] }
        if ("vector" in obj) {
            val vector = input.json.decodeFromJsonElement<GeoTransformVector>(
                obj.getValue("vector")
            )

            return GeoAnimatedData(
                lerpMode = "linear",
                post = vector
            )
        }

        // --- CASE A: full keyframe
        val lerp = obj["lerp_mode"]?.jsonPrimitive?.content ?: "linear"

        val pre = obj["pre"]?.let {
            input.json.decodeFromJsonElement<GeoTransformVector>(it)
        }

        val post = obj["post"]?.let {
            input.json.decodeFromJsonElement<GeoTransformVector>(it)
        }

        return GeoAnimatedData(
            lerpMode = lerp,
            pre = pre,
            post = post
        )
    }

    override fun serialize(encoder: Encoder, value: GeoAnimatedData) {
        val output = encoder as? JsonEncoder
            ?: error("GeoAnimatedData can only be serialized to JSON")

        val obj = buildJsonObject {
            put("lerp_mode", JsonPrimitive(value.lerpMode))
            value.pre?.let {
                put("pre", output.json.encodeToJsonElement(it))
            }
            value.post?.let {
                put("post", output.json.encodeToJsonElement(it))
            }
        }

        output.encodeJsonElement(obj)
    }
}
object OutlinerNodeSerialiser : KSerializer<OutlinerNode> {
    @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("OutlinerNode", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): OutlinerNode {
        val input = decoder as? JsonDecoder
            ?: error("OutlinerNode can only be deserialized from JSON")

        val element = input.decodeJsonElement()

        return when (element) {

            // CASE 1: "uuid-string" → leaf
            is JsonPrimitive -> {
                OutlinerLeaf(element.content)
            }

            // CASE 2: { uuid, isOpen, children } → group
            is JsonObject -> {
                val uuid = element["uuid"]?.jsonPrimitive?.content
                    ?: error("OutlinerGroup missing 'uuid'")

                val isOpen = element["isOpen"]?.jsonPrimitive?.booleanOrNull

                val children = element["children"]?.let { childrenElem ->
                    if (childrenElem is JsonArray)
                        childrenElem.map {
                            input.json.decodeFromJsonElement(
                                OutlinerNodeSerialiser,
                                it
                            )
                        }
                    else null
                }

                OutlinerGroup(uuid, isOpen, children)
            }

            else -> error("Invalid OutlinerNode JSON: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: OutlinerNode) {
        val output = encoder as? JsonEncoder
            ?: error("OutlinerNode can only be serialized to JSON")

        val json = when (value) {

            is OutlinerLeaf ->
                JsonPrimitive(value.uuid)

            is OutlinerGroup ->
                buildJsonObject {
                    put("uuid", JsonPrimitive(value.uuid))
                    value.isOpen?.let { put("isOpen", JsonPrimitive(it)) }
                    value.children?.let {
                        put(
                            "children",
                            JsonArray(it.map { child ->
                                output.json.encodeToJsonElement(
                                    OutlinerNodeSerialiser,
                                    child
                                )
                            })
                        )
                    }
                }
        }

        output.encodeJsonElement(json)
    }
}
object Vec3Serializer : KSerializer<Vec3> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vec3") {
        element<Double>("x")
        element<Double>("y")
        element<Double>("z")
    }

    private fun JsonElement.toDoubleLenient(): Double =
        // Accept number primitives or strings that contain numbers
        this.jsonPrimitive.let { prim ->
            prim.doubleOrNull
                ?: prim.content.toDoubleOrNull()
                ?: error("Expected numeric value but was: $this")
        }

    override fun serialize(encoder: Encoder, value: Vec3) {
        val out = encoder as? JsonEncoder ?: error("Vec3Serializer only supports JSON")
        val arr = buildJsonArray {
            add(JsonPrimitive(value.x))
            add(JsonPrimitive(value.y))
            add(JsonPrimitive(value.z))
        }
        out.encodeJsonElement(arr)
    }

    override fun deserialize(decoder: Decoder): Vec3 {
        val input = decoder as? JsonDecoder ?: error("Vec3Serializer only supports JSON")
        val elem = input.decodeJsonElement()

        return when (elem) {
            is JsonArray -> {
                if (elem.size != 3) error("Vec3 array must have 3 elements but had ${elem.size}")
                Vec3(
                    elem[0].toDoubleLenient(),
                    elem[1].toDoubleLenient(),
                    elem[2].toDoubleLenient()
                )
            }
            is JsonObject -> {
                val xEl = elem["x"] ?: error("Vec3 object missing 'x'")
                val yEl = elem["y"] ?: error("Vec3 object missing 'y'")
                val zEl = elem["z"] ?: error("Vec3 object missing 'z'")
                Vec3(xEl.toDoubleLenient(), yEl.toDoubleLenient(), zEl.toDoubleLenient())
            }
            is JsonPrimitive -> {
                // Defensive: if someone encoded "1,2,3" as a single string (unlikely), try parse
                val parts = elem.content.split(",").map { it.trim() }
                if (parts.size == 3) {
                    Vec3(parts[0].toDoubleOrNull() ?: error("Invalid number ${parts[0]}"),
                        parts[1].toDoubleOrNull() ?: error("Invalid number ${parts[1]}"),
                        parts[2].toDoubleOrNull() ?: error("Invalid number ${parts[2]}"))
                } else {
                    error("Invalid Vec3 JSON primitive: $elem")
                }
            }
            else -> error("Invalid Vec3 JSON: $elem")
        }
    }
}
object StringVec3Serializer : KSerializer<StringVec3> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vec3") {
        element<String>("x")
        element<String>("y")
        element<String>("z")
    }

    private fun JsonElement.toStringLenient(): String =
        this.jsonPrimitive.let { prim ->
            prim.contentOrNull?.trim()
                ?: prim.content.trim()
        }

    override fun serialize(encoder: Encoder, value: StringVec3) {
        val out = encoder as? JsonEncoder ?: error("StringVec3Serializer only supports JSON")
        val arr = buildJsonArray {
            add(JsonPrimitive(value.x))
            add(JsonPrimitive(value.y))
            add(JsonPrimitive(value.z))
        }
        out.encodeJsonElement(arr)
    }

    override fun deserialize(decoder: Decoder): StringVec3 {
        val input = decoder as? JsonDecoder ?: error("StringVec3Serializer only supports JSON")
        val elem = input.decodeJsonElement()

        return when (elem) {
            is JsonArray -> {
                if (elem.size != 3) error("StringVec3 array must have 3 elements but had ${elem.size}")
                StringVec3(
                    elem[0].toStringLenient(),
                    elem[1].toStringLenient(),
                    elem[2].toStringLenient()
                )
            }
            is JsonObject -> {
                val xEl = elem["x"]?.toStringLenient() ?: ""
                val yEl = elem["y"]?.toStringLenient() ?: ""
                val zEl = elem["z"]?.toStringLenient() ?: ""
                StringVec3(xEl, yEl, zEl)
            }
            is JsonPrimitive -> {
                // Defensive: if someone encoded "1,2,3" as a single string (unlikely), try parse
                val parts = elem.content.split(",").map { it.trim() }
                if (parts.size == 3) {
                    StringVec3(
                        parts[0],
                        parts[1],
                        parts[2]
                    )
                } else {
                    error("Invalid StringVec3 JSON primitive: $elem")
                }
            }
            else -> error("Invalid Vec3 JSON: $elem")
        }
    }
}

@Serializable
data class GeoAnimation(
    @SerialName("format_version") val formatVersion: String,
    val animations: Map<String, GeoAnimationData>
)
@Serializable
data class GeoAnimationData(
    val loop: Boolean,
    @SerialName("animation_length") val animationLength: Double,
    val bones: Map<String, GeoAnimated>
)
@Serializable
data class GeoAnimated(
    val position: Map<String, GeoAnimatedData> = emptyMap(),
    val rotation: Map<String, GeoAnimatedData> = emptyMap(),
    val scale: Map<String, GeoAnimatedData> = emptyMap(),
)
@Serializable(with = GeoAnimatedDataSerializer::class)
data class GeoAnimatedData(
    @SerialName("lerp_mode") val lerpMode: String,
    val pre: GeoTransformVector? = null,
    val post: GeoTransformVector? = null
)
@Serializable
data class GeoTransformVector(
    val vector: List<String>
)
@Serializable
data class GeoGeometry(
    val description: GeoGeometryDescription,
    val bones: List<GeoBone>
)
@Serializable
data class GeoModel(
    @SerialName("minecraft:geometry") val geometries: List<GeoGeometry>,
    @SerialName("format_version") val formatVersion: String
)
@Serializable
data class GeoGeometryDescription(
    val identifier: String,
    @SerialName("texture_width") val textureWidth: Int,
    @SerialName("texture_height") val textureHeight: Int,
    @SerialName("visible_bounds_width") val  visibleBoundsWidth: Double,
    @SerialName("visible_bounds_height") val visibleBoundsHeight: Double,
    @SerialName("visible_bounds_offset") val visibleBoundsOffset: List<Double>
)
@Serializable
data class GeoBone @OptIn(ExperimentalSerializationApi::class) constructor(
    val name: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val parent: String = "",
    val pivot: Vec3,
    val cubes: List<GeoCube>
)
@Serializable
data class GeoCube(
    val origin: Vec3,
    val size: Vec3,
    val uv: Map<String, GeoUvData>
)
@Serializable
data class GeoUvData(
    val uv: List<Double>,
    @SerialName("uv_size") val uvSize: List<Double>
)




@Serializable
data class BlockBenchModel(
    val meta: Meta,
    val name: String,
    @SerialName("model_identifier") val modelIdentifier: String = "",
    @SerialName("front_gui_light") val frontGuiLight: Boolean = false,
    @SerialName("visible_box") val visibleBox: List<Double> = listOf(),
    @SerialName("variable_placeholders") val variablePlaceholders: String = "",
    @SerialName("variable_placeholders_buttons") val variablePlaceholdersButtons: List<String> = listOf(),
    @SerialName("timeline_setups") val timelineSetups: List<String> = listOf(),
    @SerialName("unhandled_root_fields") val unhandledRootFields: Map<String, String> = mapOf(),
    @SerialName("activity_tracker") val activityTracker: Int = 0,
    @SerialName("geckolib_modid") val geckolibModid: String = "",
    @SerialName("geckolib_model_type") val geckolibModelType: String = "entity",
    val resolution: Resoulution,
    val elements: List<Element> = listOf(),
    val groups: List<Group> = listOf(),
    val outliner: List<OutlinerNode> = listOf(),
    val textures: List<Texture> = listOf(),
    val animations: List<Animations> = listOf(),
    @SerialName("animation_variable_placeholders") val animationVariablePlaceholders: String = "",
)
@Serializable
data class Meta(
    @SerialName("format_version") val formatVersion: String,
    @SerialName("model_format") val modelFormat: String,
    @SerialName("box_uv") val boxUv: Boolean
)
@Serializable
data class Resoulution(
    val width: Int,
    val height: Int
)
@Serializable(with = Vec3Serializer::class)
data class Vec3(val x: Double, val y: Double, val z: Double) {
    companion object {
        val ZERO: Vec3 = Vec3(0.0, 0.0, 0.0)
    }

    constructor(from: List<Double>) : this(
        from[0], from[1], from[2]
    )
    fun toList(): List<Double> = listOf(x, y, z)
    override fun toString(): String = "[$x, $y, $z]"
}
@Serializable(with = StringVec3Serializer::class)
data class StringVec3(val x: String, val y: String, val z: String) {
    companion object {
        val ZERO: Vec3 = Vec3(0.0, 0.0, 0.0)
    }
    constructor(from: List<String>) : this(
        from[0], from[1], from[2]
    )
    fun toList(): List<String> = listOf(x, y, z)
    override fun toString(): String = "[$x, $y, $z]"
}
@Serializable
data class Element(
    val name: String,
    val type: String,
    val uuid: String,
    val from: Vec3 = Vec3.ZERO,
    val to: Vec3 = Vec3.ZERO,
    val origin: Vec3 = Vec3.ZERO,
    val color: Int = 0,
    val faces: Map<String, UvData> = mapOf(),
    @SerialName("box_uv") val boxUv: Boolean = false,
    val locked: Boolean = false,
    @SerialName("allow_mirror_modeling") val allowMirrorModeling: Boolean = false,
    @SerialName("render_order") val renderOrder: String = "default",
    val autouv: Int = 0,
    val rescale: Boolean = false,
    @SerialName("uv_offset") val uvOffset: List<Int> = listOf(),
    @SerialName("light_emission") val lightEmission: Int? = null,
)
@Serializable
data class UvData(
    val uv: List<Double>,
    val texture: Int
)
@Serializable
data class Group(
    val uuid: String,
    val export: Boolean,
    val locked: Boolean,
    val reset: Boolean,
    val shade: Boolean,
    @SerialName("mirror_uv") val mirrorUv: Boolean,
    val isOpen: Boolean,
    @SerialName("primary_selected") val primarySelected: Boolean,
    val selected: Boolean,
    val visibility: Boolean,
    val origin: Vec3,
    val rotation: Vec3,
    val color: Int,
    val autouv: Int,
    val name: String,
    val children: List<String>
)
@Serializable(with = OutlinerNodeSerialiser::class)
sealed interface OutlinerNode
@Serializable
data class OutlinerGroup(
    val uuid: String,
    val isOpen: Boolean?,
    val children: List<OutlinerNode>?
) : OutlinerNode
@Serializable
data class OutlinerLeaf(
    val uuid: String
) : OutlinerNode {
    override fun toString(): String = uuid
}
@Serializable
data class Texture(
    val name: String,
    @SerialName("relative_path") val relativePath: String = "",
    val folder: String = "",
    val namespace: String,
    val id: String,
    val group: String,
    val width: Int,
    val height: Int,
    @SerialName("uv_width") val uvWidth: Int,
    @SerialName("uv_height") val uvHeight: Int,
    val particle: Boolean = true,
    @SerialName("use_as_default") val useAsDefault: Boolean = false,
    @SerialName("layers_enabled") val layersEnabled: Boolean = false,
    @SerialName("sync_to_project") val syncToProject: String = "",
    @SerialName("render_mode") val renderMode: String = "default",
    @SerialName("render_sides") val renderSides: String = "auto",
    @SerialName("pbr_channel") val pbrChannel: String = "color",
    @SerialName("frame_time") val frameTime: Int = 20,
    @SerialName("frame_order_type") val frameOrderType: String = "",
    @SerialName("frame_order") val frameOrder: String = "",
    @SerialName("frame_interpolate") val frameInterpolate: Boolean = false,
    val visible: Boolean = true,
    val internal: Boolean = false,
    val saved: Boolean = true,
    val uuid: String,
    val source: String
)
@Serializable
data class Animations(
    val uuid: String,
    val name: String,
    val loop: String,
    val override: Boolean,
    val length: Double,
    val snapping: Int,
    val selected: Boolean,
    val saved: Boolean,
    val path: String,
    @SerialName("anim_time_update") val animTimeUpdate: String,
    @SerialName("blend_weight") val blendWeight: String,
    @SerialName("start_delay") val startDelay: String,
    @SerialName("loop_delay") val loopDelay: String,
    val animators: Map<String, Animated>
)
@Serializable
data class Animated(
    val name: String,
    val type: String,
    @SerialName("rotation_global") val rotationGlobal: Boolean = false,
    @SerialName("quaternion_interpolation") val quaternionInterpolation: Boolean = false,
    val keyframes: List<Keyframe>? = listOf()
)
@Serializable
data class Keyframe(
    val channel: String,
    val uuid: String,
    val time: Double,
    val color: Int,
    val interpolation: String,
    @SerialName("data_points") val dataPoints: List<Map<String, String>>
)

data class ResolvedTexture(
    val image: BufferedImage,
    val mcmeta: McMeta? = null
)
@Serializable
data class McMeta(
    val animation: AnimationMeta
)
@Serializable
data class AnimationMeta(
    val frametime: Int,
    val interpolate: Boolean = false,
    val frames: List<Int>? = null
)

fun BlockBenchModel.geoGeom(makeOriginRelative: Boolean = true) : GeoModel {
    val groupsByUuid = this.groups.associateBy { it.uuid }

    val elementParent = mutableMapOf<String, String?>()
    val groupParent = mutableMapOf<String, String?>()
    fun processOutlinerNode(node: Any, parentGroupUuid: String?) {
        when (node) {
            is OutlinerLeaf -> {
                elementParent[node.uuid] = parentGroupUuid
            }
            is OutlinerGroup -> {
                groupParent[node.uuid] = parentGroupUuid
                node.children?.forEach { child ->
                    when (child) {
                        is OutlinerLeaf -> processOutlinerNode(child, node.uuid)
                        is OutlinerGroup -> processOutlinerNode(child, node.uuid)
                        is Map<*, *> -> {
                            // defensive: sometimes the deserializer gives a Map for nested node
                            val childUuid = child["uuid"] as? String
                            val childChildren = child["children"]
                            if (childUuid != null) {
                                // create a minimal Outliner wrapper and recurse
                                val tmp = OutlinerGroup(childUuid, (child["isOpen"] as? Boolean), null)
                                // if children is a List<*>, iterate manually:
                                if (childChildren is List<*>) {
                                    childChildren.forEach { cc -> processOutlinerNode(cc!!, node.uuid) }
                                } else {
                                    processOutlinerNode(tmp, node.uuid)
                                }
                            }
                        }
                    }
                }
            }
            is Map<*, *> -> {
                // defensive: top-level outliner may contain maps rather than typed Outliner
                val uuid = node["uuid"] as? String
                val children = node["children"]
                if (uuid != null) {
                    groupParent[uuid] = parentGroupUuid
                    if (children is List<*>) {
                        children.forEach { child -> if (child != null) processOutlinerNode(child, uuid) }
                    }
                }
            }
        }
    }

    this.outliner.forEach { top ->
        processOutlinerNode(top, null)
    }

    fun sizeFrom(from: Vec3, to: Vec3): Vec3 {
        return Vec3(
            to.x - from.x,
            to.y - from.y,
            to.z - from.z
        )
    }

    fun sub(a: Vec3, b: Vec3) = Vec3(a.x - b.x, a.y - b.y, a.z - b.z)

    fun faceUvFromRaw(face: UvData): GeoUvData {
        val arr = face.uv.map { it.toDouble() }
        val u1 = arr[0]; val v1 = arr[1]; val u2 = arr[2]; val v2 = arr[3]
        val w = u2 - u1
        val h = v2 - v1
        return GeoUvData(uv = listOf(u1, v1), uvSize = listOf(w, h))
    }

    val elementsByParent = this.elements.groupBy { elementParent[it.uuid] }

    val bones = this.groups.map { group ->
        val pivot = group.origin

        val childrenElements = elementsByParent[group.uuid] ?: emptyList()

        val cubes = childrenElements.map { elem ->
            val from = elem.from
            val to = elem.to
            val size = sizeFrom(from, to)
            val originAbsolute = from
            val origin = if (makeOriginRelative) sub(originAbsolute, pivot) else originAbsolute

            val uvMap = elem.faces.mapValues { (_, uvData) -> faceUvFromRaw(uvData) }

            GeoCube(origin = origin, size = size, uv = uvMap)
        }

        val parentGroupUuid = groupParent[group.uuid]
        val parentName = parentGroupUuid?.let { groupsByUuid[it]?.name }

        GeoBone(
            name = group.name,
            parent = parentName ?: "",
            pivot = pivot,
            cubes = cubes
        )
    }

    return GeoModel(listOf(GeoGeometry(
        GeoGeometryDescription(
            "geometry.${this.modelIdentifier}",
            this.resolution.width,
            this.resolution.height,
            this.visibleBox[0],
            this.visibleBox[1],
            listOf(0.0, this.visibleBox[2], 0.0),
        ),
        bones
    )), "1.12.0")
}
fun BlockBenchModel.geoAnim() : GeoAnimation {
    val anim = GeoAnimation(
        "1.8.0",
        this.animations.transformMap { animations1 ->
            val bones = mutableMapOf<String, GeoAnimated>()
            for ((_, animated) in animations1.animators) {
                if (animated.type != "bone") continue
                if (animated.keyframes.isNullOrEmpty()) continue
                val position = mutableMapOf<String, GeoAnimatedData>()
                val rotation = mutableMapOf<String, GeoAnimatedData>()
                val scale = mutableMapOf<String, GeoAnimatedData>()
                for (keyframe in animated.keyframes) {
                    when (keyframe.channel.lowercase()) {
                        "position" -> {
                            position[keyframe.time.toString()] =
                                GeoAnimatedData(
                                    keyframe.interpolation,
                                    null,
                                    GeoTransformVector(
                                        listOf(
                                            keyframe.dataPoints[0]["x"] ?: "",
                                            keyframe.dataPoints[0]["y"] ?: "",
                                            keyframe.dataPoints[0]["z"] ?: ""
                                        )
                                    )
                                )
                        }
                        "rotation" -> {
                            rotation[keyframe.time.toString()] =
                                GeoAnimatedData(
                                    keyframe.interpolation,
                                    null,
                                    GeoTransformVector(
                                        listOf(
                                            keyframe.dataPoints[0]["x"] ?: "",
                                            keyframe.dataPoints[0]["y"] ?: "",
                                            keyframe.dataPoints[0]["z"] ?: ""
                                        )
                                    )
                                )
                        }
                        "scale" -> {
                            scale[keyframe.time.toString()] =
                                GeoAnimatedData(
                                    keyframe.interpolation,
                                    null,
                                    GeoTransformVector(
                                        listOf(
                                            keyframe.dataPoints[0]["x"] ?: "",
                                            keyframe.dataPoints[0]["y"] ?: "",
                                            keyframe.dataPoints[0]["z"] ?: ""
                                        )
                                    )
                                )
                        }
                    }
                }
                bones[animated.name] = GeoAnimated(
                    position,
                    rotation,
                    scale
                )
            }
            animations1.name to GeoAnimationData(
                animations1.loop == "loop",
                animations1.length,
                bones
            )
        }
    )
    return anim
}
fun BlockBenchModel.resolveTextures() : List<ResolvedTexture> {
    val resolvedTextures = mutableListOf<ResolvedTexture>()
    fun decodeBase64Image(source: String): ByteArray {
        val clean = source.substringAfter("base64,", source)
        return Base64.getDecoder().decode(clean)
    }
    fun textureToBufferedImage(texture: Texture): BufferedImage {
        val bytes = decodeBase64Image(texture.source)
        return ImageIO.read(ByteArrayInputStream(bytes))
            ?: error("Invalid image data for texture ${texture.id}")
    }
    for (texture in this.textures) {
        val isAnimated = texture.height/texture.uvHeight > texture.width/texture.uvWidth
        var mcMeta: McMeta? = null
        if (isAnimated) {
            val frames =
                if (texture.frameOrderType == "custom")
                    texture.frameOrder.split(",").map { it.trim().toInt() }
                else
                    null
            mcMeta = McMeta(
                AnimationMeta(
                    texture.frameTime,
                    texture.frameInterpolate,
                    frames
                )
            )
        }
        resolvedTextures.add(
            ResolvedTexture(
            textureToBufferedImage(texture),
            mcMeta
        )
        )
    }
    return resolvedTextures
}
fun BlockBenchModel.saveAt(path: Path) {
    val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = false
    }

    Files.createDirectories(path)
    val file = path.resolve("${this.modelIdentifier}.bbmodel")
    Files.writeString(file, json.encodeToString(this))
}

private fun <E, T> List<E>.transformMap(transformer: (E) -> Pair<String, T>): Map<String, T> {
    val map = mutableMapOf<String, T>()
    for (thing in this) {
        val pair = transformer.invoke(thing)
        map[pair.first] = pair.second
    }
    return map
}

object BBConverter {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowStructuredMapKeys = true
    }

    fun loadBBModel(file: File): BlockBenchModel? {
        return produceBBModel(
            file.inputStream()
                .bufferedReader()
                .use { it.readText() }
        )
    }

    private fun produceBBModel(data: String): BlockBenchModel? {
        try {
            val bbModel = json.decodeFromString<BlockBenchModel>(data)
            return bbModel
        }
        catch (e: SerializationException) {
            println("An error occurred while trying to parse the block bench model...")
            e.printStackTrace()
            return null
        }
        catch (e: IllegalArgumentException) {
            println("The parsed object is not a BlockBenchModel...")
            e.printStackTrace()
            return null
        }
    }
}