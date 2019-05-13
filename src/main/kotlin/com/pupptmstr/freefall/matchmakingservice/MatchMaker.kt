package com.pupptmstr.freefall.matchmakingservice

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*
import kotlin.math.abs

class MatchMaker(channelToRead: Channel<Pair<Int, Int>>, channelToWrite: Channel<Triple<Int, Int, String>>) {

    private val map = TreeMap<Int, Int>()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            for (msg in channelToRead){
                //msg.second = lvl. Мапа сортируется по ключам, поэтому и берем лвл
                map[msg.second] = msg.first
                makeNewPairs(channelToWrite)
            }
        }
    }

    private suspend fun makeNewPairs(channelToWrite: Channel<Triple<Int, Int, String>>) {
        if (map.size > 1){
            for (i in 0 until map.size) {
                val it = map.entries.iterator()
                val playerNow = it.next()
                val playerNext = it.next()
                if (abs(playerNow.key - playerNext.key) < 2) {
                    val key = generateABattleKey(playerNow.key, playerNext.key, playerNow.value, playerNext.value)
                    map.remove(playerNow.key)
                    map.remove(playerNext.key)
                    channelToWrite.send(Triple(playerNow.value, playerNow.value, key))
                }
            }
        }
    }

    fun generateABattleKey(lvl1: Int, lvl2: Int, id1: Int, id2: Int) : String {
        return "battle-${lvl1.toByte()}-${lvl2.toByte()} ${id1.toByte()} ${id2.toByte()}"
    }
}