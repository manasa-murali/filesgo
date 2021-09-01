package com.example.filesgo.viewModel

import com.example.filesgo.model.FileData
import com.example.filesgo.utils.Action

data class AppState(
    val isSearchEnabled: Boolean = false,
    val uiState: MyUIState = MyUIState.Initial,
    val searchResult: List<FileData> = emptyList(),
    val sortOrder: Action.SortBy = Action.SortBy.EXTENSION,
    val imageDetails: FileData? = null
)

sealed class MyUIState{
    object Initial: MyUIState()
    object Loading: MyUIState()
    data class Success(val myUIDataList: List<FileData>): MyUIState()
    data class Failure(val error: String): MyUIState()
}
