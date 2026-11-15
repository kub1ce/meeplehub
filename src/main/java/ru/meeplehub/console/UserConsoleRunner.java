package ru.meeplehub.console;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.meeplehub.domain.entity.User;
import ru.meeplehub.domain.enums.UserStatus;
import ru.meeplehub.service.UserService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

@ConditionalOnProperty(
        prefix = "app.console",
        name = "enabled",
        havingValue = "true"
)
@Component
public class UserConsoleRunner implements CommandLineRunner {

    private final UserService userService;

    public UserConsoleRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        System.out.println();
        System.out.println("=== MeepleHub | управление пользователями ===");
        System.out.println("Введите номер действия. Для завершения консольного режима выберите 0.");
        printMenu();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running && scanner.hasNextLine()) {
            printMenu();
            String command = scanner.nextLine().trim();
            try {
                switch (command) {
                    case "1" -> createUser(scanner);
                    case "2" -> printUsers(userService.findAll());
                    case "3" -> findUser(scanner);
                    case "4" -> updateUser(scanner);
                    case "5" -> changeStatus(scanner);
                    case "6" -> deleteUser(scanner);
                    case "0" -> {
                        System.out.println("Консольное меню закрыто. Actuator продолжает работать на порту 8080.");
                        running = false;
                    }
                    default -> System.out.println("Неизвестная команда. Выберите пункт от 0 до 6.");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Ошибка: " + exception.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("\n1. Зарегистрировать пользователя");
        System.out.println("2. Показать всех пользователей");
        System.out.println("3. Найти пользователя по ID");
        System.out.println("4. Изменить профиль пользователя");
        System.out.println("5. Изменить статус пользователя");
        System.out.println("6. Удалить пользователя");
        System.out.println("0. Завершить консольный режим");
        System.out.print("> ");
    }

    private void createUser(Scanner scanner) {
        System.out.print("Логин: ");
        String username = readLine(scanner);
        System.out.print("Email: ");
        String email = readLine(scanner);
        System.out.print("Отображаемое имя: ");
        String displayName = readLine(scanner);

        System.out.print("Пароль: ");
        String password = readLine(scanner);

        User created = userService.registerUser(
                username,
                email,
                displayName,
                password
        );
        System.out.println("Пользователь зарегистрирован: " + created);
    }

    private void printUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("Список пользователей пуст.");
            return;
        }
        users.forEach(System.out::println);
    }

    private void findUser(Scanner scanner) {
        long id = readId(scanner);
        Optional<User> user = userService.findById(id);
        System.out.println(user.map(Object::toString).orElse("Пользователь не найден."));
    }

    private void updateUser(Scanner scanner) {
        long id = readId(scanner);
        System.out.print("Новый email: ");
        String email = readLine(scanner);
        System.out.print("Новое отображаемое имя: ");
        String displayName = readLine(scanner);
        System.out.println("Профиль обновлён: " + userService.updateProfile(id, email, displayName));
    }

    private void changeStatus(Scanner scanner) {
        long id = readId(scanner);
        System.out.print("Новый статус (ACTIVE/BLOCKED): ");
        String value = readLine(scanner).toUpperCase(Locale.ROOT);
        UserStatus status;
        try {
            status = UserStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Допустимые статусы: ACTIVE, BLOCKED.");
        }
        System.out.println("Статус изменён: " + userService.changeStatus(id, status));
    }

    private void deleteUser(Scanner scanner) {
        long id = readId(scanner);
        userService.deleteById(id);
        System.out.println("Пользователь удалён.");
    }

    private long readId(Scanner scanner) {
        System.out.print("ID пользователя: ");
        String value = readLine(scanner);
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException("ID must be positive");
            }
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("ID должен быть положительным целым числом.");
        }
    }

    private String readLine(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            throw new IllegalArgumentException("Ввод завершён.");
        }
        return scanner.nextLine();
    }
}
