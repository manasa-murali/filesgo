package com.example.filesgo.view

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.filesgo.MainActivity
import com.example.filesgo.R
import com.example.filesgo.model.FileData
import com.example.filesgo.utils.Action
import com.example.filesgo.utils.Constants
import com.example.filesgo.viewModel.FileSearchViewModel
import com.example.filesgo.viewModel.MyUIState
import com.gun0912.tedpermission.coroutine.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FileSearchFragment : Fragment(R.layout.fragment_file_search), OnDetailsClicked,
    AdapterView.OnItemSelectedListener {

    lateinit var viewModel: FileSearchViewModel

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel

        val recyclerView = view.findViewById<RecyclerView>(R.id.file_search_list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        var adapter = recyclerView.adapter
        if (adapter == null) {
            adapter = FilesAdapter(arrayListOf(), this.findNavController(), this)
            recyclerView.adapter = adapter
        }
        val spinner = view.findViewById<Spinner>(R.id.sort_order_spinner)
        ArrayAdapter.createFromResource(view.context,
            R.array.sort_array,
            android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = this
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
                            val searchResultCount = view.findViewById<TextView>(R.id.searchResult)
                            searchResultCount.visibility = View.VISIBLE
                            searchResultCount.text =
                                Constants.FILES_FOUND + (viewModel.uiDataFlow.value.searchResult.size).toString()
                        } else {
                            view.findViewById<TextView>(R.id.searchResult).visibility = View.GONE
                            (adapter as FilesAdapter).submitList(appState.uiState.myUIDataList)
                        }
                        when (appState.sortOrder) {
                            Action.SortBy.ALPHABET -> spinner.setSelection(1)
                            Action.SortBy.CHRONOLOGY -> spinner.setSelection(2)
                            Action.SortBy.EXTENSION -> spinner.setSelection(3)
                        }
                    }
                }
            }
        }
        view.findViewById<Button>(R.id.fetch_files_button).setOnClickListener {
            view.findViewById<TextView>(R.id.searchResult).visibility = GONE
            view.findViewById<EditText>(R.id.search_edittext).setText("")
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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        if ((viewModel.uiDataFlow.value.uiState is MyUIState.Success)) {
            val filesList = if (viewModel.uiDataFlow.value.isSearchEnabled) {
                viewModel.uiDataFlow.value.searchResult
            } else {
                (viewModel.uiDataFlow.value.uiState as MyUIState.Success).myUIDataList
            }
            val sortOrder = when (parent!!.getItemAtPosition(pos).toString()) {
                Action.SortBy.ALPHABET.name -> {
                    Action.SortBy.ALPHABET
                }
                Action.SortBy.CHRONOLOGY.name -> {
                    Action.SortBy.CHRONOLOGY
                }
                Action.SortBy.EXTENSION.name -> {
                    Action.SortBy.EXTENSION
                }
                else -> Action.SortBy.EXTENSION
            }
            viewModel.sortFilesBy(sortOrder, filesList)
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}