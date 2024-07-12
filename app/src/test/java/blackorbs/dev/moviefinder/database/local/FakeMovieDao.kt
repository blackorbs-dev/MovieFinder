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

package blackorbs.dev.moviefinder.database.local

import blackorbs.dev.moviefinder.models.Movie
import blackorbs.dev.moviefinder.services.local.MovieDao

class FakeMovieDao : MovieDao {
    private val movies = mutableListOf<Movie>()

    override suspend fun add(movie: Movie) {
        movies.add(movie)
    }

    override suspend fun getMovie(imdb: String): List<Movie> {
        return movies.filter { it.imdbID == imdb }.toList()
    }

    override suspend fun getMovies(searchQuery: String, page: Int, limit: Int): List<Movie> {
        return pagedList(page, limit)
    }

    override suspend fun getAll(page: Int, limit: Int): List<Movie> {
        return pagedList(page, limit)
    }

    private fun pagedList(page: Int, limit: Int): List<Movie> {
        val offset = limit*page
        val endIndex = offset+limit
        return if (movies.isEmpty() || offset >= movies.size) emptyList()
        else if(endIndex > movies.size) movies.subList(offset,movies.size)
        else movies.subList(offset,endIndex)
    }
}