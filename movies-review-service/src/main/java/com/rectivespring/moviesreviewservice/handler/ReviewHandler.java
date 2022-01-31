package com.rectivespring.moviesreviewservice.handler;

import com.rectivespring.moviesreviewservice.domain.Review;
import com.rectivespring.moviesreviewservice.exception.ReviewDataException;
import com.rectivespring.moviesreviewservice.exception.ReviewNotFoundException;
import com.rectivespring.moviesreviewservice.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    Sinks.Many<Review> reviewSinks = Sinks.many().replay().latest();

    @Autowired
    private Validator validator;

    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .doOnNext(review -> reviewSinks.tryEmitNext(review))
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);

    }

    private void validate(Review review) {

        var constraintViolations = validator.validate(review);
        log.info("constraintViolations : {}", constraintViolations);
        if (constraintViolations.size() > 0) {
            var errorMessage = constraintViolations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);

        }

    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");

        if (movieInfoId.isPresent()) {
            var reviewsFlux = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
            return buildReviewsResponse(reviewsFlux);
        } else {
            var reviewsFlux = reviewReactiveRepository.findAll();
            return buildReviewsResponse(reviewsFlux);
        }


    }

    private Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {

        var reviewId = request.pathVariable("id");

        var existingReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id " + reviewId)));

        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {

        var reviewId = request.pathVariable("id");

        var existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId)
                        .flatMap(reviewDeleted -> ServerResponse.noContent().build()));

    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewSinks.asFlux(), Review.class)
                .log();
    }
}
