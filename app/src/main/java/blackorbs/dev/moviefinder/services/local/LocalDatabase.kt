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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import blackorbs.dev.moviefinder.models.Movie

@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class LocalDatabase: RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object{
        @Volatile private var instance:LocalDatabase? = null

        fun getLocalDB(context: Context): LocalDatabase =
                instance ?: synchronized(this) { instance ?: buildDB(context).also { instance = it } }

        private fun buildDB(context: Context) = Room.databaseBuilder(context, LocalDatabase::class.java, "movies")
            .fallbackToDestructiveMigration()
            .build()
    }
}