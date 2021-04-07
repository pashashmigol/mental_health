sealed class Result<out T> {
    class Success<T>(val data: T) : Result<T>()
    class Error(val message: String) : Result<Nothing>()

    inline fun dealWithError(onError: (Error) -> Nothing): T {
        when (this) {
            is Success -> return data
            is Error -> {
                onError(this)
            }
        }
    }

    override fun toString(): String {
        return when(this){
            is Success -> "Success(${data})"
            is Error ->  "Error(${message})"
        }
    }
}