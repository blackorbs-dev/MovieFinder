package blackorbs.dev.moviefinder.di

import blackorbs.dev.moviefinder.services.remote.MovieService
import blackorbs.dev.moviefinder.services.remote.RemoteDataSource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson) : Retrofit = Retrofit.Builder()
        .baseUrl("https://www.omdbapi.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client).build()

    private val client get() = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
        val request = chain.request().newBuilder()
        request.url(chain.request().url.newBuilder().addQueryParameter("apikey", "4640c4b1").build())
        chain.proceed(request.build())
    }).build()

    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    fun provideMovieService(retrofit: Retrofit): MovieService = retrofit.create(MovieService::class.java)

    @Singleton
    @Provides
    fun provideRemoteDataSource(movieService: MovieService): RemoteDataSource = RemoteDataSource(movieService)

}