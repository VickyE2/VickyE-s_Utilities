package org.vicky.platform.guiscreens

import net.kyori.adventure.text.Component
import org.vicky.platform.PlatformPlayer
import org.vicky.platform.items.textComponent

interface Positionable {
    fun getX(): Number
    fun getY(): Number
    fun getZ(): Number
}
data class GridPos(val x: Int, val y: Int, val z: Int = 0): Positionable {
    override fun getX(): Number = x
    override fun getY(): Number = y
    override fun getZ(): Number = z
}
data class AbsolutePos(val x: Double, val y: Double, val z: Int = 0): Positionable {
    override fun getX(): Number = x
    override fun getY(): Number = y
    override fun getZ(): Number = z
}

interface PlatformScreen {
    fun close()
}

data class GuiContext(
    val user: PlatformPlayer,
    val gui: PlatformScreen
)

open class Element(
    val gridPositionable: GridPos,
    val absolutePositionable: AbsolutePos,
    val width: Int = 1, val height: Int = 1,
    var visible: Boolean = true,
    var style: StyleKey = StyleKey.DEFAULT
)
enum class StyleKey {
    DEFAULT,
    PRIMARY,
    SECONDARY,
    DANGER,
}
open class Button(val tooltip: Component, pos: GridPos, absolutePos: AbsolutePos, width: Int = 1, height: Int = 1): Element(pos, absolutePos, width, height) {
    var onClick: (GuiContext) -> Unit = {}
}
class Slider(pos: GridPos, absolutePos: AbsolutePos, width: Int, val min: Int, val max: Int, val tooltip: Component = "".textComponent()): Element(pos, absolutePos, width, 1) {
    var value: Int = min
    var onChange: (Int) -> Unit = {}
}