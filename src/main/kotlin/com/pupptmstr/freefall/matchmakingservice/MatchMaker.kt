package com.pupptmstr.freefall.matchmakingservice

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*
import kotlin.Comparator
import kotlin.math.abs

class MatchMaker(channelToRead: Channel<Pair<Int, Int>>, channelToWrite: Channel<Triple<Int, Int, String>>) {

    private val map = TreeSet<Pair<Int, Int>>(Comparator<Pair<Int, Int>> { p1, p2 -> p1.first - p2.first })

    init {
        CoroutineScope(Dispatchers.Default).launch {
            for (msg in channelToRead) {
                //msg.second = lvl. Мапа сортируется по ключам, поэтому и берем лвл
                map.add(msg.second to msg.first)
                makeNewPairs(channelToWrite)
            }
        }
    }

    private suspend fun makeNewPairs(channelToWrite: Channel<Triple<Int, Int, String>>) {
        if (map.size > 1) {
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
        return "battle-${lvl1.toByte()}-${lvl2.toByte()} ${id1.toByte()} ${id2.toByte()}"
    }
}