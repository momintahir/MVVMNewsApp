package com.androiddevs.mvvmnewsapp.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androiddevs.mvvmnewsapp.repositories.NewsRepository
import com.androiddevs.mvvmnewsapp.ui.viewmodels.NewsViewModel

class NewsViewModelProviderFactory(
    val app:Application,
    val newsRepository: NewsRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository,app) as T
    }
}