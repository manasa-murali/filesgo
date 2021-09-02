package com.example.filesgo.viewModel

import com.example.filesgo.model.FileData
import com.example.filesgo.utils.Action

data class AppState(
    val isSearchEnabled: Boolean = false,
    val uiState: MyUIState = MyUIState.Initial,
    val searchResult: SearchResult = SearchResult("", emptyList()),
    val sortOrder: Action.SortBy = Action.SortBy.EXTENSION,
    val fileDetails: FileData? = null,
    val isSorting: Boolean = false,
)

sealed class MyUIState {
    object Initial : MyUIState()
    object Loading : MyUIState()
    data class Success(val myUIDataList: List<FileData>) : MyUIState()
    data class Failure(val error: String) : MyUIState()
}

data class SearchResult(
    val searchString: String,
    val filesFound: List<FileData>,
)