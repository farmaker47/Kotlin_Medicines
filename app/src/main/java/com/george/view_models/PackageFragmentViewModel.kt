package com.george.view_models

import androidx.lifecycle.ViewModel

class PackageFragmentViewModel : ViewModel() {

    private var _stringOfHtml: String
    val stringOfHtml: String
        get() = _stringOfHtml

    private var _stringCookies: String
    val stringCookies: String
        get() = _stringCookies

    init {
        _stringOfHtml = String()
        _stringCookies = String()
    }

    fun setStringOfHtml(string: String) {
        _stringOfHtml = string
    }

    fun setStringCookies(string: String) {
        _stringCookies = string
    }
}