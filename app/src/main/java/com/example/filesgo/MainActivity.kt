package com.example.filesgo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.filesgo.viewModel.FileSearchViewModel
import com.example.filesgo.repository.FileRepository
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fileRepository = FileRepository(contentResolver)
        val fileSearchViewModel = FileSearchViewModel(fileRepository)
        fileSearchViewModel.loadFilesFromDevice()
    }
}