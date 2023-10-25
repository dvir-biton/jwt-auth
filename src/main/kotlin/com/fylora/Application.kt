package com.fylora

import com.fylora.data.user.MongoUserDataSource
import com.fylora.plugins.*
import io.ktor.server.application.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("Unused")
fun Application.module() {
    val mongoPassword = System.getenv("MONGO_PASSWORD")
    val dbName = "ktor-login"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://fylora:$mongoPassword@users.q9zqet0.mongodb.net/$dbName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(dbName)
    val userDataSource = MongoUserDataSource(db)

    configureMonitoring()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
