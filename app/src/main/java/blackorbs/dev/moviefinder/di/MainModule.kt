package blackorbs.dev.moviefinder.di

import blackorbs.dev.moviefinder.repository.MovieRepository
import blackorbs.dev.moviefinder.services.local.MovieDao
import blackorbs.dev.moviefinder.services.remote.RemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Singleton
    @Provides
    fun provideRepository(remoteDataSource: RemoteDataSource, localDatabase: MovieDao) = MovieRepository(remoteDataSource, localDatabase)

}