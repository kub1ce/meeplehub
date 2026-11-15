package ru.meeplehub.controller.dto;

import ru.meeplehub.domain.entity.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        int rating,
        String text,
        LocalDateTime createdAt,
        Long userId,
        String username,
        Long boardGameId,
        String boardGameTitle
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getText(),
                review.getCreatedAt(),
                review.getUser().getId(),
                review.getUser().getUsername(),
                review.getBoardGame().getId(),
                review.getBoardGame().getTitle()
        );
    }
}