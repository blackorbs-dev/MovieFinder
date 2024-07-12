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

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import blackorbs.dev.moviefinder.models.Movie
import blackorbs.dev.moviefinder.models.Resource
import blackorbs.dev.moviefinder.services.local.MovieDao
import blackorbs.dev.moviefinder.services.remote.MovieApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

open class MovieDataSource(private val movieApiService: MovieApiService, private val localDatabase: MovieDao, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {

    open fun getMovie(imdb: String) : LiveData<Resource<Movie>> = liveData(ioDispatcher) {
        emit(Resource.loading())
        val cachedData = localDatabase.getMovie(imdb).apply { if(isNotEmpty()) emit(Resource.success(first())) }
        try {
            val response = movieApiService.getMovie(imdb)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.success(it))
                    localDatabase.add(it)
                    return@liveData
                }
            }
            if(cachedData.isEmpty()) emit(error(" ${response.code()} ${response.message()}"))
        } catch (e: IOException) {
            if(cachedData.isEmpty()) emit(error(e.message ?: e.toString()))
        } catch (e: HttpException) {
            if(cachedData.isEmpty()) emit(error(e.message ?: e.toString()))
        }
    }

    open fun error(message: String): Resource<Movie> {
        Timber.e(message)
        return Resource.error(message)
    }
}