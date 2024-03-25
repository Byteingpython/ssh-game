package de.byteingpython.sshGame.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class EnvConfigurationProvider implements ConfigurationProvider {

    Logger logger = LoggerFactory.getLogger(EnvConfigurationProvider.class);

    @Override
    public Optional<Integer> getInt(String key) {
        String value = System.getenv(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            logger.error("Could not parse value for key " + key + " as integer: " + value);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        String value = System.getenv(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(Boolean.parseBoolean(value));
    }

    @Override
    public Optional<String> getString(String key) {
        String value = System.getenv(key);
        return Optional.ofNullable(value);
    }
}