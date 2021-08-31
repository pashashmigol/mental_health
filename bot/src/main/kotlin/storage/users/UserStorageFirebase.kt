package storage.users

import Result
import com.google.firebase.database.*
import kotlinx.coroutines.channels.Channel
import models.User
import java.util.concurrent.ConcurrentHashMap

private const val USERS = "users_info"


class UserStorageFirebase(database: FirebaseDatabase) : UserStorage {
    private val usersInfoRef: DatabaseReference = database.reference.child(USERS)

    @Volatile
    private var users = ConcurrentHashMap<Long, User>()

    init {
        usersInfoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot?) {
                users.clear()
                users.putAll(parseUsers(snapshot))

                println("onDataChange(): $users")
            }

            override fun onCancelled(error: DatabaseError?) {
                println("onCancelled(): $error")
            }
        })
    }

    override fun clear() {
        val resultChannel = Channel<Result<Unit>>(1)

        usersInfoRef
            .removeValue { error, _ ->
                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }
    }

    override fun getUser(userId: Long) = users[userId]

    override fun allUsers(): List<User> = users.values.toList()

    override fun hasUser(userId: Long) = users.containsKey(userId)

    override suspend fun saveUser(user: User): Result<Unit> {
        val resultChannel = Channel<Result<Unit>>(1)
        users[user.id] = user

        usersInfoRef
            .child(user.id.toString())
            .setValue(user) { error: DatabaseError?, ref: DatabaseReference ->
                println("add($user) ref: $ref, error: $error")

                val result = when (error == null) {
                    true -> Result.Success(Unit)
                    false -> Result.Error(error.details)
                }
                resultChannel.offer(result)
            }

        return resultChannel.receive()
    }

//    override suspend fun clearUser(user: User): Result<Unit> {
//        val resultChannel = Channel<Unit>(3)
//        mmpiAnswersRef
//            .child(user.id.toString())
//            .setValue(null) { error, ref ->
//                println("add($user) ref: $ref, error: $error")
//                resultChannel.offer(Unit)
//            }
//        lucherAnswersRef
//            .child(user.id.toString())
//            .setValue(null) { error, ref ->
//                println("add($user) ref: $ref, error: $error")
//                resultChannel.offer(Unit)
//            }
//        activeSessionsRef
//            .child(user.id.toString())
//            .setValue(null) { error, ref ->
//                println("add($user) ref: $ref, error: $error")
//                resultChannel.offer(Unit)
//            }
//
//        resultChannel.receive()
//        return Result.Success(Unit)
//    }
}