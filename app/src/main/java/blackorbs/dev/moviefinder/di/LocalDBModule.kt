package blackorbs.dev.moviefinder.di

import android.content.Context
import blackorbs.dev.moviefinder.services.local.LocalDatabase
import blackorbs.dev.moviefinder.services.local.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDBModule {

    @Singleton
    @Provides
    fun localDB(@ApplicationContext context: Context): LocalDatabase = LocalDatabase.getLocalDB(context)

    @Singleton
    @Provides
    fun movieDao(localDB: LocalDatabase): MovieDao = localDB.movieDao()
}