package com.yelldev.dwij.android.entitis

import android.graphics.Bitmap
import com.yelldev.dwij.android.yMediaStore

interface yEntity {
    suspend fun getImage(fClient: yMediaStore): Bitmap?
    abstract fun getTitle(): String
    abstract fun getInfo(): String
    abstract fun getLink(): String
}