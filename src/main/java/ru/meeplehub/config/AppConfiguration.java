package ru.meeplehub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    private final String appName;
    private final String appVersion;

    public AppConfiguration(@Value("${app.name}") String appName,
                            @Value("${app.version}") String appVersion) {
        this.appName = appName;
        this.appVersion = appVersion;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }
}
