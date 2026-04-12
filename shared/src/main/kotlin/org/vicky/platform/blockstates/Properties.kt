package org.vicky.platform.blockstates

import org.vicky.platform.utils.Axis
import org.vicky.platform.utils.Direction
import org.vicky.platform.utils.defaultproperties.BooleanProperty
import org.vicky.platform.utils.defaultproperties.EnumProperty
import org.vicky.platform.utils.defaultproperties.IntegerProperty

enum class DoubleBlockHalf {
    UPPER,
    LOWER
}

object Properties {
    val HALF: EnumProperty<DoubleBlockHalf> =
        EnumProperty("half", DoubleBlockHalf::class.java)
    val FACING: EnumProperty<Direction> =
        EnumProperty("facing", Direction::class.java)
    val AXIS: EnumProperty<Axis> =
        EnumProperty("axis", Axis::class.java)

    val SNOW_LAYERS: IntegerProperty =
        IntegerProperty("layers", 1, 8)

    val WATERLOGGED: BooleanProperty = BooleanProperty("waterlogged")
    val OPEN: BooleanProperty = BooleanProperty("open")
    val POWERED: BooleanProperty = BooleanProperty("powered")
    val PERSISTENT: BooleanProperty = BooleanProperty("persistent")
}