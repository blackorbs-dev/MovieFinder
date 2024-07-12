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

package blackorbs.dev.moviefinder.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import blackorbs.dev.moviefinder.R
import blackorbs.dev.moviefinder.services.local.MovieDao
import blackorbs.dev.moviefinder.ui.searchscreen.ListLoadStateAdapter
import blackorbs.dev.moviefinder.ui.searchscreen.SearchPage
import blackorbs.dev.moviefinder.util.MockWebServerDispatcher
import blackorbs.dev.moviefinder.util.MovieUtil.testMovie
import blackorbs.dev.moviefinder.util.TestHelper.assertDisplayed
import blackorbs.dev.moviefinder.util.TestHelper.assertItemCount
import blackorbs.dev.moviefinder.util.TestHelper.assertItemDisplayed
import blackorbs.dev.moviefinder.util.TestHelper.assertItemDisplayedAndClickable
import blackorbs.dev.moviefinder.util.TestHelper.assertNotDisplayed
import blackorbs.dev.moviefinder.util.TestHelper.getImageLoader
import blackorbs.dev.moviefinder.util.TestHelper.launchFragmentInHiltContainer
import blackorbs.dev.moviefinder.util.TestHelper.performItemClick
import blackorbs.dev.moviefinder.util.TestHelper.performSearch
import blackorbs.dev.moviefinder.util.TestHelper.scrollTo
import coil.Coil
import com.jakewharton.espresso.OkHttp3IdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@MediumTest
class SearchPageTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val mockWebServer = MockWebServer()

    @Inject lateinit var okHttp: OkHttpClient
    @Inject lateinit var localDatabase: MovieDao

    private lateinit var okHttp3IdlingResource: OkHttp3IdlingResource
    private lateinit var context: Context

    private val query = "game"
    private val delay = 0L

    @Before
    fun setUp(){
        hiltRule.inject()
        okHttp3IdlingResource = OkHttp3IdlingResource.create("okhttp", okHttp)
        IdlingRegistry.getInstance().register(okHttp3IdlingResource)
        context = ApplicationProvider.getApplicationContext()
        mockWebServer.start(8080)
    }


    @Test
    fun searchAndLoadFromRemoteSuccessThenScrollToLoadMoreUntilEmpty(){
        launchFragmentInHiltContainer<SearchPage>(null, R.style.Theme_MovieFinder)
        assertDisplayed(R.id.search_title, context.getString(R.string.find_movies))
        assertNotDisplayed(R.id.loading)
        assertDisplayed(R.id.no_result,  context.getString(R.string.no_results, context.getString(R.string.start_search)))
        Coil.setImageLoader(getImageLoader(context))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(query)
        performSearch(query)
        assertItemCount(20) //10 items preloaded
        remoteMovies.forEachIndexed{ i, m ->
            assertItemDisplayedAndClickable(i,"${m.Title} (${m.Year})", ColorDrawable(Color.BLUE))
        }
        assertNotDisplayed(R.id.no_result)
        scrollTo(20)
        assertDisplayed(R.id.search_title, context.getString(R.string.find_movies))
        assertItemCount(40) //20 more items preloaded
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher("empty") //let server return empty
        scrollTo(34); scrollTo(39) //to simulate real user scrolling
        assertItemCount(40)
        Thread.sleep(delay)
    }

    @Test
    fun loadAllFromLocalAtAppStartEnsureNoDuplicateThenNewSearchFromLocalAndRemote(){
        runBlocking { localMovies.forEach{localDatabase.add(it)} }
        Coil.setImageLoader(getImageLoader(context, IOException())) // load image error
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(query)
        launchFragmentInHiltContainer<SearchPage>(null, R.style.Theme_MovieFinder)
        assertDisplayed(R.id.search_title, context.getString(R.string.find_movies))
        assertNotDisplayed(R.id.no_result)
        assertItemCount(10)
        localMovies.forEachIndexed{i,m-> assertItemDisplayedAndClickable(i,"${m.Title} (${m.Year})", R.drawable.placeholder) }
        scrollTo(10)
        assertItemCount(10)
        scrollTo(5)
        performSearch(query)
        assertItemCount(25) //5 from local 20 from remote
        localMovies.subList(0,5).forEachIndexed{i,m-> assertItemDisplayedAndClickable(i,"${m.Title} (${m.Year})", R.drawable.placeholder) } //first five local items title contains the query
        remoteMovies.forEachIndexed{i,m-> assertItemDisplayedAndClickable(i+5,"${m.Title} (${m.Year})", R.drawable.placeholder) }
        Thread.sleep(delay)
    }

    @Test
    fun searchErrorHandledAndRetrySuccess(){
        launchFragmentInHiltContainer<SearchPage>(null, R.style.Theme_MovieFinder)
        assertDisplayed(R.id.no_result,  context.getString(R.string.no_results, context.getString(R.string.start_search)))
        mockWebServer.dispatcher = MockWebServerDispatcher().ErrorDispatcher()
        performSearch(query)
        assertItemCount(0)
        assertDisplayed(com.google.android.material.R.id.snackbar_text, context.getString(R.string.error_try_again))
        assertDisplayed(R.id.no_result,  context.getString(R.string.no_results, context.getString(R.string.start_search)))
        Coil.setImageLoader(getImageLoader(context))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(query)
        performSearch(query)
        assertItemCount(20)
        assertNotDisplayed(R.id.no_result)
        Thread.sleep(delay)
    }

    @Test
    fun searchSuccessLoadMoreErrorHandledAndRetrySuccess(){
        launchFragmentInHiltContainer<SearchPage>(null, R.style.Theme_MovieFinder)
        Coil.setImageLoader(getImageLoader(context))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(query)
        performSearch(query)
        assertItemCount(20)
        mockWebServer.dispatcher = MockWebServerDispatcher().ErrorDispatcher()
        scrollTo(15); scrollTo(19) //simulate real user scrolling
        assertItemCount(21) //load state error added
        assertItemDisplayed(20, context.getString(R.string.error_try_again))
        performItemClick<ListLoadStateAdapter.ViewHolder>(20, R.id.retry_btn)
        assertItemCount(21) //load state error still
        assertItemDisplayed(20, context.getString(R.string.error_try_again))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(query)
        performItemClick<ListLoadStateAdapter.ViewHolder>(20, R.id.retry_btn)
        assertItemCount(40) //more items loaded
        Thread.sleep(delay)
    }

    @Test
    fun searchNoResultFound(){
        launchFragmentInHiltContainer<SearchPage>(null, R.style.Theme_MovieFinder)
        assertDisplayed(R.id.no_result,  context.getString(R.string.no_results, context.getString(R.string.start_search)))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher("empty")
        performSearch(query)
        assertItemCount(0)
        assertDisplayed(R.id.no_result,  context.getString(R.string.no_results, context.getString(R.string.try_another_query)))
        Thread.sleep(delay)
    }

    @After
    fun cleanUp(){
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(okHttp3IdlingResource)
    }


    private val localMovies = listOf(
        testMovie("tt1903222", "T of Light game", "https://m.media-amazon.com/images/M/MV5BN2IzYzBiOTQtNGZmMi00NDI5LTgxMzMtN2EzZjA1NjhlOGMxXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_SX300.jpg",
            "2911–2019"),
        testMovie("tt3994892", "The C Game", "https://m.media-amazon.com/images/M/MV5BNjI3NjY1Mjg3MV5BMl5BanBnXkFtZTgwMzk5MDQ3MjE@._V1_SX300.jpg",
            "2190"),
        testMovie("tt1093833", "Sorry G Game", "https://m.media-amazon.com/images/M/MV5BYWE3MDVkN2EtNjQ5MS00ZDQ4LTliNzYtMjc2YWMzMDEwMTA3XkEyXkFqcGdeQXVyMTEzMTI1Mjk3._V1_SX300.jpg",
            "2028"),
        testMovie("tt3847430", "My Homies: A Game of Love", "https://m.media-amazon.com/images/M/MV5BMTQwMzQ5Njk1MF5BMl5BanBnXkFtZTcwNjIxNzIxNw@@._V1_SX300.jpg",
            "1882"),
        testMovie("tt2093844", "Diary of Game", "https://m.media-amazon.com/images/M/MV5BNWQ2ODFhNWItNTA4NS00MzkyLTgyYzUtZjlhYWE5MmEzY2Q1XkEyXkFqcGdeQXVyMjUzOTY1NTc@._V1_SX300.jpg",
            "8921"),
        testMovie("tt2023484", "What a Chance Night", "https://m.media-amazon.com/images/M/MV5BMjI3ODkzNDk5MF5BMl5BanBnXkFtZTgwNTEyNjY2NDM@._V1_SX300.jpg",
            "2291"),
        testMovie("tt5764932", "Beginner's Chance ", "https://m.media-amazon.com/images/M/MV5BMjAzMzI5OTgzMl5BMl5BanBnXkFtZTgwMTU5MTAwMDE@._V1_SX300.jpg",
            "1999"),
        testMovie("tt9984037", " Octopus Chance", "https://m.media-amazon.com/images/M/MV5BNTkzMzRlYjEtMTQ5Yi00OWY3LWI0NzYtNGQ4ZDkzZTU0M2IwXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_SX300.jpg",
            "2000"),
        testMovie("tt3802122", "Next Door Chance", "https://m.media-amazon.com/images/M/MV5BNjNhOGZkNzktMGU3NC00ODk2LWE4NjctZTliN2JjZTQxZmIxXkEyXkFqcGdeQXVyNDk3NzU2MTQ@._V1_SX300.jpg",
            "2032"),
        testMovie("tt5665654", " in the house Chance", "https://m.media-amazon.com/images/M/MV5BMzg0NGE0N2MtYTg1My00NTBkLWI5NjEtZTgyMDA0MTU4MmIyXkEyXkFqcGdeQXVyMTU2NTcyMg@@._V1_SX300.jpg",
            "2033")
    )

    private val remoteMovies = listOf(
        testMovie("tt0944947", "Game of Thrones", "https://m.media-amazon.com/images/M/MV5BN2IzYzBiOTQtNGZmMi00NDI5LTgxMzMtN2EzZjA1NjhlOGMxXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_SX300.jpg",
            "2011–2019"),
        testMovie("tt2084970", "The Imitation Game", "https://m.media-amazon.com/images/M/MV5BNjI3NjY1Mjg3MV5BMl5BanBnXkFtZTgwMzk5MDQ3MjE@._V1_SX300.jpg",
            "2014"),
        testMovie("tt10919420", "Squid Game", "https://m.media-amazon.com/images/M/MV5BYWE3MDVkN2EtNjQ5MS00ZDQ4LTliNzYtMjc2YWMzMDEwMTA3XkEyXkFqcGdeQXVyMTEzMTI1Mjk3._V1_SX300.jpg",
            "2021–"),
        testMovie("tt1515091", "Sherlock Holmes: A Game of Shadows", "https://m.media-amazon.com/images/M/MV5BMTQwMzQ5Njk1MF5BMl5BanBnXkFtZTcwNjIxNzIxNw@@._V1_SX300.jpg",
            "2011"),
        testMovie("tt0119174", "The Game", "https://m.media-amazon.com/images/M/MV5BNWQ2ODFhNWItNTA4NS00MzkyLTgyYzUtZjlhYWE5MmEzY2Q1XkEyXkFqcGdeQXVyMjUzOTY1NTc@._V1_SX300.jpg",
            "1997"),
        testMovie("tt2704998", "Game Night", "https://m.media-amazon.com/images/M/MV5BMjI3ODkzNDk5MF5BMl5BanBnXkFtZTgwNTEyNjY2NDM@._V1_SX300.jpg",
            "2018"),
        testMovie("tt1731141", "Ender's Game", "https://m.media-amazon.com/images/M/MV5BMjAzMzI5OTgzMl5BMl5BanBnXkFtZTgwMTU5MTAwMDE@._V1_SX300.jpg",
            "2013"),
        testMovie("tt4209788", "Molly's Game", "https://m.media-amazon.com/images/M/MV5BNTkzMzRlYjEtMTQ5Yi00OWY3LWI0NzYtNGQ4ZDkzZTU0M2IwXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_SX300.jpg",
            "2017"),
        testMovie("tt0266987", "Spy Game", "https://m.media-amazon.com/images/M/MV5BNjNhOGZkNzktMGU3NC00ODk2LWE4NjctZTliN2JjZTQxZmIxXkEyXkFqcGdeQXVyNDk3NzU2MTQ@._V1_SX300.jpg",
            "2001"),
        testMovie("tt3748172", "Gerald's Game", "https://m.media-amazon.com/images/M/MV5BMzg0NGE0N2MtYTg1My00NTBkLWI5NjEtZTgyMDA0MTU4MmIyXkEyXkFqcGdeQXVyMTU2NTcyMg@@._V1_SX300.jpg",
            "2017")
    )
}