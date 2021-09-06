package com.example.filesgo.view

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.filesgo.R
import com.example.filesgo.model.FileData
import com.example.filesgo.utils.Constants
import com.example.filesgo.utils.SortBy
import com.example.filesgo.viewModel.FileSearchViewModel
import com.example.filesgo.viewModel.MyUIState
import com.gun0912.tedpermission.TedPermissionResult
import com.gun0912.tedpermission.coroutine.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class FileSearchFragment : Fragment(R.layout.fragment_file_search) {

    private var notificationManager: NotificationManagerCompat? = null

    val viewModel: FileSearchViewModel by activityViewModels()

    private val TAG: String = "FileSearchFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel(view)
        setUpSearch(view)
        setUpRefreshView(view)
        setUpSortOrderView(view)
        setUpNotificationManager(view)
        setUpWriteView(view)
    }

    private fun setUpWriteView(view: View) {
        view.findViewById<Button>(R.id.write_button).setOnClickListener {
            viewModel.saveContent()
        }
    }

    private fun setUpNotificationManager(view: View) {
        notificationManager = NotificationManagerCompat.from(view.context)
    }

    private fun setUpSortOrderView(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.sort_order_spinner)
        ArrayAdapter.createFromResource(
            view.context,
            R.array.sort_array,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = it
        }
        spinner.onItemSelectedListener = getOnItemSelectedListener()
    }

    private fun getOnItemSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {

            val sortOrder = when (pos) {
                1 -> SortBy.ALPHABET_Z_A
                2 -> SortBy.CREATED_Z_A
                3 -> SortBy.CREATED_A_Z
                4 -> SortBy.EXTENSION_A_Z
                5 -> SortBy.EXTENSION_Z_A
                else -> SortBy.ALPHABET_A_Z
            }

            viewModel.sortBy(sortOrder)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {

        }

    }

    private fun setUpRefreshView(view: View) {
        view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).setOnRefreshListener {
            view.findViewById<AppCompatEditText>(R.id.search_edittext).text?.clear()
            viewModel.refreshLayout()
        }
    }

    private fun setUpSearch(view: View) {
        view.findViewById<AppCompatImageButton>(R.id.search_button).setOnClickListener {
            val searchString =
                view.findViewById<AppCompatEditText>(R.id.search_edittext).text.toString()
            if (searchString.isNotEmpty()) {
                viewModel.search(searchString.lowercase())
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.empty_search_string),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }

        view.findViewById<AppCompatImageButton>(R.id.clear_search).setOnClickListener {
            viewModel.clearSearch()
            view.findViewById<AppCompatEditText>(R.id.search_edittext).text?.clear()
        }
    }

    private fun subscribeToViewModel(view: View) {
        val errorStateUi = view.findViewById<TextView>(R.id.error_text)
        val successUi = view.findViewById<RecyclerView>(R.id.file_search_list)
        lifecycleScope.launchWhenCreated {
            viewModel.listingDataFlow.collect {
                when (val fetchFilesState = it.fetchFilesState) {
                    MyUIState.Initial -> {
                        Log.i(TAG, "initial")
                        val permissions = getPermissions()
                        if (permissions.isGranted) {
                            viewModel.loadFilesFromDevice()
                        }
                    }

                    MyUIState.Fetching -> {
                        setRefreshing(true, view)
                        Log.i(TAG, "Fetching")
                    }

                    MyUIState.Processing -> {
                        setRefreshing(true, view)
                        Log.i(TAG, "Processing")
                    }
                    is MyUIState.Success -> {
                        setRefreshing(false, view)
                        Log.i(TAG, "Success ${fetchFilesState.filesList}")
                        requireActivity().runOnUiThread {
                            setSuccessUiState(view, fetchFilesState, it.searchString)
                        }
                        errorStateUi.visibility = View.GONE
                        successUi.visibility = View.VISIBLE
                    }
                    is MyUIState.Failure -> {
                        setRefreshing(false, view)
                        errorStateUi.visibility = View.VISIBLE
                        errorStateUi.text = getString(R.string.something_went_wrong)
                        successUi.visibility = View.GONE
                        Log.i(TAG, "Failure")
                    }
                    MyUIState.EmptyFiles -> {
                        setRefreshing(false, view)
                        errorStateUi.visibility = View.VISIBLE
                        errorStateUi.text = getString(R.string.empty_files)
                        successUi.visibility = View.GONE
                        setCountView(view, "0")
                        Log.i(TAG, "Empty")
                    }

                    MyUIState.Saved -> {
                        setRefreshing(false, view)
                        Log.i(TAG, "Saved")
                    }
                }
            }
        }
    }

    private fun setRefreshing(isRefreshing: Boolean, view: View) {
        requireActivity().runOnUiThread {
            view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isRefreshing = isRefreshing
        }
    }

    private fun setSuccessUiState(
        view: View,
        successState: MyUIState.Success,
        searchString: String,
    ) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.file_search_list)
        var currentAdapter = recyclerView.adapter
        if (currentAdapter == null) {
            currentAdapter = FilesAdapter(
                filesList = successState.filesList,
                ITalkToFragment = object : ITalkToFragment {
                    override fun onItemClicked(fileData: FileData) {
                        viewModel.loadFileDetails(fileData)
                        if (findNavController().currentDestination?.id == R.id.fileSearchFragment) {
                            findNavController().navigate(R.id.action_fileSearchFragment_to_detailsFragment)
                        }
                    }
                }, searchString
            )
            recyclerView.adapter = currentAdapter
        } else {
            (currentAdapter as FilesAdapter).submitItems(successState.filesList, searchString)
        }
        val count = successState.filesList.size.toString()
        setCountView(view, count)
    }

    private fun setCountView(view: View, count: String) {
        val searchCountView = view.findViewById<TextView>(R.id.searchResult)
        searchCountView.text = getString(R.string.files_tag, count)
        constructNotification(count)
    }

    private suspend fun getPermissions(): TedPermissionResult {
        val permissionBuilder: TedPermission.Builder = TedPermission.create()
        return permissionBuilder.setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .setDeniedMessage(getString(R.string.denied_permission))
            .check()
    }

    private fun constructNotification(filesFound: String) {
        val notificationBuilder = NotificationCompat.Builder(requireContext(), Constants.CHANNEL_ID)
        val notification = notificationBuilder.setSmallIcon(R.drawable.ic_baseline_search_24)
            .setContentTitle(getString(R.string.search))
            .setContentText(getString(R.string.files_tag, filesFound))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager?.notify(2105, notification)

    }
}