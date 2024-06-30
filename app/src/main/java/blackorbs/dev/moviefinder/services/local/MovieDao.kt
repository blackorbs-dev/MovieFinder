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

package blackorbs.dev.moviefinder.services.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import blackorbs.dev.moviefinder.models.Movie

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(movie: Movie)

    @Query("SELECT * FROM movies WHERE imdbID=:imdb")
    fun getMovie(imdb: String): List<Movie>

    @Query("SELECT * FROM movies WHERE Title LIKE '%' || :searchQuery || '%'")
    suspend fun getMovies(searchQuery:String): List<Movie>

    @Query("SELECT * FROM movies")
    suspend fun getAll(): List<Movie>
}