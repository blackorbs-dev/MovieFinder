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
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import blackorbs.dev.moviefinder.R
import blackorbs.dev.moviefinder.services.local.MovieDao
import blackorbs.dev.moviefinder.ui.searchscreen.ListAdapter
import blackorbs.dev.moviefinder.util.MockWebServerDispatcher
import blackorbs.dev.moviefinder.util.MovieUtil.testMovie
import blackorbs.dev.moviefinder.util.TestHelper.assertClickable
import blackorbs.dev.moviefinder.util.TestHelper.assertDisplayed
import blackorbs.dev.moviefinder.util.TestHelper.assertDisplayedWithScroll
import blackorbs.dev.moviefinder.util.TestHelper.assertItemCount
import blackorbs.dev.moviefinder.util.TestHelper.getImageLoader
import blackorbs.dev.moviefinder.util.TestHelper.performClick
import blackorbs.dev.moviefinder.util.TestHelper.performItemClick
import blackorbs.dev.moviefinder.util.TestHelper.performSearch
import coil.Coil
import com.jakewharton.espresso.OkHttp3IdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val mockWebServer = MockWebServer()

    @Inject
    lateinit var okHttp: OkHttpClient
    @Inject
    lateinit var localDatabase: MovieDao

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
    fun goToAndFroMoviePage(){
        Coil.setImageLoader(getImageLoader(context))
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(query)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        performSearch(query)
        assertItemCount(20)
        mockWebServer.dispatcher = MockWebServerDispatcher().RequestDispatcher(movie.imdbID)
        performItemClick<ListAdapter.ViewHolder>(0,null)
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
        pressBack()
        performItemClick<ListAdapter.ViewHolder>(15,null)
        performClick(R.id.back_btn)
        Thread.sleep(delay)
        scenario.close()
    }

    @After
    fun cleanUp(){
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(okHttp3IdlingResource)
    }

    private val movie = testMovie("tt0944947", "Game of Thrones", "https://m.media-amazon.com/images/M/MV5BN2IzYzBiOTQtNGZmMi00NDI5LTgxMzMtN2EzZjA1NjhlOGMxXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_SX300.jpg",
        "2011–2019","17 Apr 2011","Action, Adventure, Drama",
        "Nine noble families fight for control over the lands of Westeros, while an ancient enemy returns after being dormant for millennia.",
        "Emilia Clarke, Peter Dinklage, Kit Harington", "N/A","57 min","9.2")
}