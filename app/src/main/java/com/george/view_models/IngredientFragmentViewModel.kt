package com.george.view_models

import androidx.lifecycle.ViewModel

class IngredientFragmentViewModel : ViewModel() {

    private var _stringOfHeader: String
    val stringOfHeader: String
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
        _stringOfHeader = String()
    }

    fun setStringText(string: String) {
        _stringOfText = string
    }

    fun setStringOfImage(string: String) {
        _stringOfImage = string
    }

    fun setStringOfHeader(string: String) {
        _stringOfHeader = string
    }
}