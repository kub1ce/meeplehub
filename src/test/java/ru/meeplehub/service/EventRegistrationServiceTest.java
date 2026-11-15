package ru.meeplehub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.meeplehub.domain.entity.*;
import ru.meeplehub.domain.enums.*;
import ru.meeplehub.repository.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class EventRegistrationServiceTest {

    @Autowired
    private EventRegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardGameRepository boardGameRepository;

    @Autowired
    private EventRepository eventRepository;

    @MockitoSpyBean
    private RegistrationRepository registrationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private User user;
    private Event event;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        reviewRepository.deleteAll();
        eventRepository.deleteAll();
        boardGameRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setUsername("player");
        user.setEmail("player@example.com");
        user.setDisplayName("Player");
        user.setRegisteredAt(LocalDateTime.now());
        user.setRole(UserRole.MEMBER);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        BoardGame game = new BoardGame();
        game.setTitle("Dixit");
        game.setDescription("Test game");
        game.setMinPlayers(3);
        game.setMaxPlayers(6);
        game.setDurationMinutes(30);
        game.setMinAge(8);
        game = boardGameRepository.save(game);

        event = new Event();
        event.setTitle("Dixit evening");
        event.setDescription("Board game meeting");
        event.setStartsAt(LocalDateTime.now().plusDays(1));
        event.setEndsAt(LocalDateTime.now().plusDays(1).plusHours(2));
        event.setLocation("Игариум");
        event.setMaxParticipants(5);
        event.setRegisteredCount(0);
        event.setStatus(EventStatus.PLANNED);
        event.setOrganizer(user);
        event.setBoardGame(game);
        event = eventRepository.save(event);
    }

    @Test
    void shouldRegisterUserAndIncreaseEventCount() {
        Registration registration =
                registrationService.registerForEvent(
                        user.getId(),
                        event.getId(),
                        "Буду вовремя"
                );

        assertNotNull(registration.getId());
        assertEquals(1, registrationRepository.count());

        Event updatedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(1, updatedEvent.getRegisteredCount());
    }

    @Test
    void shouldRollbackEventUpdateWhenRegistrationFails() {

        doThrow(new DataIntegrityViolationException(
                "Test database error"))
                .when(registrationRepository)
                .save(any(Registration.class));

        assertThrows(DataIntegrityViolationException.class,
                () -> registrationService.registerForEvent(
                        user.getId(),
                        event.getId(),
                        "Буду вовремя"
                ));

        Event updatedEvent = eventRepository
                .findById(event.getId())
                .orElseThrow();

        assertEquals(0, updatedEvent.getRegisteredCount());
        assertEquals(0, registrationRepository.count());
    }
}