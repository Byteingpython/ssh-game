package de.byteingpython.sshGame.ssh.auth;

import com.sshtools.common.ssh.components.SshPublicKey;

import java.util.Optional;

public interface CredentialProvider {
    Optional<String> getHashedPassword(String username);

    Optional<SshPublicKey> getPublicKey(String username);

    boolean doesUserExist(String username);

    void createUserWithHashedPassword(String username, String passwordHash);

    void createUser(String username, SshPublicKey publicKey);

    void updatePasswordHash(String username, String passwordHash);

    void updateUserKey(String username, SshPublicKey publicKey);
}
