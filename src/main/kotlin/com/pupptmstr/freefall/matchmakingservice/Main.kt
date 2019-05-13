package com.pupptmstr.freefall.matchmakingservice

import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * @author pupptmstr
 * */

val settings = File("settings.txt").readLines()
val KEY = settings[2].split(" : ")[1]

fun main() = runBlocking {
    val mainServer = Server(KEY)
    mainServer.start()
}