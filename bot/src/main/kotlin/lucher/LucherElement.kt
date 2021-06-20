package lucher


sealed class LucherElement {
    abstract fun same(to: LucherElement): Boolean
    abstract fun addAttribute(attribute: String): LucherElement

    data class Single(
        val color: AttributedColor
    ) : LucherElement() {
        constructor(colorIndex: String) : this(AttributedColor(LucherColor.of(colorIndex)))

        override fun same(to: LucherElement) = when (to) {
            is Single -> color == to.color
            else -> false
        }

        override fun addAttribute(attribute: String) = copy(color = color.copy(attribute = attribute))

        override fun toString() = "$color"
    }

    data class Pair(
        val firstColor: AttributedColor,
        val secondColor: AttributedColor
    ) : LucherElement() {
        constructor(firstColorIndex: String, secondColorIndex: String) : this(
            AttributedColor(LucherColor.of(firstColorIndex)),
            AttributedColor(LucherColor.of(secondColorIndex))
        )

        override fun same(to: LucherElement) = when (to) {
            is Pair -> setOf(firstColor, secondColor) == setOf(to.firstColor, to.secondColor)
            else -> false
        }

        override fun addAttribute(attribute: String): LucherElement = copy(
            firstColor = firstColor.copy(attribute = attribute),
            secondColor = secondColor.copy(attribute = attribute),
        )

        fun sameColors(other: Pair): Boolean =
            setOf(firstColor.color, secondColor.color) == setOf(other.firstColor.color, other.secondColor.color)

        override fun toString(): String = "$firstColor$secondColor"
    }
}

data class AttributedColor(val color: LucherColor, val attribute: String? = null) {
    constructor(colorIndex: String, attribute: String? = null)
            : this(
        LucherColor.of(colorIndex),
        attribute
    )

    override fun toString(): String = "${attribute ?: "_"}${color.index}"
}