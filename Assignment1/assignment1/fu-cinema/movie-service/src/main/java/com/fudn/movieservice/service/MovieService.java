package com.fudn.movieservice.service;

import com.fudn.movieservice.model.Movie;
import com.fudn.movieservice.model.MovieStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MongoTemplate mongoTemplate;

    public List<Movie> search(String keyword, String genreId, MovieStatus status) {
        Query query = new Query();
        if (keyword != null && !keyword.isBlank()) {
            query.addCriteria(Criteria.where("title").regex(Pattern.quote(keyword.trim()), "i"));
        }
        if (genreId != null && !genreId.isBlank()) {
            query.addCriteria(Criteria.where("genreId").is(genreId));
        }
        if (status != null) {
            query.addCriteria(Criteria.where("movieStatus").is(status));
        }
        query.with(Sort.by("title"));
        return mongoTemplate.find(query, Movie.class);
    }
}
