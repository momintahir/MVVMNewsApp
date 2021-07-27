package com.androiddevs.mvvmnewsapp.interfaces

import com.androiddevs.mvvmnewsapp.models.Article

interface OnItemClickListener {
    fun onItemClickListener(article: Article)
}