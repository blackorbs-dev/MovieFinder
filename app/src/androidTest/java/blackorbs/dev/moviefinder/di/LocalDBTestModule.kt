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

package blackorbs.dev.moviefinder.di

import android.content.Context
import androidx.room.Room
import blackorbs.dev.moviefinder.services.local.LocalDatabase
import blackorbs.dev.moviefinder.services.local.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [LocalDBModule::class]
)
object LocalDBTestModule {

    @Singleton
    @Provides
    fun provideLocalDB(@ApplicationContext context: Context): LocalDatabase =
        Room.inMemoryDatabaseBuilder(context, LocalDatabase::class.java).build()

    @Provides
    fun provideMovieDao(localDB: LocalDatabase): MovieDao = localDB.movieDao()
}