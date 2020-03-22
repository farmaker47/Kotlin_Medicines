package com.george.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class IngredientFragmentViewModel : ViewModel() {

    private val _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word

    private var _stringOfHeader = MutableLiveData<String>()
    val stringOfHeader: LiveData<String>
        get() = _stringOfHeader

    private var _stringOfText: String
    val stringOfText: String
        get() = _stringOfText

    private var _stringOfImage: String
    val stringOfImage: String
        get() = _stringOfImage

    init {
        _stringOfText = String()
        _stringOfImage = String()
        _stringOfHeader = MutableLiveData<String>()
    }

    fun setStringText(string: String) {
        _stringOfText = string
    }

    fun setStringOfImage(string: String) {
        _stringOfImage = string
    }

    fun setStringOfHeader(string: MutableLiveData<String>) {
        _stringOfHeader = string
    }
}