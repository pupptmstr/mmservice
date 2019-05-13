package com.pupptmstr.freefall.matchmakingservice

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import redis.clients.jedis.Jedis
import java.io.File

class Server(key: String) {

    private val successMessage = 0
    private val wrongKeyError = 1
    private val cancelMMError = 2
    private val unknownError = 3
    private val map = mutableMapOf<Int, ApplicationCall>()
    private val channelToRequest = Channel<Pair<Int, Int>>()
    private val channelToRespond = Channel<Triple<Int, Int, String>>()
    val matchMaker = MatchMaker(channelToRequest, channelToRespond)


    fun start() {
        embeddedServer(Netty, 9992) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                    serializeNulls()
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                for (msg in channelToRespond){
                    val redisWriting = async { writeToRedis(msg.first, msg.second, msg.third) }
                    val responding = async { respond(msg.first, msg.second, msg.third) }
                    awaitAll(redisWriting, responding)
                }
            }

            routing {
                post("/mm") {
                    val user = call.receive<UserModel>()
                    val file = File("tokens/${user.token}")
                    if (file.exists()) {
                        map[user.id] = call
                        channelToRequest.send(user.id to user.level)
                    } else {
                        call.respond(errorMessage(wrongKeyError))
                    }
                }

                post("/cancelMm") {
                    val user = call.receive<UserModel>()
                    val file = File("tokens/${user.token}")
                    if (file.exists()) {
                        map.remove(user.id)
                    } else {
                        call.respond(errorMessage(wrongKeyError))
                    }
                }
            }
        }.start(wait = true)
    }

    private suspend fun respond(id1: Int, id2: Int, key: String) {
        if(map.contains(id1) && map.contains(id2)) {
            map[id1]!!.respond(successMessage(key))
            map[id2]!!.respond(successMessage(key))
            map.remove(id1)
            map.remove(id2)
        }
    }

    private suspend fun writeToRedis(id1: Int, id2: Int, key: String) {
        val jedis = Jedis()
        jedis.set(key, id1.toString())
        jedis.set(key, id2.toString())
    }

    /**
     * Коды ошибок:
     * 1 - неверный ключ
     * 2 - отмена поиска
     * 3 - неизвестная ошибка
     * */
    private fun errorMessage(type: Int): RespondModel {
        return when (type) {
            wrongKeyError -> RespondModel(wrongKeyError, "Неверный ключ пользователя")
            cancelMMError -> RespondModel(cancelMMError, "Поиск был отменен")
            else -> RespondModel(unknownError, "Неизвестная ошибка. Упс, кажется что-то пошло не так")
        }
    }

    /**
     * Код успешного сообщения(Противник найден): 0
     * */
    private fun successMessage(battleKey: String): RespondModel {
        return RespondModel(successMessage, battleKey)
    }

}