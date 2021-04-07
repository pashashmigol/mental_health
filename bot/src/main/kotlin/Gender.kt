import storage.CentralDataStorage.string

enum class Gender {
    Male, Female;

    val title:String
        get() = when(this){
            Male -> string("male")
            Female -> string("female")
        }
}