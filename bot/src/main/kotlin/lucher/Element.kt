package lucher

import java.lang.IllegalArgumentException

sealed class Element {
//    abstract fun toString(prefix: String): String
    abstract fun same(to: Element): Boolean
    abstract fun addAttribute(attribute: String): Element

    data class Single(val color: AttributedColor) : Element() {
        constructor(colorIndex: String)
                : this(AttributedColor(LucherColor.of(colorIndex)))

//        override fun toString(prefix: String) = "$prefix$color"
        override fun same(to: Element) = when (to) {
            is Single -> color == to.color
            else -> false
        }

        override fun addAttribute(attribute: String): Element =
            copy(color = color.copy(attribute = attribute))

        companion object {
            fun parse(string: String): Element {
                return Single(
                    AttributedColor(
                        attribute = string[0].toString(),
                        color = LucherColor.of(string[1].toString())
                    )
                )
            }
        }


        override fun toString() = "$color"
    }

    data class Pair(
        val firstColor: AttributedColor,
        val secondColor: AttributedColor
    ) : Element() {
        constructor(firstColorIndex: String, secondColorIndex: String)
                : this(
            AttributedColor(LucherColor.of(firstColorIndex)),
            AttributedColor(LucherColor.of(secondColorIndex))
        )

//        override fun toString(prefix: String) = "$prefix$firstColor$prefix$secondColor"
        override fun same(to: Element) = when (to) {
            is Pair -> setOf(firstColor, secondColor) == setOf(to.firstColor, to.secondColor)
            else -> false
        }


        override fun addAttribute(attribute: String): Element =
            copy(
                firstColor = firstColor.copy(attribute = attribute),
                secondColor = secondColor.copy(attribute = attribute),
            )

        fun sameColors(other: Pair): Boolean =
            setOf(firstColor.color, secondColor.color) == setOf(other.firstColor.color, other.secondColor.color)

        override fun toString(): String = "$firstColor$secondColor"

        companion object {
            fun parse(string: String): Element {
                return Pair(
                    firstColor = AttributedColor(
                        attribute = string[0].toString(),
                        color = LucherColor.of(string[1].toString())
                    ),
                    secondColor = AttributedColor(
                        attribute = string[2].toString(),
                        color = LucherColor.of(string[3].toString())
                    ),
                )
            }
        }
    }

    companion object {
        fun parse(string: String): Element = when (string.length) {
            2 -> Single.parse(string)
            4 -> Pair.parse(string)
            else -> throw IllegalArgumentException()
        }
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