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

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import androidx.test.filters.SmallTest
import blackorbs.dev.moviefinder.database.local.FakeMovieDao
import blackorbs.dev.moviefinder.database.remote.FakeApiService
import blackorbs.dev.moviefinder.models.Movie
import blackorbs.dev.moviefinder.services.MoviesPagingSource
import blackorbs.dev.moviefinder.util.MovieUtil.testMovie
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@SmallTest
class MoviesPagingSourceTest {

    private val localMovies = ('a'..'z').mapIndexed { i, d -> testMovie("$i", "local$d") }
    private val remoteMovies = ('z' downTo 'a').mapIndexed { i, d -> testMovie("${i+100}", "remote$d") }
    private val config = PagingConfig(pageSize = 10, enablePlaceholders = false)

    @Test
    fun `get paged movies request from local, then remote when last loaded list from local is less than load size`() = runTest{
        //parameter query here does not matter
        val pager = TestPager(config, MoviesPagingSource("query", FakeApiService(), FakeMovieDao().apply { localMovies.forEach{add(it)} }))
        assertEquals(localMovies.subList(0,10), (pager.refresh() as LoadResult.Page).data)
        assertEquals(localMovies.subList(10,20), (pager.append() as LoadResult.Page).data)
        assertEquals(localMovies.subList(20,localMovies.size), (pager.append() as LoadResult.Page).data)
        assertTrue((pager.getLastLoadedPage() as LoadResult.Page).data.size < config.pageSize) // not up to load size continue from remote
        assertEquals(remoteMovies.subList(0,10), (pager.append() as LoadResult.Page).data)
        assertEquals(remoteMovies.subList(10,20), (pager.append() as LoadResult.Page).data)
    }

    @Test
    fun `get all paged movies request from local success, no remote loading`() = runTest{
        //parameter query here does not matter
        val pager = TestPager(config, MoviesPagingSource("", FakeApiService(), FakeMovieDao().apply { localMovies.forEach{add(it)} }))
        assertEquals(localMovies.subList(0,10), (pager.refresh() as LoadResult.Page).data)
        assertEquals(localMovies.subList(10,20), (pager.append() as LoadResult.Page).data)
        assertEquals(localMovies.subList(20,localMovies.size), (pager.append() as LoadResult.Page).data)
        assertTrue((pager.getLastLoadedPage() as LoadResult.Page).data.size < config.pageSize) // not up to load size continue from remote
        assertEquals(null, pager.append())
    }

    @Test
    fun `get paged movies request from local then remote, when the last loaded list from local is equal to requested load size`() = runTest{
        //parameter query here does not matter
        val pager = TestPager(config, MoviesPagingSource("query", FakeApiService(), FakeMovieDao().apply { localMovies.subList(0,20).forEach{add(it)} }))
        assertEquals(localMovies.subList(0,10), (pager.refresh() as LoadResult.Page).data)
        assertEquals(localMovies.subList(10,20), (pager.append() as LoadResult.Page).data)
        assertTrue((pager.getLastLoadedPage() as LoadResult.Page).data.size == 10)
        assertEquals("localt", pager.getLastLoadedPage()!!.data[pager.getLastLoadedPage()!!.data.size-1].Title) // localt is the last local item item, load next from remote
        assertEquals(remoteMovies.subList(0,10), (pager.append() as LoadResult.Page).data)
        assertEquals(remoteMovies.subList(10,20), (pager.append() as LoadResult.Page).data)
        assertEquals(remoteMovies.subList(20,remoteMovies.size), (pager.append() as LoadResult.Page).data)
        assertTrue((pager.getLastLoadedPage() as LoadResult.Page).data.size < config.pageSize) // load finished
        assertEquals(emptyList<Movie>(), (pager.append() as LoadResult.Page).data)
    }

