package com.example.filesgo.di

import com.example.filesgo.repository.FileRepository
import com.example.filesgo.viewModel.FileSearchViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideVM(
        fileRepository: FileRepository
    ): FileSearchViewModel {
        return FileSearchViewModel(fileRepository)
    }

}
