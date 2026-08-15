package org.vicky.gradle.blockbench

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import org.vicky.gradle.Utils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

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

        if (value.baked) {
            output.encodeJsonElement(output.json.encodeToJsonElement(value.pre!!.vector))
        }
        else {
            val obj = buildJsonObject {
                if (value.lerpMode == "linear" || value.lerpMode == "bezier" || value.lerpMode == "") {
                    put("vector", output.json.encodeToJsonElement(value.pre!!.vector))
                } else {
                    put("lerp_mode", JsonPrimitive(value.lerpMode))
                    if (value.pre != null && value.post == null || (value.pre == value.post)) {
                        put("post", output.json.encodeToJsonElement(value.pre))
                    } else {
                        value.pre?.let {
                            put("pre", output.json.encodeToJsonElement(it))
                        }
                        value.post?.let {
                            put("post", output.json.encodeToJsonElement(it))
                        }
                    }
                }

                value.easing?.let {
                    put("easing", output.json.encodeToJsonElement(it))
                }
            }

            output.encodeJsonElement(obj)
        }
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
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("Vec3 serializer only works with JSON")

        val r = value.round(4)

        jsonEncoder.encodeJsonElement(
            JsonArray(
                listOf(
                    JsonPrimitive(r.x),
                    JsonPrimitive(r.y),
                    JsonPrimitive(r.z)
                )
            )
        )
    }

    override fun deserialize(decoder: Decoder): Vec3 {
        val input = decoder as? JsonDecoder ?: error("Vec3Serializer only supports JSON")
        return when (val elem = input.decodeJsonElement()) {
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
                    Vec3(
                        parts[0].toDoubleOrNull() ?: error("Invalid number ${parts[0]}"),
                        parts[1].toDoubleOrNull() ?: error("Invalid number ${parts[1]}"),
                        parts[2].toDoubleOrNull() ?: error("Invalid number ${parts[2]}")
                    )
                } else {
                    error("Invalid Vec3 JSON primitive: $elem")
                }
            }

            else -> error("Invalid Vec3 JSON: $elem")
        }
    }
}

object InlineVec2Serializer : KSerializer<List<Double>> {
    override val descriptor =
        PrimitiveSerialDescriptor("InlineVec2", PrimitiveKind.STRING)

    private fun Double.formatTrimZero(): Number =
        if (this % 1.0 == 0.0) this.toInt() else this

    override fun serialize(encoder: Encoder, value: List<Double>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("Vec3 serializer only works with JSON")
        require(value.size == 2) { "Expected Vec2 (size 2), got ${value.size}" }
        jsonEncoder.encodeJsonElement(
            JsonArray(
                listOf(
                    JsonPrimitive(value[0].formatTrimZero()),
                    JsonPrimitive(value[1].formatTrimZero())
                )
            )
        )
    }

    override fun deserialize(decoder: Decoder): List<Double> {
        val s = decoder.decodeString()
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim().toDouble() }

        require(s.size == 2)
        return s
    }
}

