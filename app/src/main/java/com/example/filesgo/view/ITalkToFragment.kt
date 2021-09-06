package com.example.filesgo.view

import com.example.filesgo.model.FileData

interface ITalkToFragment {
    fun onItemClicked(fileData: FileData)
}