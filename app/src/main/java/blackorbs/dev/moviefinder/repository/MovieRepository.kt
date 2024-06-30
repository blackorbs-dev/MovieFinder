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

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import blackorbs.dev.moviefinder.models.Movie
import blackorbs.dev.moviefinder.services.local.MovieDao
import blackorbs.dev.moviefinder.services.remote.RemoteDataSource
import javax.inject.Inject

class MovieRepository @Inject constructor(private val remoteDataSource: RemoteDataSource, private val localDatabase: MovieDao){

    fun getMovie(imdb: String) = executeGetMovie(
        databaseQuery = {localDatabase.getMovie(imdb)},
        networkCall = {remoteDataSource.getMovie(imdb)},
        saveCallResult = { localDatabase.add(it)}
    )

    fun getMovies(searchQuery: String) : LiveData<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = 10, prefetchDistance = 5, initialLoadSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { MoviesPagingSource(searchQuery, remoteDataSource.movieApiService, localDatabase) }
    ).liveData

}