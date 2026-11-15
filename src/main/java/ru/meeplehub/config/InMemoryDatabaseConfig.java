package ru.meeplehub.config;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.meeplehub.domain.entity.User;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class InMemoryDatabaseConfig {

    @Bean(name = "userStorage")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public List<User> userStorage() {
        return new ArrayList<>();
    }
}
