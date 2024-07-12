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

package blackorbs.dev.moviefinder.database.local

import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import blackorbs.dev.moviefinder.services.local.LocalDatabase
import blackorbs.dev.moviefinder.services.local.MovieDao
import blackorbs.dev.moviefinder.util.MovieUtil.testMovie
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@SmallTest
class MovieDaoTest {
    private lateinit var database: LocalDatabase
    private lateinit var movieDao: MovieDao

    @Before
    fun setUp(){
        database = androidx.room.Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LocalDatabase::class.java).build()
        movieDao = database.movieDao()
    }

    @Test
    fun addAndGetMovieFromLocalSourceSuccessful() = runTest {
        val imdb = "tt1092737"
        assertTrue(movieDao.getMovie(imdb).isEmpty())
        val movie = testMovie(imdb, "MyName")
        movieDao.add(movie)
        assertEquals(movie, movieDao.getMovie(imdb)[0])
        movieDao.add(testMovie(imdb, "MyNameUpdate"))
        assertEquals("MyNameUpdate", movieDao.getMovie(imdb)[0].Title)
    }

    @Test
    fun getPagedMoviesSuccessful() = runBlocking{
        ('a'..'z').mapIndexed {i,d -> testMovie("$i","$d") }
            .forEach { movieDao.add(it) }
        assertEquals(10, movieDao.getAll(0).size)
        assertEquals(10, movieDao.getAll(1).size)
        assertEquals(6, movieDao.getAll(2).size)
        assertEquals(1, movieDao.getMovies("a",0).size)
    }

    @After
    fun cleanUp(){
        database.close()
    }
}