package com.fylora

import com.fylora.data.user.MongoUserDataSource
import com.fylora.data.user.User
import com.fylora.plugins.*
import io.ktor.server.application.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@OptIn(DelicateCoroutinesApi::class)
@Suppress("Unused")
fun Application.module() {
    val mongoPassword = System.getenv("MONGO_PASSWORD")
    val dbName = "ktor-login"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://fylora:$mongoPassword@users.q9zqet0.mongodb.net/$dbName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(dbName)
    val userDataSource = MongoUserDataSource(db)

    GlobalScope.launch {
        val user = User(
            "test",
            "test_password",
            "salt"
        )
        userDataSource.insertUser(user)
    }

    configureMonitoring()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
