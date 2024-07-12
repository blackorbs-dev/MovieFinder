package blackorbs.dev.moviefinder.di

import blackorbs.dev.moviefinder.repository.BaseRepository
import blackorbs.dev.moviefinder.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MainModule {

    @Singleton
    @Binds
    abstract fun bindMovieRepository(repository: MovieRepository): BaseRepository

}