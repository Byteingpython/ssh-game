package de.byteingpython.sshGame.ssh.auth;

import java.security.PublicKey;
import java.util.Optional;

public interface CredentialProvider {
    Optional<String> getHashedPassword(String username);

    Optional<PublicKey> getPublicKey(String username);

    boolean doesUserExist(String username);

    void createUserWithHashedPassword(String username, String passwordHash);

    void createUser(String username, PublicKey publicKey);

    void updatePasswordHash(String username, String passwordHash);

    void updateUserKey(String username, PublicKey publicKey);
}
