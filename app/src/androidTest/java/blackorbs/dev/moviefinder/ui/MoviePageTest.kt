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
import blackorbs.dev.moviefinder.ui.moviescreen.MoviePage
import blackorbs.dev.moviefinder.ui.moviescreen.MoviePageArgs
import blackorbs.dev.moviefinder.util.MockWebServerDispatcher
import blackorbs.dev.moviefinder.util.MovieUtil.testMovie
import blackorbs.dev.moviefinder.util.TestHelper.IO_EXCEPTION
import blackorbs.dev.moviefinder.util.TestHelper.assertClickable
import blackorbs.dev.moviefinder.util.TestHelper.assertDisplayed
import blackorbs.dev.moviefinder.util.TestHelper.assertDisplayedWithScroll
import blackorbs.dev.moviefinder.util.TestHelper.assertNotDisplayed
import blackorbs.dev.moviefinder.util.TestHelper.assertViewRemoved
import blackorbs.dev.moviefinder.util.TestHelper.getImageLoader
import blackorbs.dev.moviefinder.util.TestHelper.launchFragmentInHiltContainer
import blackorbs.dev.moviefinder.util.TestHelper.performClick
import coil.Coil
import com.jakewharton.espresso.OkHttp3IdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@MediumTest
class MoviePageTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val mockWebServer = MockWebServer()

    @Inject lateinit var okHttp: OkHttpClient
    @Inject lateinit var localDatabase: MovieDao

    private lateinit var okHttp3IdlingResource: OkHttp3IdlingResource
    private lateinit var context: Context

    private val movie = testMovie("tt0944947", "Game of Thrones", "https://m.media-amazon.com/images/M/MV5BN2IzYzBiOTQtNGZmMi00NDI5LTgxMzMtN2EzZjA1NjhlOGMxXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_SX300.jpg",
        "2011–2019","17 Apr 2011","Action, Adventure, Drama",
        "Nine noble families fight for control over the lands of Westeros, while an ancient enemy returns after being dormant for millennia.",
        "Emilia Clarke, Peter Dinklage, Kit Harington", "N/A","57 min","9.2")
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
    fun showMovieDetailsPageFromRemoteSuccessAndSavedToLocal() {
        runBlocking {
            assertTrue(localDatabase.getMovie(movie.imdbID).isEmpty())
        }
        Coil.setImageLoader(getImageLoader(context))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(movie.imdbID)
        launchFragmentInHiltContainer<MoviePage>(MoviePageArgs(movie.imdbID).toBundle(), R.style.Theme_MovieFinder)
        assertDisplayed(R.id.image, ColorDrawable(Color.BLUE))
        assertClickable(R.id.back_btn)
        assertDisplayedWithScroll(R.id.title, movie.Title)
        assertDisplayed(R.id.title_divider)
        assertDisplayedWithScroll(R.id.runtime,movie.Runtime+"utes")
        assertDisplayedWithScroll(R.id.rating,movie.imdbRating+"/10 (IMDb)")
        assertDisplayedWithScroll(R.id.date_title,"Released Date")
        assertDisplayedWithScroll(R.id.date, movie.Released)
        assertDisplayedWithScroll(R.id.genre_title,"Genre")
        assertDisplayedWithScroll(R.id.genre, movie.Genre)
        assertDisplayedWithScroll(R.id.plot_title,"Plot")
        assertDisplayedWithScroll(R.id.plot, movie.Plot)
        assertDisplayedWithScroll(R.id.actors_title,"Starring")
        assertDisplayedWithScroll(R.id.actors, movie.Actors)
        assertDisplayedWithScroll(R.id.director_title,"Directed by")
        assertDisplayedWithScroll(R.id.director, movie.Director)
        runBlocking {
            assertEquals(movie, localDatabase.getMovie(movie.imdbID)[0])
        }
        Thread.sleep(delay)
    }

    @Test
    fun loadFromLocalDatabaseSuccessRemoteErrorNotShown(){
        runBlocking { localDatabase.add(movie) }
        Coil.setImageLoader(getImageLoader(context))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(IO_EXCEPTION)
        launchFragmentInHiltContainer<MoviePage>(MoviePageArgs(movie.imdbID).toBundle(), R.style.Theme_MovieFinder)
        assertDisplayed(R.id.image, ColorDrawable(Color.BLUE))
        assertClickable(R.id.back_btn)
        assertDisplayedWithScroll(R.id.title, movie.Title)
        assertDisplayed(R.id.title_divider)
        assertDisplayedWithScroll(R.id.runtime,movie.Runtime+"utes")
        assertDisplayedWithScroll(R.id.rating,movie.imdbRating+"/10 (IMDb)")
        assertDisplayedWithScroll(R.id.date_title,"Released Date")
        assertDisplayedWithScroll(R.id.date, movie.Released)
        assertDisplayedWithScroll(R.id.genre_title,"Genre")
        assertDisplayedWithScroll(R.id.genre, movie.Genre)
        assertDisplayedWithScroll(R.id.plot_title,"Plot")
        assertDisplayedWithScroll(R.id.plot, movie.Plot)
        assertDisplayedWithScroll(R.id.actors_title,"Starring")
        assertDisplayedWithScroll(R.id.actors, movie.Actors)
        assertDisplayedWithScroll(R.id.director_title,"Directed by")
        assertDisplayedWithScroll(R.id.director, movie.Director)
        assertViewRemoved(com.google.android.material.R.id.snackbar_text)
        assertViewRemoved(com.google.android.material.R.id.snackbar_action)
        Thread.sleep(delay)
    }

    @Test
    fun showMovieDetailsPageErrorHandled() {
        mockWebServer.dispatcher = MockWebServerDispatcher().ErrorDispatcher()
        launchFragmentInHiltContainer<MoviePage>(MoviePageArgs(movie.imdbID).toBundle(), R.style.Theme_MovieFinder)
        assertDisplayed(R.id.image, R.drawable.placeholder)
        assertClickable(R.id.back_btn)
        assertNotDisplayed(R.id.title,"")
        assertDisplayed(R.id.title_divider)
        assertNotDisplayed(R.id.runtime,"")
        assertNotDisplayed(R.id.rating,"")
        assertNotDisplayed(R.id.date_title,"Released Date")
        assertNotDisplayed(R.id.date,"")
        assertNotDisplayed(R.id.genre_title,"Genre")
        assertNotDisplayed(R.id.genre,"")
        assertNotDisplayed(R.id.plot_title,"Plot")
        assertNotDisplayed(R.id.plot,"")
        assertNotDisplayed(R.id.actors_title,"Starring")
        assertNotDisplayed(R.id.actors,"")
        assertNotDisplayed(R.id.director_title,"Directed by")
        assertNotDisplayed(R.id.director,"")
        assertDisplayed(com.google.android.material.R.id.snackbar_text, context.getString(R.string.error_try_again))
        assertDisplayed(com.google.android.material.R.id.snackbar_action, context.getString(R.string.retry))
        performClick(com.google.android.material.R.id.snackbar_action)
        assertDisplayed(com.google.android.material.R.id.snackbar_text, context.getString(R.string.error_try_again))
        Thread.sleep(delay)
    }

    @Test
    fun showMovieDetailsPageFailedRetrySuccess() {
        mockWebServer.dispatcher = MockWebServerDispatcher().ErrorDispatcher()
        launchFragmentInHiltContainer<MoviePage>(MoviePageArgs(movie.imdbID).toBundle(), R.style.Theme_MovieFinder)
        assertDisplayed(R.id.image, R.drawable.placeholder)
        assertClickable(R.id.back_btn)
        assertNotDisplayed(R.id.title,"")
        assertDisplayed(R.id.title_divider)
        assertNotDisplayed(R.id.runtime,"")
        assertNotDisplayed(R.id.rating,"")
        assertDisplayed(com.google.android.material.R.id.snackbar_text, context.getString(R.string.error_try_again))
        assertDisplayed(com.google.android.material.R.id.snackbar_action, context.getString(R.string.retry))
        //Retry
        Coil.setImageLoader(getImageLoader(context))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(movie.imdbID)
        performClick(com.google.android.material.R.id.snackbar_action)
        assertViewRemoved(com.google.android.material.R.id.snackbar_text)
        assertViewRemoved(com.google.android.material.R.id.snackbar_action)
        assertDisplayed(R.id.image, ColorDrawable(Color.BLUE))
        assertClickable(R.id.back_btn)
        assertDisplayedWithScroll(R.id.title,movie.Title)
        assertDisplayed(R.id.title_divider)
        assertDisplayedWithScroll(R.id.runtime,movie.Runtime+"utes")
        assertDisplayedWithScroll(R.id.rating,movie.imdbRating+"/10 (IMDb)")
        assertDisplayedWithScroll(R.id.date_title,"Released Date")
        assertDisplayedWithScroll(R.id.date, movie.Released)
        Thread.sleep(delay)
    }

    @Test
    fun showMovieDetailsPageImageLoadErrorHandled() {
        Coil.setImageLoader(getImageLoader(context, IOException()))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(movie.imdbID)
        launchFragmentInHiltContainer<MoviePage>(MoviePageArgs(movie.imdbID).toBundle())
        assertDisplayed(R.id.image, R.drawable.placeholder)
        Thread.sleep(delay)
    }

    @After
    fun cleanUp(){
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(okHttp3IdlingResource)
    }
}