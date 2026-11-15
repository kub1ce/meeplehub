package ru.meeplehub.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.meeplehub.domain.entity.*;
import ru.meeplehub.domain.enums.*;
import ru.meeplehub.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class MeepleHubCriteriaDaoTest {

    @Autowired
    private MeepleHubCriteriaDao criteriaDao;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardGameRepository boardGameRepository;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        reviewRepository.deleteAll();
        eventRepository.deleteAll();
        boardGameRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldFindRegistrationsByStatusAndPeriod() {
        LocalDateTime now = LocalDateTime.now();

        User player1 = createUser("player1");
        User player2 = createUser("player2");
        User player3 = createUser("player3");

        BoardGame game = createGame("Dixit");
        Event event = createEvent(player1, game);

        Registration expected = createRegistration(
                player1, event,
                RegistrationStatus.CONFIRMED,
                now.minusDays(1)
        );

        createRegistration(
                player2, event,
                RegistrationStatus.CANCELLED,
                now.minusHours(1)
        );

        createRegistration(
                player3, event,
                RegistrationStatus.CONFIRMED,
                now.plusDays(1)
        );

        LocalDateTime from = now.minusDays(2);
        LocalDateTime to = now;

        List<Registration> springDataResult =
                registrationRepository.findByStatusAndRegisteredAtBetween(
                        RegistrationStatus.CONFIRMED, from, to);

        List<Registration> criteriaResult =
                criteriaDao.findRegistrationsByStatusAndPeriod(
                        RegistrationStatus.CONFIRMED, from, to);

        assertEquals(Set.of(expected.getId()), registrationIds(springDataResult));
        assertEquals(registrationIds(springDataResult),
                registrationIds(criteriaResult));
    }

    @Test
    void shouldFindReviewsByAuthorUsername() {
        User alice = createUser("alice");
        User bob = createUser("bob");

        BoardGame game = createGame("Catan");

        Review expected = createReview(alice, game);
        createReview(bob, game);

        List<Review> jpqlResult =
                reviewRepository.findReviewsByAuthorUsername("alice");

        List<Review> criteriaResult =
                criteriaDao.findReviewsByAuthorUsername("alice");

        assertEquals(Set.of(expected.getId()), reviewIds(jpqlResult));
        assertEquals(reviewIds(jpqlResult), reviewIds(criteriaResult));
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setDisplayName(username);
        user.setRegisteredAt(LocalDateTime.now());
        user.setRole(UserRole.MEMBER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    private BoardGame createGame(String title) {
        BoardGame game = new BoardGame();
        game.setTitle(title);
        game.setDescription("Test game");
        game.setMinPlayers(2);
        game.setMaxPlayers(4);
        game.setDurationMinutes(60);
        game.setMinAge(8);
        game.setPublisher("Test Publisher");
        game.setCategory("Strategy");

        return boardGameRepository.save(game);
    }

    private Event createEvent(User organizer, BoardGame game) {
        Event event = new Event();
        event.setTitle("Test event");
        event.setDescription("Test description");
        event.setStartsAt(LocalDateTime.now().plusDays(1));
        event.setEndsAt(LocalDateTime.now().plusDays(1).plusHours(2));
        event.setLocation("Club");
        event.setMaxParticipants(10);
        event.setRegisteredCount(0);
        event.setStatus(EventStatus.PLANNED);
        event.setOrganizer(organizer);
        event.setBoardGame(game);

        return eventRepository.save(event);
    }

    private Registration createRegistration(
            User user,
            Event event,
            RegistrationStatus status,
            LocalDateTime registeredAt) {

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(status);
        registration.setRegisteredAt(registeredAt);

        return registrationRepository.save(registration);
    }

    private Review createReview(User user, BoardGame game) {
        Review review = new Review();
        review.setUser(user);
        review.setBoardGame(game);
        review.setRating(5);
        review.setText("Хорошая игра!");
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    private Set<Long> registrationIds(List<Registration> registrations) {
        return registrations.stream()
                .map(Registration::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> reviewIds(List<Review> reviews) {
        return reviews.stream()
                .map(Review::getId)
                .collect(Collectors.toSet());
    }
}