package com.george.view_models

import android.util.Log
import androidx.lifecycle.ViewModel

class SearchFragmentNavigationViewModel : ViewModel() {

    private var _currentList: ArrayList<String>
    val currentList: ArrayList<String>
        get() = _currentList

    private var _stringOfEditText: String
    val stringOfEditText: String
        get() = _stringOfEditText

    init {
        _currentList = ArrayList()
        _stringOfEditText = String()
    }

    fun setList(list: ArrayList<String>) {
        _currentList = list
    }

    fun setStringOfEditText(string: String) {
        _stringOfEditText = string
    }

}