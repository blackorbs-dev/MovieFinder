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

package blackorbs.dev.moviefinder.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import blackorbs.dev.moviefinder.services.local.MovieDao
import blackorbs.dev.moviefinder.services.remote.MovieService
import blackorbs.dev.moviefinder.models.Movie
import retrofit2.HttpException
import java.io.IOException

class MoviePagingSource(private val searchQuery: String, private val movieService: MovieService, private val localDatabase: MovieDao): PagingSource<Int, Movie>() {

    private var localData: List<Movie> = emptyList()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return try {
            var movies: List<Movie> = emptyList()
            if(page == 1) {
                movies = localDatabase.getMovies(searchQuery).also { localData = it }
            }
            if(movies.isEmpty()) movieService.getMovies(searchQuery,"$page").Search?.let {
                movies = it.filter { movie ->
                    !localData.contains(movie)
                }
            }
            LoadResult.Page(movies, null, if(movies.isEmpty()) null else page+1)
        }
        catch (exception: IOException){
            return LoadResult.Error(exception)
        }
        catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}