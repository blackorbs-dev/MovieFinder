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
import androidx.lifecycle.liveData
import blackorbs.dev.moviefinder.models.Movie
import blackorbs.dev.moviefinder.models.Resource
import kotlinx.coroutines.Dispatchers

fun executeGetMovie(databaseQuery: () -> List<Movie>,
                           networkCall: suspend () -> Resource<Movie>,
                           saveCallResult: suspend (Movie) -> Unit): LiveData<Resource<Movie>> =
    liveData(Dispatchers.IO) {
        emit(Resource.loading())
        val source = databaseQuery.invoke().apply { if(isNotEmpty()) emit(Resource.success(first())) }
        val responseStatus = networkCall.invoke()
        if (responseStatus.status == Resource.Status.SUCCESS) {
            emit(Resource.success(responseStatus.data!!))
            saveCallResult(responseStatus.data)
        }
        else if (responseStatus.status == Resource.Status.ERROR) {
            if(source.isEmpty()) {
                emit(Resource.error(responseStatus.message!!))
            }
        }
    }