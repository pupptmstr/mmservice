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
import java.util.*
import kotlin.Comparator
import kotlin.math.abs

class Server(key: String) {

    private val successMessage = 0
    private val wrongKeyError = 1
    private val cancelMMError = 2
    private val unknownError = 3
    private val mapId = mutableMapOf<Int, ApplicationCall>()
    private val channelToRequest = Channel<Pair<Int, Int>>()
    private val channelToRespond = Channel<Triple<Int, Int, String>>()
    private val map = TreeSet<Pair<Int, Int>>(Comparator<Pair<Int, Int>> { p1, p2 -> p1.first - p2.first })


    fun start() {
        embeddedServer(Netty, 9992) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                    serializeNulls()
                }
            }
            println("запустился сервер")

            CoroutineScope(Dispatchers.IO).launch {
                println("запустился отвечатель")
                for (msg in channelToRespond){
                    val redisWriting = async { writeToRedis(msg.first, msg.second, msg.third) }
                    val responding = async { respond(msg.first, msg.second, msg.third) }
                    awaitAll(redisWriting, responding)
                }
            }

            CoroutineScope(Dispatchers.Default).launch {
                println("запустился матч-мэйкер")
                for (msg in channelToRequest) {
                    println("прочитали сообщение из канала 1")
                    println("${msg.first} to ${msg.second}")
                    println(channelToRequest.isFull)
                    //msg.second = lvl. Мапа сортируется по ключам, поэтому и берем лвл
                    map.add(msg.second to msg.first)
                    println("добавили игрока в сэт")
                    makeNewPairs(channelToRespond)
                }
            }

            routing {
                post("/mm") {
                    println("Запрос пришел")
                    val user = call.receive<UserModel>()
                    val file = File("tokens/${user.token}")
                    if (file.exists()) {
                        println("файл существует")
                        mapId[user.id] = call
                        channelToRequest.send(user.id to user.level)
                        println("отправлено сообщение в канал 1")
                    } else {
                        call.respond(errorMessage(wrongKeyError))
                    }
                }

                post("/cancelMm") {
                    val user = call.receive<UserModel>()
                    val file = File("tokens/${user.token}")
                    if (file.exists()) {
                        mapId.remove(user.id)
                    } else {
                        call.respond(errorMessage(wrongKeyError))
                    }
                }
            }
        }.start(wait = true)
    }

    private suspend fun respond(id1: Int, id2: Int, key: String) {
        if (mapId.contains(id1) && mapId.contains(id2)) {
            println("начинаю отвечать после нахождения соперника")
            mapId[id1]!!.respond(successMessage(key))
            mapId[id2]!!.respond(successMessage(key))
            mapId.remove(id1)
            mapId.remove(id2)
        }
    }

    private suspend fun writeToRedis(id1: Int, id2: Int, key: String) {
        println("пишу в редис")
        val jedis = Jedis()
        jedis.set(key, id1.toString())
        jedis.set(key, id2.toString())
        println("записал в редис")
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

    private suspend fun makeNewPairs(channelToWrite: Channel<Triple<Int, Int, String>>) {
        println("запустился подбор соперника")
        if (map.size > 1) {
            println("размер подходящий")
            val list = map.toMutableList()
            for (i in 0 until map.size) {
                val playerNow = list[i]
                val playerNext = list[i + 1]
                if (abs(playerNow.first - playerNext.first) < 2) {
                    val key = generateABattleKey(playerNow.first, playerNext.first, playerNow.second, playerNext.second)
                    map.remove(playerNow)
                    map.remove(playerNext)
                    list.remove(playerNow)
                    list.remove(playerNext)
                    channelToWrite.send(Triple(playerNow.second, playerNext.second, key))
                }
            }
        }
    }

    private fun generateABattleKey(lvl1: Int, lvl2: Int, id1: Int, id2: Int): String {
        println("сочиняю ключ")
        return "battle-${lvl1.toByte()}-${lvl2.toByte()} ${id1.toByte()} ${id2.toByte()}"
    }


}