    @Test
    fun `get paged movies through multiple requests from local then remote, add local data to remote data if search query is the same and ensure no duplicate`() = runTest{
        val localDataSave = mutableListOf<Movie>()
        val localData = ('a'..'o').mapIndexed { i, d -> testMovie("$i", "remote$d") } //create a new local data with search query in movie title
        var moviesSource = MoviesPagingSource(
            "remote", FakeApiService(),
            FakeMovieDao().apply { remoteMovies.subList(0,3).forEach{add(it)}; localData.forEach{add(it)} }, localDataSave
        )
        assertEquals(remoteMovies.subList(0,3)+localData.subList(0,7), (TestPager(config,moviesSource).refresh() as LoadResult.Page).data)
        moviesSource = MoviesPagingSource("remote", FakeApiService(), FakeMovieDao(), localDataSave)  //parameter "remote" matters
        assertEquals(localDataSave+remoteMovies.subList(3,10), (TestPager(config,moviesSource).refresh() as LoadResult.Page).data) // new request
    }

    @Test
    fun `get paged movies through duplicate requests from local then remote, ensure no duplicate with saved local data`() = runTest{
        val localDataSave = mutableListOf<Movie>()
        val moviesSource = MoviesPagingSource(
            "remote", FakeApiService(),
            FakeMovieDao().apply { localMovies.forEach{add(it)} }, localDataSave
        )
        assertEquals(localMovies.subList(0,10), (TestPager(config,moviesSource).refresh() as LoadResult.Page).data)
        assertEquals(remoteMovies.subList(0,10), (TestPager(config,moviesSource).refresh() as LoadResult.Page).data) // new duplicate request from local, but new search query so load remote only
    }

    @Test
    fun `get paged movies through multiple requests from local then remote, don't add local data to remote data if search query is not the same and ensure no duplicate`() = runTest{
        val localDataSave = mutableListOf<Movie>()
        val localData = ('a'..'o').mapIndexed { i, d -> testMovie("$i", "remote$d") }
        var moviesSource = MoviesPagingSource(
            "remote", FakeApiService(),
            FakeMovieDao().apply { remoteMovies.subList(0,3).forEach{add(it)}; localData.forEach{add(it)} }, localDataSave
        )
        assertEquals(remoteMovies.subList(0,3)+localData.subList(0,7), (TestPager(config,moviesSource).refresh() as LoadResult.Page).data)
        moviesSource = MoviesPagingSource("query", FakeApiService(), FakeMovieDao(), localDataSave)  //parameter should not be "remote"
        assertEquals(remoteMovies.subList(3,10), (TestPager(config,moviesSource).refresh() as LoadResult.Page).data) // new request
    }

    @Test
    fun `get paged movies request from local empty so request from remote until no more`() = runTest{
        //parameter query here does not matter
        val pager = TestPager(config, MoviesPagingSource("query", FakeApiService(), FakeMovieDao()))
        assertEquals(remoteMovies.subList(0,10), (pager.refresh() as LoadResult.Page).data)
        assertEquals(remoteMovies.subList(10,20), (pager.append() as LoadResult.Page).data)
        assertEquals(remoteMovies.subList(20,remoteMovies.size), (pager.append() as LoadResult.Page).data)
        assertTrue((pager.getLastLoadedPage() as LoadResult.Page).data.size < config.pageSize) // no next items
        assertEquals(emptyList<Movie>(), (pager.append() as LoadResult.Page).data)
    }

    @Test
    fun `get paged movies request from remote IO error handled`() = runTest{
        val moviesSource = MoviesPagingSource(FakeApiService.IO_EXCEPTION, FakeApiService(), FakeMovieDao())
        assertTrue(TestPager(config, moviesSource).refresh() is LoadResult.Error)
    }

    @Test
    fun `get paged movies request from remote HTTP error handled`() = runTest{
        val moviesSource = MoviesPagingSource(FakeApiService.HTTP_EXCEPTION, FakeApiService(), FakeMovieDao())
        assertTrue(TestPager(config, moviesSource).refresh() is LoadResult.Error)
    }

    @Test
    fun `get paged movies request from remote null list return handled`() = runTest{
        val moviesSource = MoviesPagingSource("null", FakeApiService(), FakeMovieDao())
        assertEquals(emptyList<Movie>(), (TestPager(config, moviesSource).refresh() as LoadResult.Page).data)
    }

    @Test
    fun `get paged movies request from remote empty list return handled`() = runTest{
        val moviesSource = MoviesPagingSource("empty", FakeApiService(), FakeMovieDao())
        assertEquals(emptyList<Movie>(), (TestPager(config, moviesSource).refresh() as LoadResult.Page).data)
    }

}