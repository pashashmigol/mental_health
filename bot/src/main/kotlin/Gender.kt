import storage.R

enum class Gender {
    Male, Female;

    val title:String
        get() = when(this){
            Male -> R.string("male")
            Female -> R.string("female")
        }

    companion object{
        val names = values().map { it.name }
    }
}