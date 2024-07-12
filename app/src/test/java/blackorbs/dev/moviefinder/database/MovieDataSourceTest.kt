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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import blackorbs.dev.moviefinder.database.local.FakeMovieDao
import blackorbs.dev.moviefinder.database.remote.FakeApiService
import blackorbs.dev.moviefinder.models.Resource
import blackorbs.dev.moviefinder.services.MovieDataSource
import blackorbs.dev.moviefinder.util.MainCoroutineRule
import blackorbs.dev.moviefinder.util.MovieUtil.testMovie
import blackorbs.dev.moviefinder.util.TestExtensions.captureValues
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@SmallTest
class MovieDataSourceTest {
    // Run tasks synchronously
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var movieDataSource: MovieDataSource
    private lateinit var movieDao: FakeMovieDao

    @Before
    fun setUp(){
        movieDao = FakeMovieDao()
        movieDataSource = MovieDataSource(FakeApiService(), movieDao, mainCoroutineRule.testDispatcher)
    }

    @Test
    fun `get New Movie From Local Or Remote IOError Handled`() = runTest{
        val imdb = FakeApiService.IO_EXCEPTION
        val data = movieDataSource.getMovie(imdb)
        data.captureValues {
            runCurrent()
            assertEquals(arrayListOf(Resource.Status.LOADING, Resource.Status.ERROR), values.map { it?.status })
        }
    }

    @Test
    fun `get New Movie From Local Or Remote HttpError Handled `() = runTest{
        val imdb = FakeApiService.HTTP_EXCEPTION
        val data = movieDataSource.getMovie(imdb)
        data.captureValues {
            runCurrent()
            assertEquals(arrayListOf(Resource.Status.LOADING, Resource.Status.ERROR), values.map { it?.status })
        }
    }

    @Test
    fun `get New Movie From Local Or Remote NullResponseError Handled`() = runTest{
        val imdb = "null"
        val data = movieDataSource.getMovie(imdb)
        data.captureValues {
            runCurrent()
            assertEquals(arrayListOf(Resource.Status.LOADING, Resource.Status.ERROR), values.map { it?.status })
        }
    }

    @Test
    fun `get New Movie From Local Or Remote UnsuccessfulResponse Handled`() = runTest{
        val imdb = "error"
        val data = movieDataSource.getMovie(imdb)
        data.captureValues {
            runCurrent()
            assertEquals(arrayListOf(Resource.Status.LOADING, Resource.Status.ERROR), values.map { it?.status })
        }
    }

    @Test
    fun `get New Movie From Local failed but Remote Successful, and Saved`() = runTest{
        val imdb = "tt0944947"
        val data = movieDataSource.getMovie(imdb)
        data.captureValues {
            runCurrent()
            assertEquals(arrayListOf(Resource.Status.LOADING, Resource.Status.SUCCESS), values.map { it?.status })
        }
        assertEquals(imdb, data.value!!.data!!.imdbID)
        assertEquals(imdb, movieDao.getMovie(imdb)[0].imdbID)
    }

    @Test
    fun `get Movie Update From Local and Remote Successful`() = runTest{
        val imdb = "tt0944947"
        movieDao.add(testMovie(imdb))
        val data = movieDataSource.getMovie(imdb)
        data.captureValues {
            runCurrent()
            assertEquals(arrayListOf(Resource.Status.LOADING, Resource.Status.SUCCESS, Resource.Status.SUCCESS), values.map { it?.status })
        }
    }

    @Test
    fun `get Movie Update From Local Success but Remote Failed, Error Handled and Not Returned`() = runTest{
        val imdb = FakeApiService.IO_EXCEPTION
        movieDao.add(testMovie(imdb))
        val data = movieDataSource.getMovie(imdb)
        data.captureValues {
            runCurrent()
            assertEquals(arrayListOf(Resource.Status.LOADING, Resource.Status.SUCCESS), values.map { it?.status })
        }
    }
}