### Movie Finder (blackorbs)
Find Movies from OMDb Database (https://www.omdbapi.com)

<p align="center">
  <img src="https://github.com/blackorbs-dev/MovieFinder/assets/62489500/960ea7de-161c-4038-aae2-74f47bd4efe5" width="300">
  <img src="https://github.com/blackorbs-dev/MovieFinder/assets/62489500/2c4643f1-0fc9-4a9a-9de9-92c0a78ddf17" width="300">
</p>

App utilises: 
* Jetpack Navigation Component
* Dagger Hilt
* Retrofit
* Room
* Kotlin Coroutine
* Pagination
* Clean Architecture Pattern

#### TODO: Set your OMDb api key in `NetworkModule.kt`:
```
object NetworkModule {
    private const val API_KEY = "" //TODO: add api key
    ....
}
```

