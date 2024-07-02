package blackorbs.dev.moviefinder.di

import blackorbs.dev.moviefinder.services.remote.MovieApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val API_KEY = "" //TODO: add api key

    @Singleton
    @Provides
    fun provideMovieService() : MovieApiService = Retrofit.Builder()
        .baseUrl("https://www.omdbapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client).build().create(MovieApiService::class.java)

    private val client get() = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
        val request = chain.request().newBuilder()
        request.url(chain.request().url.newBuilder().addQueryParameter("apikey", API_KEY).build())
        chain.proceed(request.build())
    }).build()

}