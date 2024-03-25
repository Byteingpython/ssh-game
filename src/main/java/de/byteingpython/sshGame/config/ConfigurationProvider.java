package de.byteingpython.sshGame.config;

import java.util.Optional;

public interface ConfigurationProvider {
    Optional<Integer> getInt(String key);

    Optional<Boolean> getBoolean(String key);

    Optional<String> getString(String key);
}
