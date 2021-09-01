package com.example.filesgo.view

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.filesgo.MainActivity
import com.example.filesgo.R
import com.example.filesgo.model.Image
import com.example.filesgo.viewModel.FileSearchViewModel
import com.example.filesgo.viewModel.MyUIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File
import java.util.*


@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private lateinit var viewModel: FileSearchViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel
        lifecycleScope.launchWhenCreated {
            viewModel.uiDataFlow.collect { appState ->
                when (appState.uiState) {
                    is MyUIState.Failure -> {
                    }
                    MyUIState.Initial -> {
                    }
                    MyUIState.Loading -> {
                    }
                    is MyUIState.Success -> {
                        if (appState.imageDetails != null) {
                            val imageData = appState.imageDetails.fileType as Image
                            val date = Date(imageData.dateCreated.toLong()).toString()
                            view.findViewById<TextView>(R.id.date_added_value).text = date
                            val uri = "file://" + (appState.imageDetails.path)
                            val file = File(appState.imageDetails.path)
                            val imageView = view.findViewById<ImageView>(R.id.imageview)
                            if (file.exists()) {
                                Glide.with(this@DetailsFragment)
                                    .load(uri)
                                    .centerInside()
                                    .into(imageView)
                            } else {
                                view.findViewById<ImageView>(R.id.imageview)
                                    .setImageResource(R.drawable.ic_baseline_camera_alt_24)
                            }
                        }
                    }
                }
            }
        }
    }
}
