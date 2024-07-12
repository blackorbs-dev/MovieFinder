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

package blackorbs.dev.moviefinder.database.remote

import blackorbs.dev.moviefinder.models.Movie
import blackorbs.dev.moviefinder.models.MovieList
import blackorbs.dev.moviefinder.services.remote.MovieApiService
import blackorbs.dev.moviefinder.util.MovieUtil.testMovie
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class FakeApiService : MovieApiService {
    private val limit = 10
    private val movies = ('z' downTo 'a').mapIndexed { i, d -> testMovie("${i+100}", "remote$d") }

    companion object{
        const val IO_EXCEPTION = "IO_EXCEPTION"
        const val HTTP_EXCEPTION = "HTTP_EXCEPTION"
    }

    override suspend fun getMovies(searchQuery: String, page: String): MovieList {
        return when(searchQuery) {
            IO_EXCEPTION -> throw IOException()
            HTTP_EXCEPTION -> throw HttpException(Response.error<Movie>(404,searchQuery.toResponseBody()))
            "empty" -> MovieList(emptyList())
            "null" -> MovieList(null)
            else -> {
                val offset = limit*(page.toInt()-1) //actual OMDb movie list page start from 1 but fake/local data loading start from 0 (index)
                val endIndex = offset+limit
                if (movies.isEmpty() || offset >= movies.size) MovieList(emptyList())
                else if (endIndex > movies.size) MovieList(movies.subList(offset,movies.size))
                else MovieList(movies.subList(offset,endIndex))
            }
        }
    }

    override suspend fun getMovie(imdb: String): Response<Movie> {
        return when(imdb) {
            IO_EXCEPTION -> throw IOException()
            HTTP_EXCEPTION -> throw HttpException(Response.error<Movie>(404,imdb.toResponseBody()))
            "error" -> Response.error(400, imdb.toResponseBody("text/html".toMediaTypeOrNull()))
            "null" -> Response.success(null)
            else -> Response.success(testMovie(imdb))
        }
    }
}