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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import blackorbs.dev.moviefinder.R
import blackorbs.dev.moviefinder.databinding.MovieListItemBinding
import blackorbs.dev.moviefinder.models.Movie
import coil.load

class ListAdapter: PagingDataAdapter<Movie, ListAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        MovieListItemBinding.inflate(LayoutInflater.from(parent.context))
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object{
        const val IMDB_KEY = "imdbID"
        val diffCallback = object : ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
                return oldItem.imdbID == newItem.imdbID
            }

            override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
                return oldItem.Title == newItem.Title
            }
        }
    }

    inner class ViewHolder(private val binding: MovieListItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(movie: Movie?){
            with(binding){
                movie?.let {
                    image.load(movie.Poster){
                        val placeholder = AppCompatResources.getDrawable(root.context,R.drawable.placeholder)
                        placeholder(placeholder)
                        error(placeholder)
                    }
                    root.setOnClickListener {
                        binding.root.findNavController()
                            .navigate(R.id.action_searchPage_to_moviePage,
                                bundleOf(IMDB_KEY to movie.imdbID)
                            )
                    }
                }
                title.text = root.context.getString(R.string.title, movie?.Title, movie?.Year)
            }
        }
    }
}