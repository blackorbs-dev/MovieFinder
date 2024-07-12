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

import androidx.test.filters.SmallTest
import blackorbs.dev.moviefinder.util.MovieUtil.testMovie
import blackorbs.dev.moviefinder.services.remote.MovieApiService
import blackorbs.dev.moviefinder.util.TestExtensions.enqueueResponse
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
@SmallTest
class MovieApiServiceTest {

    private val mockWebServer = MockWebServer()
    private lateinit var movieApiService: MovieApiService

    @Before
    fun setUp(){
        movieApiService = Retrofit.Builder().baseUrl(mockWebServer.url("/")).addConverterFactory(GsonConverterFactory.create()).build().create(MovieApiService::class.java)
    }

    @Test
    fun `get Movie Remote`() = runTest {
        mockWebServer.enqueueResponse("movie-200.json",200)
        val imdb = "tt0944947"
        val actual = movieApiService.getMovie(imdb).body()
        val request = mockWebServer.takeRequest()
        assertTrue(request.requestLine.contains("?i=$imdb"))
        val expected = testMovie(imdb, "Game of Thrones", "https://m.media-amazon.com/images/M/MV5BN2IzYzBiOTQtNGZmMi00NDI5LTgxMzMtN2EzZjA1NjhlOGMxXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_SX300.jpg",
            "2011–2019","17 Apr 2011","Action, Adventure, Drama",
            "Nine noble families fight for control over the lands of Westeros, while an ancient enemy returns after being dormant for millennia.",
            "Emilia Clarke, Peter Dinklage, Kit Harington", "N/A","57 min","9.2")
        assertEquals(expected, actual)
    }

    @Test
    fun `get Movies List Remote`() = runTest {
        mockWebServer.enqueueResponse("movies-all-200.json",200)
        val actual = movieApiService.getMovies("query", "10").Search
        val request = mockWebServer.takeRequest()
        assertTrue(request.requestLine.contains("?s=query&page=10"))
        val expected = listOf( testMovie("tt0944947", "Game of Thrones", "https://m.media-amazon.com/images/M/MV5BN2IzYzBiOTQtNGZmMi00NDI5LTgxMzMtN2EzZjA1NjhlOGMxXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_SX300.jpg", "2011–2019"),
            testMovie("tt2084970", "The Imitation Game", "https://m.media-amazon.com/images/M/MV5BNjI3NjY1Mjg3MV5BMl5BanBnXkFtZTgwMzk5MDQ3MjE@._V1_SX300.jpg", "2014")
        )
        assertEquals(expected, actual)
    }

    @After
    fun cleanUp(){
        mockWebServer.shutdown()
    }
}