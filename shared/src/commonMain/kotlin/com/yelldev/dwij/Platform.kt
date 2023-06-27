package com.yelldev.dwij

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform