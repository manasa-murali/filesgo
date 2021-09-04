package com.example.filesgo.view

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import kotlin.random.Random


@AndroidEntryPoint
class FileSearchFragment : Fragment(R.layout.fragment_file_search), OnDetailsClicked,
    AdapterView.OnItemSelectedListener {

    lateinit var viewModel: FileSearchViewModel
    lateinit var notificationManager: NotificationManagerCompat

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel

        val recyclerView = view.findViewById<RecyclerView>(R.id.file_search_list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        var adapter = recyclerView.adapter
        if (adapter == null) {
            adapter = FilesAdapter(arrayListOf(), this)
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


        notificationManager = NotificationManagerCompat.from(requireContext())

        lifecycleScope.launchWhenCreated {
            var oldState = viewModel.uiDataFlow.value
            if (oldState.uiState == viewModel.uiDataFlow.value.uiState) {
                view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isRefreshing =
                    false
            }
            viewModel.uiDataFlow.collect { appState ->
                when (appState.uiState) {
                    is MyUIState.Failure -> {
                        if (oldState.uiState !is MyUIState.Initial) {
                            view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isEnabled =
                                true
                        }
                        view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isRefreshing =
                            false
                        recyclerView.visibility = View.GONE
                        view.findViewById<TextView>(R.id.error_text).visibility = View.VISIBLE
                        view.findViewById<TextView>(R.id.error_text).text = appState.uiState.error
                    }
                    MyUIState.Initial -> {
                        view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isEnabled = false
                        view.findViewById<TextView>(R.id.error_text).visibility = View.GONE
                        recyclerView.visibility = View.GONE

                    }
                    MyUIState.Loading -> {
                        if (oldState.uiState is MyUIState.Success || oldState.uiState is MyUIState.Failure) {
                            view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isEnabled =
                                true
                            view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isRefreshing =
                                true
                            view.findViewById<TextView>(R.id.error_text).visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        } else if (oldState.uiState is MyUIState.Initial) {
                            view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isEnabled =
                                false
                        }
                    }
                    is MyUIState.Success -> {
                        view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isEnabled =
                            !viewModel.uiDataFlow.value.isSearchEnabled && oldState.uiState !is MyUIState.Initial
                        view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isRefreshing =
                            false
                        view.findViewById<TextView>(R.id.error_text).visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        if (appState.isSearchEnabled) {
                            (recyclerView.adapter as FilesAdapter).setAdapterData(appState.searchResult.filesFound,
                                appState.searchResult.searchString, this@FileSearchFragment)
                            (recyclerView.adapter as FilesAdapter).notifyDataSetChanged()

                            val searchResultCount =
                                view.findViewById<TextView>(R.id.searchResult)
                            searchResultCount.visibility = View.VISIBLE
                            val filesFound =
                                (viewModel.uiDataFlow.value.searchResult.filesFound.size).toString()
                            searchResultCount.text =
                                Constants.FILES_FOUND + filesFound
                            if (!appState.isSorting && (oldState != appState && appState.fileDetails == null)) {
                                constructNotification(filesFound)
                            }
                        } else {
                            view.findViewById<TextView>(R.id.searchResult).visibility = View.GONE
                            (recyclerView.adapter as FilesAdapter).setAdapterData(appState.uiState.myUIDataList,
                                "", this@FileSearchFragment)
                            (recyclerView.adapter as FilesAdapter).notifyDataSetChanged()
                        }
                        when (appState.sortOrder) {
                            Action.SortBy.ALPHABET -> spinner.setSelection(1)
                            Action.SortBy.CHRONOLOGY -> spinner.setSelection(2)
                            Action.SortBy.EXTENSION -> spinner.setSelection(3)
                        }
                    }
                }
                oldState = appState
            }
        }
        view.findViewById<Button>(R.id.fetch_files_button).setOnClickListener {
            view.findViewById<TextView>(R.id.searchResult).visibility = GONE
            view.findViewById<EditText>(R.id.search_edittext).setText("")
            lifecycleScope.launch(Dispatchers.Main) {
                val check = TedPermission.create()
                    .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .setDeniedMessage(Constants.GRANT_PERMISSION)
                    .check()
                if (check.isGranted) {
                    viewModel.loadFilesFromDevice()
                }
            }
        }
        view.findViewById<ImageButton>(R.id.search_button).setOnClickListener {
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
        view.findViewById<ImageButton>(R.id.cancel_button).setOnClickListener {
            if (viewModel.uiDataFlow.value.uiState is MyUIState.Success) {
                view.findViewById<EditText>(R.id.search_edittext).setText("")
                viewModel.cancelSearch()
            }
        }
        view.findViewById<Button>(R.id.write_button).setOnClickListener {
            if (viewModel.uiDataFlow.value.uiState is MyUIState.Success &&
                viewModel.uiDataFlow.value.isSearchEnabled
            ) {
                lifecycleScope.launch(Dispatchers.Main) {
                    val check = TedPermission.create()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .setDeniedMessage(Constants.GRANT_PERMISSION)
                        .check()
                    if (check.isGranted) {
                        viewModel.writeToFile()
                        if (viewModel.uiDataFlow.value.searchResult.filesFound.isNotEmpty()) {
                            Toast.makeText(requireContext(),
                                Constants.FILES_WRITTEN,
                                Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(requireContext(),
                                Constants.FILES_NOT_WRITTEN,
                                Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            }
        }
        view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).setOnRefreshListener {
            viewModel.refreshLayout()
        }
    }

    private fun constructNotification(filesFound: String) {
        val notificationBuilder = NotificationCompat.Builder(requireContext(), Constants.CHANNEL_ID)
        val notification = notificationBuilder.setSmallIcon(R.drawable.ic_baseline_search_24)
            .setContentTitle(Constants.SEARCH_RESULT)
            .setContentText(Constants.FILES_FOUND + filesFound)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(Random.nextInt(), notification)

    }

    override fun onItemClicked(fileData: FileData) {
        viewModel.displayDetails(fileData)
        if (findNavController().currentDestination?.id == R.id.fileSearchFragment) {
            findNavController().navigate(R.id.action_fileSearchFragment_to_detailsFragment)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        if ((viewModel.uiDataFlow.value.uiState is MyUIState.Success)) {
            val filesList = if (viewModel.uiDataFlow.value.isSearchEnabled) {
                viewModel.uiDataFlow.value.searchResult.filesFound
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