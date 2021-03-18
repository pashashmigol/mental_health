package storage

import com.google.firebase.database.*


class Users(database: FirebaseDatabase) {

    private val usersRef: DatabaseReference = database.reference.child("users")

    @Volatile
    private var users = mapOf<String, User>()

    init {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                users = parseToMap(snapshot)
                println("onDataChange(): $users")
            }

            override fun onCancelled(error: DatabaseError?) {
                println("onCancelled(): $error")
            }
        })
    }

    private fun parseToMap(snapshot: DataSnapshot?) = snapshot!!.children.map {
        val user = it.getValue(User::class.java)
        Pair(it.key, user)
    }.toMap()

    fun add(user: User) {
        usersRef.push()
        usersRef.child(user.id).setValue(user) { error: DatabaseError, ref: DatabaseReference ->
            println("onCancelled() ref: $ref, error: $error")
        }
    }

    fun get(id: String) = users[id]
}

data class User(
    val id: String
) {
    constructor() : this("")
}