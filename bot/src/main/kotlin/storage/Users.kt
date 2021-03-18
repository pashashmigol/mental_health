package storage

import com.google.firebase.database.*
import models.User


class Users(database: FirebaseDatabase) {
    private val usersRef: DatabaseReference = database.reference.child("users")

    @Volatile
    private var users = mapOf<Long, User>()

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
        Pair(user.id, user)
    }.toMap()

    fun get(id: Long) = users[id]

    fun allUsers(): List<User> = users.values.toList()

    fun add(user: User) {
        usersRef.push()
        usersRef.child(user.id.toString()).setValue(user) { error: DatabaseError, ref: DatabaseReference ->
            println("onCancelled() ref: $ref, error: $error")
        }
    }
}