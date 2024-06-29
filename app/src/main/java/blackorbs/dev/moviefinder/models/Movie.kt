package blackorbs.dev.moviefinder.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie (
    @PrimaryKey val imdbID: String,
    val Title: String?,
    val Poster: String?,
    val Year: String?,
    val Released: String?,
    val Genre: String?,
    val Plot: String?,
    val Actors: String?,
    val Director: String?
)