package com.example.filesgo

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.filesgo.model.Image
import com.example.filesgo.viewModel.FileSearchViewModel
import com.example.filesgo.viewModel.MyUIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File


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
                            view.findViewById<TextView>(R.id.date_added_value).text =
                                imageData.dateCreated.toString()
                            val file = File((appState.imageDetails.path))
                            if (file.exists()) {
                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                if (bitmap != null) {
                                    view.findViewById<ImageView>(R.id.imageview)
                                        .setImageBitmap(bitmap)
                                } else {
                                    view.findViewById<ImageView>(R.id.imageview)
                                        .setImageResource(R.drawable.ic_baseline_camera_alt_24)
                                }
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
