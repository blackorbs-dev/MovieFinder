/*
 * Copyright 2024 BlackOrbs (blackorbs@icloud.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package blackorbs.dev.moviefinder.ui.searchscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import blackorbs.dev.moviefinder.R
import blackorbs.dev.moviefinder.databinding.FragmentSearchBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchPage: Fragment() {

    private val searchViewModel: SearchViewModel by viewModels()
    private var binding: FragmentSearchBinding? = null
    private val listAdapter = ListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if(binding == null) {
            binding = FragmentSearchBinding.inflate(inflater)
            initPage()
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservers()
    }

    private fun setUpObservers(){
        searchViewModel.movies.observe(viewLifecycleOwner){
            viewLifecycleOwner.lifecycleScope.launch {
                listAdapter.submitData(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            listAdapter.loadStateFlow.collect {
                when (it.refresh) {
                    is LoadState.NotLoading -> binding!!.loading.hide()
                    is LoadState.Error -> {
                        binding!!.loading.hide()
                        if(listAdapter.itemCount == 0) Snackbar.make(binding!!.root, getString(R.string.error_try_again), Snackbar.LENGTH_LONG).show()
                    }
                    LoadState.Loading -> {}
                }
            }
        }
    }

    private fun initPage() {
        binding!!.moviesList.apply {
            setHasFixedSize(true)
            adapter = listAdapter.withLoadStateFooter(ListLoadStateAdapter { listAdapter.retry() })
        }
        binding!!.searchBar.setOnQueryTextListener(object : OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                getMovies(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        getMovies("")
    }

    private fun getMovies(query: String?){
        query?.let {
            binding!!.loading.show()
            searchViewModel.getMovies(it.trim())
            binding!!.searchBar.clearFocus()
        }
    }

}