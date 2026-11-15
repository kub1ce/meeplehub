package ru.meeplehub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.meeplehub.controller.dto.RegistrationResponse;
import ru.meeplehub.controller.dto.ReviewResponse;
import ru.meeplehub.domain.enums.RegistrationStatus;
import ru.meeplehub.repository.RegistrationRepository;
import ru.meeplehub.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/custom")
@Tag(name = "Custom search", description = "Поиск записей и отзывов")
public class CustomSearchController {

    private final RegistrationRepository registrationRepository;
    private final ReviewRepository reviewRepository;

    public CustomSearchController(
            RegistrationRepository registrationRepository,
            ReviewRepository reviewRepository) {

        this.registrationRepository = registrationRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/registrations")
    @Operation(summary = "Поиск записей по статусу и периоду")
    public List<RegistrationResponse> findRegistrations(
            @RequestParam("status") RegistrationStatus status,
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to) {

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Начало периода не может быть позже его окончания.");
        }

        return registrationRepository
                .findByStatusAndRegisteredAtBetween(status, from, to)
                .stream()
                .map(RegistrationResponse::from)
                .toList();
    }

    @GetMapping("/reviews")
    @Operation(summary = "Поиск отзывов по автору")
    public List<ReviewResponse> findReviews(
            @RequestParam("username") String username) {

        if (username.isBlank()) {
            throw new IllegalArgumentException(
                    "Логин автора не может быть пустым.");
        }

        return reviewRepository
                .findReviewsByAuthorUsername(username)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }
}