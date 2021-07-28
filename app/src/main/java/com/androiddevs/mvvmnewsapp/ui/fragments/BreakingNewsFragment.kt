package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.interfaces.OnItemClickListener
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.viewmodels.NewsViewModel
import com.androiddevs.mvvmnewsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_breaking_news.*
import kotlinx.android.synthetic.main.fragment_breaking_news.view.*


class BreakingNewsFragment : Fragment() {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_breaking_news, container, false)
        viewModel = (activity as NewsActivity).viewModel
        setupRecyclerView(view)

        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages=newsResponse.totalResults/20 + 2
                        isLastPage=viewModel.breakingNewsPage==totalPages
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Log.e("breakingNewsFragment", message)

                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

            }

        })
        return view
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
        isLoading=true
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading=false
    }


    var isLoading=false
    var isLastPage=false
    var isScrolling=false

    val scrollListener=object:RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState==AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling=true

            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager=recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition=layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount=layoutManager.childCount
            val totalItemCount=layoutManager.itemCount

           val isNotLoadingAndNotLastPage=!isLoading && !isLastPage
            val isAtLastItem=firstVisibleItemPosition +visibleItemCount>=totalItemCount
            val isNotAtBeginning=firstVisibleItemPosition>=0
            val isTotalMoreThanVisible=totalItemCount>=20
            val shouldPaginate=isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate){
                viewModel.getBreakingNews("us")
                isScrolling=false
            }else
            {

            }

        }
    }

    private fun setupRecyclerView(view: View) {
        newsAdapter = NewsAdapter(object :OnItemClickListener{
            override fun onItemClickListener(article: Article) {
                val bundle = Bundle().apply {
                    putParcelable("article", article)
                }
                findNavController().navigate(
                    R.id.action_breakingNewsFragment_to_articleFragment, bundle
                )
            }

        })
        view.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }

}