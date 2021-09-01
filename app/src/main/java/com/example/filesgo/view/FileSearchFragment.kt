package com.example.filesgo.view

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.filesgo.MainActivity
import com.example.filesgo.R
import com.example.filesgo.model.FileData
import com.example.filesgo.utils.Constants
import com.example.filesgo.viewModel.FileSearchViewModel
import com.example.filesgo.viewModel.MyUIState
import com.gun0912.tedpermission.coroutine.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FileSearchFragment : Fragment(R.layout.fragment_file_search),OnDetailsClicked {

    lateinit var viewModel : FileSearchViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel

        val recyclerView = view.findViewById<RecyclerView>(R.id.file_search_list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        var adapter = recyclerView.adapter
        if (adapter == null) {
            adapter = FilesAdapter(arrayListOf(), this.findNavController(),this)
            recyclerView.adapter = adapter
        }
        lifecycleScope.launchWhenCreated {
            viewModel.uiDataFlow.collect { appState ->
                when (appState.uiState) {
                    is MyUIState.Failure -> {
                        recyclerView.visibility = View.GONE
                        view.findViewById<TextView>(R.id.error_text).visibility = View.VISIBLE
                        view.findViewById<TextView>(R.id.error_text).text = appState.uiState.error
                    }
                    MyUIState.Initial -> {
                        view.findViewById<TextView>(R.id.error_text).visibility = View.GONE
                        recyclerView.visibility = View.GONE

                    }
                    MyUIState.Loading -> {
                        view.findViewById<TextView>(R.id.error_text).visibility = View.GONE
                        recyclerView.visibility = View.GONE
                    }
                    is MyUIState.Success -> {
                        view.findViewById<TextView>(R.id.error_text).visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        if (appState.isSearchEnabled) {
                            (adapter as FilesAdapter).submitList(appState.searchResult)
                        } else {
                            (adapter as FilesAdapter).submitList(appState.uiState.myUIDataList)
                        }
                    }
                }
            }
        }
        view.findViewById<Button>(R.id.fetch_files_button).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val check = TedPermission.create()
                    .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .setDeniedMessage(Constants.GRANT_PERMISSION)
                    .check()
                if (check.isGranted) {
                    viewModel.loadFilesFromDevice()
                }
            }
        }
        view.findViewById<Button>(R.id.search_button).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (viewModel.uiDataFlow.value.uiState is MyUIState.Success) {
                    val searchString =
                        view.findViewById<EditText>(R.id.search_edittext).text.toString()
                    if (searchString.isNotEmpty()) {
                        viewModel.searchForFiles(searchString)
                    }
                }
            }
        }
        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            if (viewModel.uiDataFlow.value.uiState is MyUIState.Success) {
                view.findViewById<EditText>(R.id.search_edittext).setText("")
                viewModel.cancelSearch()
            }
        }
    }

    override fun onImageClicked(fileData: FileData) {
        viewModel.displayDetails(fileData)
    }
}