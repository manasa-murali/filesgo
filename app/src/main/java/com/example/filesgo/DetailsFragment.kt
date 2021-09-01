package com.example.filesgo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.filesgo.model.Image
import com.example.filesgo.viewModel.FileSearchViewModel
import com.example.filesgo.viewModel.MyUIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

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
                            view.findViewById<TextView>(R.id.date_added_value).text = imageData.dateCreated.toString()


                        }
                    }
                }
            }
        }
    }
}
