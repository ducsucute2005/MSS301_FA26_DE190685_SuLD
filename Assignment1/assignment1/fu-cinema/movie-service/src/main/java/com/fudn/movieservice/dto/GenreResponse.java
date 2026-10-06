package com.fudn.movieservice.dto;

import com.fudn.movieservice.model.Genre;

public record GenreResponse(String genreId, String genreName, String description) {

    public static GenreResponse from(Genre genre) {
        return new GenreResponse(genre.getGenreId(), genre.getGenreName(), genre.getDescription());
    }
}
