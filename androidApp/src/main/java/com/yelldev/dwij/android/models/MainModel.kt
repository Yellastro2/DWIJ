package com.yelldev.dwij.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yelldev.yandexmusiclib.yClient


class MainModel: ViewModel() {

	var data: MutableLiveData<yClient>? = null
}