package com.androiddevs.mvvmnewsapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.androiddevs.mvvmnewsapp.repositories.NewsRepository

class NewsViewModel(
    val newRepository: NewsRepository
):ViewModel() {

}