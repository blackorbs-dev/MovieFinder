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

package blackorbs.dev.moviefinder.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.PagingData
import blackorbs.dev.moviefinder.models.Movie
import blackorbs.dev.moviefinder.models.Resource
import blackorbs.dev.moviefinder.repository.BaseRepository

class FakeRepository(private val movies: List<Movie> = emptyList()) : BaseRepository {

    override fun getMovie(imdb: String): LiveData<Resource<Movie>> =  liveData{
        emit(Resource.success(movies[0]))
    }

    override fun getMovies(searchQuery: String): LiveData<PagingData<Movie>> = liveData{
        emit(PagingData.from(movies))
    }
}