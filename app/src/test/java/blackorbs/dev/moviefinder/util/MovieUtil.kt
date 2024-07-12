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

package blackorbs.dev.moviefinder.util

import blackorbs.dev.moviefinder.models.Movie

object MovieUtil {
    fun testMovie(imdbID: String, title: String? = null, poster: String? = null, year: String? = null, released: String? = null, genre: String? = null,
                  plot: String? = null, actors: String? = null, director: String? = null, runtime: String? = null, imdbRating: String? = null
    ): Movie {
        return Movie (imdbID, title, poster, year, released, genre, plot, actors, director, runtime, imdbRating)
    }
}