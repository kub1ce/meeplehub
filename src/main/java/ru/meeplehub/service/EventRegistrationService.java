package ru.meeplehub.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.meeplehub.domain.entity.Event;
import ru.meeplehub.domain.entity.Registration;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.EventStatus;
import ru.meeplehub.domain.enums.RegistrationStatus;
import ru.meeplehub.repository.EventRepository;
import ru.meeplehub.repository.RegistrationRepository;
import ru.meeplehub.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class EventRegistrationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EntityManager entityManager;

    public EventRegistrationService(
            UserRepository userRepository,
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            EntityManager entityManager) {

        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Registration registerForEvent(
            Long userId,
            Long eventId,
            String comment) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Пользователь не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Встреча не найдена."));

        if (event.getStatus() != EventStatus.PLANNED) {
            throw new IllegalStateException(
                    "Запись на эту встречу недоступна.");
        }

        if (event.getRegisteredCount() >= event.getMaxParticipants()) {
            throw new IllegalStateException(
                    "На встречу больше нет свободных мест.");
        }

        if (registrationRepository.existsByUser_IdAndEvent_Id(
                userId, eventId)) {
            throw new IllegalStateException(
                    "Вы уже записаны на эту встречу.");
        }

        event.setRegisteredCount(event.getRegisteredCount() + 1);
        eventRepository.save(event);

        entityManager.flush();

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.setComment(comment);

        return registrationRepository.save(registration);
    }
}