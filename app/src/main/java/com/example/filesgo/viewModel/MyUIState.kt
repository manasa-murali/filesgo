package com.example.filesgo.viewModel

import com.example.filesgo.model.FileData
import com.example.filesgo.utils.SortBy

data class AppState(
    val fetchFilesState: MyUIState = MyUIState.Initial,
    val sortOrder: SortBy = SortBy.ALPHABET_A_Z,
    val fileDetails: FileData? = null,
    val shouldSave: Boolean = false,
    val searchString: String = "",
)

sealed class MyUIState {
    object Initial : MyUIState()
    object Fetching : MyUIState()
    object Processing : MyUIState()
    data class Success(val filesList: List<FileData>) : MyUIState()
    object EmptyFiles : MyUIState()
    data class Failure(val error: String) : MyUIState()
    object Saved : MyUIState()
}

data class SearchResult(
    val searchString: String,
    val filesFound: List<FileData>,
)