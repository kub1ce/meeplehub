package ru.meeplehub.controller.dto;

import ru.meeplehub.domain.entity.Registration;
import ru.meeplehub.domain.enums.RegistrationStatus;

import java.time.LocalDateTime;

public record RegistrationResponse(
        Long id,
        LocalDateTime registeredAt,
        RegistrationStatus status,
        String comment,
        Long userId,
        String username,
        Long eventId,
        String eventTitle
) {
    public static RegistrationResponse from(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getRegisteredAt(),
                registration.getStatus(),
                registration.getComment(),
                registration.getUser().getId(),
                registration.getUser().getUsername(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle()
        );
    }
}