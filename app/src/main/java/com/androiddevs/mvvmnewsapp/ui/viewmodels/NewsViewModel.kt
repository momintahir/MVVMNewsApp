package com.androiddevs.mvvmnewsapp.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.NewsApplication
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponse
import com.androiddevs.mvvmnewsapp.repositories.NewsRepository
import com.androiddevs.mvvmnewsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    val newRepository: NewsRepository,
    app:Application
) : AndroidViewModel(app) {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse:NewsResponse?=null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse:NewsResponse?=null


    init {

        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        breakingNews.postValue(Resource.Loading())
        try{
            if(hasInternetConnection()) {
                val response = newRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }
            else {
                breakingNews.postValue(Resource.Error("No internet connection"))
            }
        }

        catch (t:Throwable){
                when (t){
                    is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                    else -> breakingNews.postValue(Resource.Error("Conversion Error"))
                }
        }

    }



    fun searchNews(searchQuery:String)=viewModelScope.launch {
        searchNews.postValue(Resource.Loading())
        try{
            if(hasInternetConnection()) {
                val response = newRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }
            else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        }

        catch (t:Throwable){
            when (t){
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }


    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse==null) {
                    breakingNewsResponse=resultResponse
                }
                else {
                    val oldArticles=breakingNewsResponse?.articles
                    val newArticles=resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse==null) {
                    searchNewsResponse=resultResponse
                }
                else {
                    val oldArticles=searchNewsResponse?.articles
                    val newArticles=resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?:resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article)=viewModelScope.launch {
        newRepository.upsert(article)
    }
    fun getSavedNews()=newRepository.getSavedNews()
    fun deleteArticle(article: Article)=viewModelScope.launch {
        newRepository.deleteArticle(article)
    }

    private fun hasInternetConnection():Boolean{
        val connectivityManager=getApplication<NewsApplication>().
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            val activeNetwork=connectivityManager.activeNetwork?:return false
            val capabilities=   connectivityManager.getNetworkCapabilities(activeNetwork)?: return false
            return  when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> return  true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> return  true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> return  true

                else -> false
            }
        }
        else{
            connectivityManager.activeNetworkInfo?.run {
                return when(type)
                {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET ->true

                    else -> false
                }
            }
        }
        return false

    }

}