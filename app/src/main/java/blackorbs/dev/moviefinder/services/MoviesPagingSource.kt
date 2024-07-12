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

package blackorbs.dev.moviefinder.services

import androidx.paging.PagingSource
import androidx.paging.PagingState
import blackorbs.dev.moviefinder.models.Movie
import blackorbs.dev.moviefinder.services.local.MovieDao
import blackorbs.dev.moviefinder.services.remote.MovieApiService
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class MoviesPagingSource(private val searchQuery: String, private val movieApiService: MovieApiService, private val localDatabase: MovieDao, private val localData: MutableList<Movie> = mutableListOf()): PagingSource<Int, Movie>() {

    private var isRemoteLoading = false

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        var page = params.key ?: 0
        return try {
            var movies: List<Movie> = emptyList()
            if(page == 0 || searchQuery.isEmpty() || (localData.isNotEmpty() && localData.size % 10 == 0 && !isRemoteLoading)){
                (if(searchQuery.isEmpty()) localDatabase.getAll(page) else localDatabase.getMovies(searchQuery, page)).also {
                    if(it.isNotEmpty() && localData.map { m->m.imdbID } != it.map { m->m.imdbID }) {
                        movies = it
                        if(searchQuery.isNotEmpty()){
                            if(page==0) localData.clear()
                            localData.addAll(it)
                        }
                    }
                }
                if(movies.isEmpty()) page = 1 else if (movies.size < 10) page = -2
            }
            if(movies.isEmpty() && searchQuery.isNotEmpty()) {
                isRemoteLoading = true
                movieApiService.getMovies(searchQuery,"${if(page==-1) 1 else page}").Search?.let {
                    if(it.isNotEmpty()){
                        movies = it.filter {
                            movie -> !localData.any {localMovie -> localMovie.imdbID == movie.imdbID}
                        }
                        if(page==1 && localData.all { movie -> movie.Title!!.contains(searchQuery, true) }){
                            movies = localData.plus(movies)
                        }
                        if(page==-1)page=1
                    }
                }
            }
            LoadResult.Page(movies, null, if(movies.isEmpty() || (searchQuery.isEmpty() && page == -2)) null else page+1)
        }
        catch (e: IOException){
            return error(e)
        }
        catch (e: HttpException) {
            return error(e)
        }
    }

    private fun error(e: Exception): LoadResult<Int,Movie> {
        Timber.e(e.message)
        return LoadResult.Error(e)
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}