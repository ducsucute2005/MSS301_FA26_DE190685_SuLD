package com.fudn.movieservice.dto;

import com.fudn.movieservice.model.AgeRating;
import com.fudn.movieservice.model.Movie;
import com.fudn.movieservice.model.MovieStatus;

import java.time.LocalDate;

public record MovieResponse(String movieId, String title, String description, String director,
                            Integer durationMinutes, String language, AgeRating ageRating,
                            LocalDate releaseDate, String genreId, String genreName, MovieStatus movieStatus) {

    public static MovieResponse from(Movie movie, String genreName) {
        return new MovieResponse(movie.getMovieId(), movie.getTitle(), movie.getDescription(), movie.getDirector(),
                movie.getDurationMinutes(), movie.getLanguage(), movie.getAgeRating(), movie.getReleaseDate(),
                movie.getGenreId(), genreName, movie.getMovieStatus());
    }
}
