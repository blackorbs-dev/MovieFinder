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

package blackorbs.dev.moviefinder.viewmodels

import androidx.lifecycle.asFlow
import androidx.paging.testing.asSnapshot
import androidx.test.filters.SmallTest
import blackorbs.dev.moviefinder.database.FakeRepository
import blackorbs.dev.moviefinder.ui.moviescreen.MovieViewModel
import blackorbs.dev.moviefinder.ui.searchscreen.SearchViewModel
import blackorbs.dev.moviefinder.util.MainCoroutineRule
import blackorbs.dev.moviefinder.util.MovieUtil.testMovie
import blackorbs.dev.moviefinder.util.TestExtensions.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@SmallTest
class MovieAndSearchViewModelsTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var movieViewModel: MovieViewModel
    private lateinit var searchViewModel: SearchViewModel
    private val movies = listOf(testMovie("imdb"), testMovie("imdb2"))

    @Before
    fun setUp(){
        movieViewModel = MovieViewModel(FakeRepository(movies))
        searchViewModel = SearchViewModel(FakeRepository(movies))
    }

    @Test
    fun `observe movie new data`() = runTest{
        movieViewModel.getMovie("")
        assertEquals(movies[0], movieViewModel.movie.getOrAwaitValue{advanceUntilIdle()}.data)
    }

    @Test
    fun `observe movies list new data`() = runTest{
        searchViewModel.getMovies("")
        searchViewModel.movies.getOrAwaitValue { advanceUntilIdle() }
        assertEquals(movies, searchViewModel.movies.asFlow().asSnapshot())
    }
}