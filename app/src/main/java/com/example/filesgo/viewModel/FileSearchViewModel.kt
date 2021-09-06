package com.example.filesgo.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filesgo.model.FileData
import com.example.filesgo.repository.FileRepository
import com.example.filesgo.utils.SortBy
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

    private val mutableAppState = MutableStateFlow(AppState())

    val listingDataFlow = mutableAppState.asStateFlow()

    private val mutableDetailsState = MutableLiveData<FileData>()

    val detailsState: LiveData<FileData> = mutableDetailsState

    fun loadFilesFromDevice() {
        val currentState = listingDataFlow.value
        viewModelScope.launch(dispatcher) {

            //Emit fetching State
            mutableAppState.emit(
                currentState.copy(
                    fetchFilesState = MyUIState.Fetching
                )
            )

            val filesList = fileRepository.loadFilesFromStorage()

            //Emit processing State
            mutableAppState.emit(
                currentState.copy(
                    fetchFilesState = MyUIState.Processing
                )
            )

            if (filesList.isNotEmpty()) {
                val sortedFiles = sort(
                    currentState.sortOrder,
                    filesList
                )

                //Emit success state
                mutableAppState.emit(
                    currentState.copy(
                        fetchFilesState = MyUIState.Success(
                            filesList = sortedFiles
                        ),
                        searchString = ""
                    )
                )
            } else {
                //Emit empty fils state
                mutableAppState.emit(
                    currentState.copy(
                        fetchFilesState = MyUIState.EmptyFiles,
                        searchString = ""
                    )
                )
            }
        }
    }

    private fun sort(sortOrder: SortBy, filesList: List<FileData>): List<FileData> {
        val sortedList = when (sortOrder) {

            SortBy.ALPHABET_A_Z -> {
                filesList.sortedBy {
                    val split = it.name.split(".")
                    val dropLast = split.dropLast(1)
                    val stringBuilder = StringBuilder()
                    dropLast.forEach {
                        stringBuilder.append(it)
                    }
                    stringBuilder.toString()
                }
            }

            SortBy.ALPHABET_Z_A -> {
                filesList.sortedByDescending {
                    val split = it.name.split(".")
                    val dropLast = split.dropLast(1)
                    val stringBuilder = StringBuilder()
                    dropLast.forEach {
                        stringBuilder.append(it)
                    }
                    stringBuilder.toString()
                }
            }

            SortBy.CREATED_A_Z -> {
                filesList.sortedBy {
                    it.dateCreated
                }
            }

            SortBy.CREATED_Z_A -> {
                filesList.sortedByDescending {
                    it.dateCreated
                }
            }

            SortBy.EXTENSION_A_Z -> {
                filesList.sortedBy {
                    it.extension.lowercase(Locale.getDefault())
                }
            }

            SortBy.EXTENSION_Z_A -> {
                filesList.sortedByDescending {
                    it.extension.lowercase(Locale.getDefault())
                }
            }
        }
        return sortedList
    }

    fun sortBy(sortOrder: SortBy) {
        viewModelScope.launch(dispatcher) {
            val currentState = listingDataFlow.value
            if (currentState.fetchFilesState is MyUIState.Success) {
                val sortedFiles = sort(
                    sortOrder,
                    currentState.fetchFilesState.filesList
                )
                mutableAppState.emit(
                    currentState.copy(
                        fetchFilesState = MyUIState.Success(
                            filesList = sortedFiles
                        ),
                        sortOrder = sortOrder
                    )
                )
            }
        }
    }

    fun search(searchStr: String) {
        val currentState = listingDataFlow.value
        if (currentState.fetchFilesState is MyUIState.Success) {
            viewModelScope.launch(dispatcher) {
                val currentState = listingDataFlow.value

                //Emit processing State
                mutableAppState.emit(
                    currentState.copy(
                        fetchFilesState = MyUIState.Processing
                    )
                )

                //Search only if fecthing is success
                if (currentState.fetchFilesState is MyUIState.Success) {
                    val filesFound =
                        fileRepository.searchFiles(
                            searchStr,
                            currentState.fetchFilesState.filesList
                        )

                    if (filesFound.isNotEmpty()) {
                        //Emit success state
                        mutableAppState.emit(
                            currentState.copy(
                                fetchFilesState = MyUIState.Success(
                                    filesList = filesFound
                                ),
                                searchString = searchStr
                            )
                        )
                    } else {
                        //Emit empty files state
                        mutableAppState.emit(
                            currentState.copy(
                                fetchFilesState = MyUIState.EmptyFiles,
                                searchString = searchStr
                            )
                        )
                    }
                } else {
                    //Emit fetching State
                    mutableAppState.emit(
                        currentState.copy(
                            fetchFilesState = MyUIState.Fetching
                        )
                    )
                }

            }
        }
    }

    fun clearSearch() {
        loadFilesFromDevice()
    }


    fun refreshLayout() {
        loadFilesFromDevice()
    }

    fun saveContent() {
        val currentState = listingDataFlow.value
        viewModelScope.launch(dispatcher) {
            if (currentState.fetchFilesState is MyUIState.Success) {

                //Emit processing State
                mutableAppState.emit(
                    currentState.copy(
                        fetchFilesState = MyUIState.Processing
                    )
                )

                fileRepository.writeToFile(currentState.fetchFilesState.filesList)

                //Emit saved State
                mutableAppState.emit(
                    currentState.copy(
                        fetchFilesState = MyUIState.Saved
                    )
                )
            }
        }
    }

    fun loadFileDetails(fileData: FileData) {
        mutableDetailsState.postValue(fileData)
    }
}