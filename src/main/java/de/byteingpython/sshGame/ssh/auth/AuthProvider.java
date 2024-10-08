package de.byteingpython.sshGame.ssh.auth;

import java.security.PublicKey;
import java.util.Optional;

public interface AuthProvider {
    Optional<Boolean> authenticate(String username, String password);

    Optional<Boolean> authenticate(String username, PublicKey publicKey);

    boolean doesUserExist(String username);

    boolean isCreationPossible();

    void createUser(String username, String password);

    void createUser(String username, PublicKey publicKey);
}
