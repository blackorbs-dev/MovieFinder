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

package blackorbs.dev.moviefinder.ui.moviescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import blackorbs.dev.moviefinder.R
import blackorbs.dev.moviefinder.databinding.FragmentMovieBinding
import blackorbs.dev.moviefinder.models.Movie
import blackorbs.dev.moviefinder.models.Resource
import blackorbs.dev.moviefinder.ui.searchscreen.ListAdapter
import coil.load
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoviePage: Fragment() {

    private val movieViewModel: MovieViewModel by viewModels()
    private var binding: FragmentMovieBinding? = null
    private var imdb: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if(binding==null){
            binding = FragmentMovieBinding.inflate(inflater)
            initPage()
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObserver()
    }

    private fun setUpObserver(){
        movieViewModel.movie.observe(viewLifecycleOwner){
            when(it.status){
                Resource.Status.SUCCESS -> {
                    it.data?.let {
                            movie -> bindData(movie)
                        binding!!.blurBack.visibility = View.GONE
                        binding!!.loading.hide()
                    }
                }

                Resource.Status.ERROR -> {
                    binding!!.loading.hide()
                    Snackbar.make(binding!!.root, getString(R.string.error_try_again), Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry){
                            imdb?.let {id -> movieViewModel.getMovie(id) }
                        }
                        .show()
                }

                Resource.Status.LOADING -> {
                    binding!!.blurBack.visibility = View.VISIBLE
                    binding!!.loading.show()
                }
            }
        }
    }

    private fun initPage(){
        imdb = arguments?.getString(ListAdapter.IMDB_KEY)?.apply { movieViewModel.getMovie(this) }
        binding!!.backBtn.setOnClickListener { findNavController().popBackStack() }
    }

    private fun bindData(movie: Movie){
        with(binding!!){
            image.load(movie.Poster){
                val placeholder = AppCompatResources.getDrawable(root.context, R.drawable.placeholder)
                placeholder(placeholder)
                error(placeholder)
            }
            title.text = movie.Title
            runtime.text = if(movie.Runtime?.equals("N/A") == true) movie.Runtime else getString(R.string.runtime,
                movie.Runtime?.split(" ", limit = 2)?.first() ?: "N/A"
            )
            rating.text = if(movie.imdbRating?.equals("N/A") == true) movie.imdbRating else getString(R.string.rating, movie.imdbRating)
            date.text = movie.Released
            genre.text = movie.Genre
            plot.text = movie.Plot
            actors.text = movie.Actors
            director.text = movie.Director
        }
    }
}