object StringVec3Serializer : KSerializer<StringVec3> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vec3") {
        element<String>("x")
        element<String>("y")
        element<String>("z")
    }

    private fun String.clean(): String = this.replace(Regex("[\r\n]+$"), "").trim()
    private fun Double.round(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return kotlin.math.round(this * factor) / factor
    }
    private fun String.isDefinitelyZeroExpression(): Boolean {
        val s = clean()

        // Simple zero
        if (s == "0" || s == "-0" || s == "+0") return true

        // 0 * anything
        if (Regex("""^[+-]?\(?\s*0(?:\.0+)?\s*\*""").containsMatchIn(s)) {
            return true
        }

        return false
    }
    private fun Double.ifZeroAbs(): Double {
        return if (abs(this) == 0.0) 0.0 else this
    }
    private fun Double.ifZeroAbsElse(action: (Double) -> Double): Double {
        return if (abs(this) == 0.0) 0.0 else action.invoke(this)
    }
    val epsilon = 1e-9 // Tolerance for precision errors
    fun Double.isEquivalentToInteger(): Boolean {
        val rounded = round(this)
        return abs(this - rounded) < epsilon
    }

    /**
     * Evaluates basic mathematical expressions (supports +, -, *, /, unary minus, and parentheses).
     * Returns null if the string is not a valid expression or number.
     */
    private fun String.evalDoubleOrNull(): Double? {
        try {
            val cleaned = this.replace("\\s+".toRegex(), "")
            if (cleaned.isEmpty()) return null

            class Parser(val s: String) {
                var pos = 0
                val peek: Char? get() = if (pos < s.length) s[pos] else null
                fun next(): Char? = if (pos < s.length) s[pos++] else null

                fun parse(): Double {
                    val res = parseExpression()
                    if (pos < s.length) throw RuntimeException("Unexpected character")
                    return res
                }

                // expression = term (( "+" | "-" ) term)*
                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        when (peek) {
                            '+' -> {
                                next(); x += parseTerm()
                            }

                            '-' -> {
                                next(); x -= parseTerm()
                            }

                            else -> return x
                        }
                    }
                }

                // term = factor (( "*" | "/" ) factor)*
                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        when (peek) {
                            '*' -> {
                                next(); x *= parseFactor()
                            }

                            '/' -> {
                                next()
                                val divisor = parseFactor()
                                if (divisor == 0.0) throw ArithmeticException("Division by zero")
                                x /= divisor
                            }

                            else -> return x
                        }
                    }
                }

                // factor = "+" factor | "-" factor | number | "(" expression ")"
                fun parseFactor(): Double {
                    if (peek == '+') {
                        next()
                        return parseFactor()
                    }
                    if (peek == '-') {
                        next()
                        return -parseFactor()
                    }

                    if (peek == '(') {
                        next()
                        val x = parseExpression()
                        if (next() != ')') throw RuntimeException("Missing closing parenthesis")
                        return x
                    }

                    val start = pos
                    while (peek != null && (peek!!.isDigit() || peek == '.')) {
                        next()
                    }
                    if (start == pos) throw RuntimeException("Expected number")
                    return s.substring(start, pos).toDoubleOrNull() ?: throw RuntimeException("Invalid number")
                }
            }

            return Parser(cleaned).parse()
        } catch (e: Exception) {
            return null
        }
    }

    private fun String.toPossibleDoubleJsonPrimitive(): JsonPrimitive {
        val cleanedStr = this.clean()
        if (cleanedStr.isBlank()) {
            return JsonPrimitive(0)
        }

        if (isTernaryExpression(cleanedStr)) {
            return JsonPrimitive(cleanedStr)
        }

        if (cleanedStr.isDefinitelyZeroExpression()) {
            return JsonPrimitive(0)
        }

        // Try evaluating as an expression or number first
        val evaluated = cleanedStr.evalDoubleOrNull()
        return if (evaluated != null && !evaluated.isNaN() && !evaluated.isInfinite()) {
            if (evaluated.isEquivalentToInteger()) {
                JsonPrimitive(evaluated.toInt())
            }
            else
                JsonPrimitive(evaluated.ifZeroAbsElse { num -> num.round(4) })
        } else {
            JsonPrimitive(cleanedStr)
        }
    }

    private fun JsonElement.toStringLenient(): String =
        this.jsonPrimitive.let { prim ->
            prim.contentOrNull?.trim()
                ?: prim.content.trim()
        }

    override fun serialize(encoder: Encoder, value: StringVec3) {
        val out = encoder as? JsonEncoder ?: error("StringVec3Serializer only supports JSON")
        val arr = buildJsonArray {
            add(value.x.toPossibleDoubleJsonPrimitive())
            add(value.y.toPossibleDoubleJsonPrimitive())
            add(value.z.toPossibleDoubleJsonPrimitive())
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
    val post: GeoTransformVector? = null,
    val easing: String? = null,
    val baked: Boolean = false
)

@Serializable
data class GeoTransformVector(
    val vector: StringVec3
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
    @SerialName("visible_bounds_width") val visibleBoundsWidth: Double,
    @SerialName("visible_bounds_height") val visibleBoundsHeight: Double,
    @SerialName("visible_bounds_offset") val visibleBoundsOffset: List<Double>
)

@Serializable
data class GeoBone @OptIn(ExperimentalSerializationApi::class) constructor(
    val name: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val parent: String = "",
    val pivot: Vec3,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val rotation: Vec3? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val cubes: List<GeoCube> = emptyList(),
    @EncodeDefault(EncodeDefault.Mode.NEVER) val locators: Map<String, Vec3> = emptyMap()
)

@Serializable
data class GeoCube @OptIn(ExperimentalSerializationApi::class) constructor(
    val origin: Vec3,
    val size: Vec3,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val rotation: Vec3? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val pivot: Vec3? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val uv: Map<String, GeoUvData> = emptyMap()
)

@Serializable
data class GeoLocator @OptIn(ExperimentalSerializationApi::class) constructor(
    val position: Vec3
)

@Serializable
data class GeoUvData(
    @Serializable(with = InlineVec2Serializer::class)
    val uv: List<Double>,
    @Serializable(with = InlineVec2Serializer::class)
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
    val display: BlockbenchDisplay? = null,
    @SerialName("animation_variable_placeholders") val animationVariablePlaceholders: String = "",
)

@Serializable
data class BlockbenchDisplay(
    val thirdperson_righthand: BlockbenchDisplayTransform? = null,
    val thirdperson_lefthand: BlockbenchDisplayTransform? = null,
    val firstperson_righthand: BlockbenchDisplayTransform? = null,
    val firstperson_lefthand: BlockbenchDisplayTransform? = null,
    val ground: BlockbenchDisplayTransform? = null,
    val gui: BlockbenchDisplayTransform? = null,
    val head: BlockbenchDisplayTransform? = null,
    val fixed: BlockbenchDisplayTransform? = null,
    val on_shelf: BlockbenchDisplayTransform? = null
)

@Serializable
data class BlockbenchDisplayTransform(
    val rotation: Vec3? = null,
    val translation: Vec3? = null,
    val scale: Vec3? = null
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
class Vec3 {
    val x: Double
    val y: Double
    val z: Double

    companion object {
        val ZERO: Vec3 = Vec3(0.0, 0.0, 0.0)
    }

    constructor(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(from: List<Double>) : this(
        Math.round(from[0] * 100.0) / 100.0, Math.round(from[1] * 100.0) / 100.0, Math.round(from[2] * 100.0) / 100.0
    ) {
    }

    fun toList(): List<Double> = listOf(x, y, z)
    override fun toString(): String = "[$x, $y, $z]"
    override fun equals(other: Any?): Boolean {
        if (other !is Vec3) return false
        return other.x == this.x && other.y == this.y && other.z == this.z
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

operator fun Vec3.plus(other: Vec3): Vec3 = Vec3(
    x + other.x,
    y + other.y,
    z + other.z
)

operator fun Vec3.div(magnitude: Double): Vec3 = Vec3(
    x / magnitude,
    y / magnitude,
    z / magnitude
)

operator fun Vec3.div(magnitude: Int): Vec3 = Vec3(
    x / magnitude,
    y / magnitude,
    z / magnitude
)

operator fun Vec3.minus(other: Vec3) = Vec3(
    x - other.x,
    y - other.y,
    z - other.z
)

operator fun Vec3.times(scale: Double) = Vec3(
    x * scale,
    y * scale,
    z * scale
)

operator fun Vec3.times(scale: Vec3) = Vec3(
    x * scale.x,
    y * scale.y,
    z * scale.z
)

fun Vec3.round(decimals: Int): Vec3 {
    val factor = 10.0.pow(decimals)
    return Vec3(
        kotlin.math.round(x * factor) / factor,
        kotlin.math.round(y * factor) / factor,
        kotlin.math.round(z * factor) / factor
    )
}

fun Vec3.isZero(): Boolean {
    val rounded = this.round(1)
    return rounded.x == 0.0 && rounded.y == 0.0 && rounded.z == 0.0
}

fun Vec3.ifIsZero(toUse: () -> Vec3): Vec3 {
    return if (this.isZero()) toUse.invoke() else this
}

fun Vec3.ifIsZeroDoing(toUse: () -> Vec3, whenNotZero: (Vec3) -> Vec3): Vec3 {
    return if (this.isZero()) toUse.invoke() else whenNotZero.invoke(this)
}

fun Vec3.ifIsZeroNullable(toUse: () -> Vec3?): Vec3? {
    return if (this.isZero()) toUse.invoke() else this
}

fun Vec3.ifIsZeroNullableDoing(toUse: () -> Vec3?, whenNotZero: (Vec3) -> Vec3): Vec3? {
    return if (this.isZero()) toUse.invoke() else whenNotZero.invoke(this)
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
    fun invert(): StringVec3 = StringVec3(
        x.isDoubleDoElse({ "-($it)" }) { -it },
        y.isDoubleDoElse({ "-($it)" }) { -it },
        z.isDoubleDoElse({ "-($it)" }) { -it }
    )

    fun invertYZ(): StringVec3 = StringVec3(
        x,
        y.isDoubleDoElse({ "-($it)" }) { -it },
        z.isDoubleDoElse({ "-($it)" }) { -it }
    )

    fun invertXY(): StringVec3 = StringVec3(
        x.isDoubleDoElse({ "-($it)" }) { -it },
        y.isDoubleDoElse({ "-($it)" }) { -it },
        z
    )

    fun invertXZ(): StringVec3 = StringVec3(
        x.isDoubleDoElse({ "-($it)" }) { -it },
        y,
        z.isDoubleDoElse({ "-($it)" }) { -it }
    )
}

fun String.isDoubleDoElse(stringAction: (String) -> String, action: (Double) -> Double): String {
    if (this.toDoubleOrNull() == null) return stringAction.invoke(this)
    return action.invoke(this.toDouble()).toString()
}

@Serializable
data class Element(
    val name: String,
    val type: String,
    val uuid: String,
    val from: Vec3 = Vec3.ZERO,
    val to: Vec3 = Vec3.ZERO,
    val origin: Vec3 = Vec3.ZERO,
    val rotation: Vec3 = Vec3.ZERO,
    val position: Vec3 = Vec3.ZERO,
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
    val snapping: Double,
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
    val easing: String? = null,
    @SerialName("data_points") val dataPoints: List<Map<String, String>>,
    @SerialName("bezier_linked") val bezierLinked: Boolean = false,
    @SerialName("bezier_left_time") val bezierLeftTime: List<Double> = listOf(),
    @SerialName("bezier_left_value") val bezierLeftValue: List<Double> = listOf(),
    @SerialName("bezier_right_time") val bezierRightTime: List<Double> = listOf(),
    @SerialName("bezier_right_value") val bezierRightValue: List<Double> = listOf(),
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

fun GeoCube.validate() {
    val faces = uv.keys

    val required = setOf("north", "south", "east", "west", "up", "down")
    require(faces.containsAll(required)) {
        "Missing faces: ${required - faces}"
    }
}

fun bbPosition(pos: Vec3) =
    Vec3(-pos.x, pos.y, pos.z)

fun bbRotation(rot: Vec3) =
    Vec3(-rot.x, -rot.y, rot.z)

fun bbOrigin(from: Vec3, to: Vec3): Vec3 {
    val size = to - from
    return Vec3(-from.x - size.x, from.y, from.z)
}

fun BlockBenchModel.geoGeom(): GeoModel {
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

    fun faceUvFromRaw(face: UvData, side: String): GeoUvData {
        val (u1, v1, u2, v2) = face.uv

        val u = min(u1, u2)
        val v = min(v1, v2)

        val w = abs(u2 - u1)
        val h = abs(v2 - v1)

        return when (side) {
            "down" -> GeoUvData(
                uv = listOf(u, v + h),
                uvSize = listOf(w, -h)
            )

            else -> GeoUvData(
                uv = listOf(u, v),
                uvSize = listOf(w, h)
            )
        }
    }

    val elementsByParent = this.elements.groupBy { elementParent[it.uuid] }

    val bones = this.groups.map { group ->
        val childrenElements = elementsByParent[group.uuid] ?: emptyList()

        val cubes: List<GeoCube> = childrenElements.mapNotNull cynrax@{ elem ->
            if (elem.type != "cube") return@cynrax null
            val uvMap = elem.faces.mapValues { (side, uvData) -> faceUvFromRaw(side = side, face = uvData) }

            val cube = GeoCube(
                origin = bbOrigin(elem.from, elem.to), // [FIXED] Removed the subtraction of group.origin
                size = elem.to - elem.from,
                // inflate = elem.inflate ?: 0.0, // [ADDED] Required for Blockbench inflation

                rotation = elem.rotation
                    .ifIsZeroNullableDoing({ null }) { bbRotation(it) },

                uv = uvMap.ifEmpty { emptyMap() },

                pivot = elem.rotation.ifIsZeroNullableDoing(
                    { null }
                ) { bbPosition(elem.origin) }
            )
            try {
                cube.validate()
                return@cynrax cube
            } catch (e: Exception) {
                Utils.log("Encountered error while on cube: ${elem.uuid}, it will be skipped", true)
                e.printStackTrace()
                return@cynrax null
            }
        }

        val locators: Map<String, Vec3> = childrenElements.transformMapNotNull cynrax@{ elem ->
            if (elem.type != "locator") return@cynrax null
            elem.name to bbPosition(elem.position)
        }

        val parentGroupUuid = groupParent[group.uuid]
        val parentName = parentGroupUuid?.let { groupsByUuid[it]?.name }

        GeoBone(
            name = group.name,
            parent = parentName ?: "",
            pivot = bbPosition(group.origin),

            rotation = group.rotation
                .ifIsZeroNullableDoing({ null }) { bbRotation(it) },

            cubes = cubes.ifEmpty { emptyList() },

            locators = locators.ifEmpty { emptyMap() }
        )
    }

    return GeoModel(
        listOf(
            GeoGeometry(
                GeoGeometryDescription(
                    "geometry.${this.modelIdentifier}",
                    this.resolution.width,
                    this.resolution.height,
                    this.visibleBox[0],
                    this.visibleBox[1],
                    listOf(0.0, this.visibleBox[2], 0.0),
                ),
                bones
            )
        ), "1.12.0"
    )
}

fun isTernaryExpression(value: String): Boolean {
    var depth = 0
    var hasQuestion = false

    for (char in value) {
        when (char) {
            '(' -> depth++
            ')' -> depth--

            '?' -> {
                if (depth == 0) {
                    hasQuestion = true
                }
            }

            ':' -> {
                if (depth == 0 && hasQuestion) {
                    return true
                }
            }
        }
    }

    return false
}

fun invertExpression(value: String?): String {
    if (value == null) return "0"

    val v = value.trim()

    if (v == "0" || v.isEmpty())
        return "0"

    if (isTernaryExpression(v))
        return v

    if (v.startsWith("(")) {
        return "-1 * $v"
    }

    return if (v.startsWith("-")) {
        v.removePrefix("-")
    } else {
        "-$v"
    }
}

fun BlockBenchModel.geoAnim(): GeoAnimation {

    fun bbPosition(x: String?, y: String?, z: String?): StringVec3 {
        return StringVec3(
            invertExpression(x ?: "0"),
            y ?: "0",
            z ?: "0"
        )
    }

    fun bbRotation(x: String?, y: String?, z: String?): StringVec3 {
        return StringVec3(
            invertExpression(x ?: "0"),
            invertExpression(y ?: "0"),
            z ?: "0"
        )
    }

    fun bbScale(x: String?, y: String?, z: String?): StringVec3 {
        return StringVec3(
            x ?: "0",
            y ?: "0",
            z ?: "0"
        )
    }

    return GeoAnimation(
        "1.8.0",
        this.animations.transformMap { animation ->

            val bones = mutableMapOf<String, GeoAnimated>()

            for ((_, animated) in animation.animators) {

                if (animated.type != "bone")
                    continue

                if (animated.keyframes.isNullOrEmpty())
                    continue

                val position = convertChannel(
                    animated.keyframes,
                    animation.snapping,
                    "position"
                )

                val rotation = convertChannel(
                    animated.keyframes,
                    animation.snapping,
                    "rotation"
                )

                val scale = convertChannel(
                    animated.keyframes,
                    animation.snapping,
                    "scale"
                )

                bones[animated.name] = GeoAnimated(
                    position,
                    rotation,
                    scale
                )
            }

            animation.name to GeoAnimationData(
                animation.loop == "loop",
                animation.length,
                bones
            )
        }
    )
}
fun Double.formatTime(): String {
    return BigDecimal(this)
        .setScale(4, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

fun BlockBenchModel.resolveTextures(): List<ResolvedTexture> {
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
        val isAnimated = texture.height / texture.uvHeight > texture.width / texture.uvWidth
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

private data class BezierPoint(
    val time: Double,
    val value: Double
)
private fun cubicBezier(
    p0: BezierPoint,
    p1: BezierPoint,
    p2: BezierPoint,
    p3: BezierPoint,
    t: Double
): BezierPoint {
    val u = 1.0 - t

    val uu = u * u
    val tt = t * t

    val uuu = uu * u
    val ttt = tt * t

    return BezierPoint(
        time =
            uuu * p0.time +
                    3.0 * uu * t * p1.time +
                    3.0 * u * tt * p2.time +
                    ttt * p3.time,

        value =
            uuu * p0.value +
                    3.0 * uu * t * p1.value +
                    3.0 * u * tt * p2.value +
                    ttt * p3.value
    )
}
private fun sampleBezier(
    beforeTime: Double,
    beforeValue: Double,
    beforeRightTime: Double,
    beforeRightValue: Double,
    afterTime: Double,
    afterValue: Double,
    afterLeftTime: Double,
    afterLeftValue: Double,
    alpha: Double
): Double {

    val timeGap = afterTime - beforeTime

    val rightTime =
        beforeRightTime
            .coerceIn(0.0, timeGap)

    val leftTime =
        afterLeftTime
            .coerceIn(-timeGap, 0.0)

    val p0 = BezierPoint(
        beforeTime,
        beforeValue
    )

    val p1 = BezierPoint(
        beforeTime + rightTime,
        beforeValue + beforeRightValue
    )

    val p2 = BezierPoint(
        afterTime + leftTime,
        afterValue + afterLeftValue
    )

    val p3 = BezierPoint(
        afterTime,
        afterValue
    )

    val targetTime =
        beforeTime +
                (afterTime - beforeTime) * alpha

    /*
     * Blockbench samples the cubic curve at 200 points.
     */
    val samples = 200

    var closest: BezierPoint? = null
    var closestDiff = Double.POSITIVE_INFINITY

    var secondClosest: BezierPoint? = null
    var secondClosestDiff = Double.POSITIVE_INFINITY

    for (i in 0..samples) {

        val t = i.toDouble() / samples

        val point = cubicBezier(
            p0,
            p1,
            p2,
            p3,
            t
        )

        val diff = abs(point.time - targetTime)

        if (diff < closestDiff) {
            secondClosest = closest
            secondClosestDiff = closestDiff

            closest = point
            closestDiff = diff
        }
        else if (diff < secondClosestDiff) {
            secondClosest = point
            secondClosestDiff = diff
        }
    }

    val a = closest ?: return beforeValue
    val b = secondClosest ?: return a.value

    val interpolation =
        if (abs(b.time - a.time) < 1e-12) {
            0.0
        }
        else {
            ((targetTime - a.time) / (b.time - a.time))
                .coerceIn(0.0, 1.0)
        }

    return a.value +
            (b.value - a.value) * interpolation
}
private fun bakeBezierPair(
    before: Keyframe,
    after: Keyframe,
    snapping: Double,
    channel: String,
    transform: (Double, Double, Double) -> GeoTransformVector
): List<Pair<Double, GeoTransformVector>> {

    val interval = 1.0 / snapping

    val result =
        mutableListOf<Pair<Double, GeoTransformVector>>()

    var time = before.time + interval

    while (time < after.time + interval / 2.0) {

        val alpha =
            (time - before.time) /
                    (after.time - before.time)

        val x = sampleBezier(
            before.time,
            before.dataPoints[0]["x"]!!.toDouble(),
            before.bezierRightTime[0],
            before.bezierRightValue[0],
            after.time,
            after.dataPoints[0]["x"]!!.toDouble(),
            after.bezierLeftTime[0],
            after.bezierLeftValue[0],
            alpha
        )

        val y = sampleBezier(
            before.time,
            before.dataPoints[0]["y"]!!.toDouble(),
            before.bezierRightTime[1],
            before.bezierRightValue[1],
            after.time,
            after.dataPoints[0]["y"]!!.toDouble(),
            after.bezierLeftTime[1],
            after.bezierLeftValue[1],
            alpha
        )

        val z = sampleBezier(
            before.time,
            before.dataPoints[0]["z"]!!.toDoubleOrNull() ?: 0.0,
            before.bezierRightTime[2],
            before.bezierRightValue[2],
            after.time,
            after.dataPoints[0]["z"]!!.toDoubleOrNull() ?: 0.0,
            after.bezierLeftTime[2],
            after.bezierLeftValue[2],
            alpha
        )

        result += time to transform(x, y, z)

        time += interval
    }

    return result
}
private fun convertChannel(
    keyframes: List<Keyframe>,
    snapping: Double,
    channel: String
): MutableMap<String, GeoAnimatedData> {

    val result = mutableMapOf<String, GeoAnimatedData>()

    val frames = keyframes
        .filter { it.channel.equals(channel, true) }
        .sortedBy { it.time }

    for (i in frames.indices) {

        val current = frames[i]
        val next = frames.getOrNull(i + 1)

        // 1. Always emit the authored keyframe.
        result[current.time.formatTime()] =
            convertKeyframe(current, channel)

        // 2. Bake Bezier interval.
        if (
            next != null &&
            (
                    current.interpolation.equals("bezier", true) ||
                            next.interpolation.equals("bezier", true)
                    )
        ) {

            val baked = bakeBezierPair(
                current,
                next,
                snapping,
                channel
            ) { x, y, z ->
                var x2 = x
                var y2 = y

                if (channel.equals("position", "rotation"))
                    x2 = -x2
                if (channel == "rotation")
                    y2 = -y2

                GeoTransformVector(StringVec3(x2.toString(), y2.toString(), z.toString()))
            }

            for ((time, vector) in baked) {
                result[time.formatTime()] =
                    GeoAnimatedData(
                        lerpMode = "linear",
                        pre = vector,
                        baked = true
                    )
            }
        }
    }

    return result
}
private fun convertKeyframe(
    keyframe: Keyframe,
    channel: String
): GeoAnimatedData {
    val point = keyframe.dataPoints
        .getOrNull(0)
    val point2 = keyframe.dataPoints
        .getOrNull(1)

    val x = point?.get("x")
    val y = point?.get("y")
    val z = point?.get("z")

    val x2 = point2?.get("x")
    val y2 = point2?.get("y")
    val z2 = point2?.get("z")

    return when (channel.lowercase()) {
        "position" -> {
            val vector = GeoTransformVector(
                StringVec3(
                    invertExpression(x ?: "0"),
                    y ?: "0",
                    z ?: "0")
            )
            val vector2 = if (point2 != null) GeoTransformVector(
                StringVec3(
                    invertExpression(x2 ?: "0"),
                    y2 ?: "0",
                    z2 ?: "0"
                )
            ) else null

            GeoAnimatedData(
                    keyframe.interpolation,
                    vector,
                    vector2 ?: vector,
                    keyframe.easing
                )
        }

        "rotation" -> {
            val vector = GeoTransformVector(StringVec3(
                invertExpression(x ?: "0"),
                invertExpression(y ?: "0"),
                z ?: "0")
            )
            val vector2 = if (point2 != null) GeoTransformVector(
                StringVec3(
                    invertExpression(x2 ?: "0"),
                    invertExpression(y2 ?: "0"),
                    z2 ?: "0"
                )
            ) else null

            GeoAnimatedData(
                    keyframe.interpolation,
                    vector,
                    vector2 ?: vector,
                    keyframe.easing
                )
        }

        "scale" -> {
            val vector = GeoTransformVector(StringVec3(x ?: "0", y ?: "0", z ?: "0"))
            val vector2 = if (point2 != null) GeoTransformVector(StringVec3(x2 ?: "0", y2 ?: "0", z2 ?: "0")) else null

            GeoAnimatedData(
                    keyframe.interpolation,
                    vector,
                    vector2 ?: vector,
                    keyframe.easing
                )
        }

        else -> throw RuntimeException()
    }
}

fun String.equals(vararg options : String) : Boolean {
    for (option in options)
        if (this == option)
            return true

    return false
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

private fun <E, T> List<E>.transformMapNotNull(transformer: (E) -> Pair<String, T>?): Map<String, T> {
    val map = mutableMapOf<String, T>()
    for (thing in this) {
        val pair = transformer.invoke(thing) ?: continue
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
        } catch (e: SerializationException) {
            println("An error occurred while trying to parse the block bench model...")
            e.printStackTrace()
            return null
        } catch (e: IllegalArgumentException) {
            println("The parsed object is not a BlockBenchModel...")
            e.printStackTrace()
            return null
        }
    }
}