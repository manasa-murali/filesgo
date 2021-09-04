package com.example.filesgo.viewModel

import android.text.SpannableStringBuilder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filesgo.model.Audio
import com.example.filesgo.model.FileData
import com.example.filesgo.model.Image
import com.example.filesgo.model.Video
import com.example.filesgo.repository.FileRepository
import com.example.filesgo.utils.Action
import com.example.filesgo.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FileSearchViewModel
@Inject
constructor(
    private val fileRepository: FileRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val mutableAppStateFlow = MutableStateFlow(AppState(uiState = MyUIState.Initial))

    val uiDataFlow = mutableAppStateFlow.asStateFlow()

    fun loadFilesFromDevice() {
        viewModelScope.launch(dispatcher) {
            mutableAppStateFlow.emit(
                AppState(uiState = MyUIState.Loading)
            )
            val filesList = fileRepository.loadFilesFromStorage()
            if (filesList.isNotEmpty()) {
                sortFilesBy(uiDataFlow.value.sortOrder, filesList)
            } else {
                mutableAppStateFlow.emit(
                    AppState(uiState = MyUIState.Failure(
                        Constants.FILE_FETCH_ERROR
                    ))
                )
            }
        }
    }

    fun sortFilesBy(sortOrder: Action.SortBy, filesList: List<FileData>) {
        viewModelScope.launch(dispatcher) {
            val sortedFiles = when (sortOrder) {
                Action.SortBy.ALPHABET -> {
                    filesList.sortedBy {
                        val fileNameList = it.name.split(".")
                        val subList = fileNameList.subList(0, fileNameList.size - 1)
                        val stringBuilder = SpannableStringBuilder()
                        subList.forEach { fileNameEntry -> stringBuilder.append(fileNameEntry) }
                        stringBuilder.toString()
                    }
                }
                Action.SortBy.CHRONOLOGY -> {
                    filesList.sortedBy {
                        when (it.fileType) {
                            is Audio -> it.fileType.dateCreated
                            is Image -> it.fileType.dateCreated
                            is Video -> it.fileType.dateCreated
                        }
                    }
                }
                Action.SortBy.EXTENSION -> {
                    filesList.sortedBy {
                        it.extension.lowercase(Locale.getDefault())
                    }
                }
            }
            if (uiDataFlow.value.isSearchEnabled) {
                mutableAppStateFlow.emit(
                    AppState(isSearchEnabled = uiDataFlow.value.isSearchEnabled,
                        uiState = uiDataFlow.value.uiState,
                        sortOrder = sortOrder,
                        searchResult = SearchResult(uiDataFlow.value.searchResult.searchString,
                            sortedFiles),
                        isSorting = true)
                )
            } else {
                mutableAppStateFlow.emit(
                    AppState(uiState = MyUIState.Success(sortedFiles),
                        sortOrder = sortOrder,
                        isSorting = true)
                )
            }
        }
    }

    fun searchForFiles(searchString: String) {
        viewModelScope.launch(dispatcher) {
            val currentState = uiDataFlow.value
            if (currentState.uiState is MyUIState.Success) {
                val filesFound =
                    fileRepository.searchFiles(searchString, currentState.uiState.myUIDataList)
                mutableAppStateFlow.emit(
                    AppState(
                        isSearchEnabled = true,
                        uiState = uiDataFlow.value.uiState,
                        searchResult = SearchResult(searchString, filesFound),
                        sortOrder = uiDataFlow.value.sortOrder
                    )
                )
            }
        }
    }

    fun cancelSearch() {
        viewModelScope.launch(dispatcher) {
            mutableAppStateFlow.emit(
                AppState(isSearchEnabled = false,
                    uiState = uiDataFlow.value.uiState,
                    sortOrder = uiDataFlow.value.sortOrder,
                    searchResult = SearchResult("", emptyList()))
            )
        }
    }

    fun displayDetails(fileData: FileData) {
        viewModelScope.launch(dispatcher) {
            mutableAppStateFlow.emit(
                uiDataFlow.value.copy(
                    fileDetails = fileData
                )
            )
        }
    }

    fun writeToFile() {
        if (uiDataFlow.value.uiState is MyUIState.Success
            && uiDataFlow.value.searchResult.filesFound.isNotEmpty()
        ) {
            viewModelScope.launch(dispatcher) {
                fileRepository.writeToFile(uiDataFlow.value.searchResult.filesFound)
            }
        }
    }

    fun refreshLayout() {
        if (!uiDataFlow.value.isSearchEnabled) {
            loadFilesFromDevice()
        }

    }
}