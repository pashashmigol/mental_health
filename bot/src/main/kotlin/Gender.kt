enum class Gender(val option: Int) {
    Male(0), Female(1);

    companion object {
        private val VALUES = values()
        fun byValue(value: Int) = VALUES.firstOrNull { it.option == value } ?: Male
    }
}