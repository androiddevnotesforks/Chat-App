package com.devwarex.chatapp.util

object Timeout {

    private const val ttl = 86_400_000
    fun isTimeout(currentTime: Long,savedTime: Long): Boolean = (currentTime - savedTime) > ttl
}