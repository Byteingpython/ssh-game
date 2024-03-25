package de.byteingpython.sshGame.ssh.auth;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;

import java.util.Optional;

public abstract class CredentialAuthProvider implements CredentialProvider, AuthProvider {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private final HashFunction hashing = Hashing.sha256();

    @Override
    public Optional<Boolean> authenticate(String username, String password) {
        Optional<String> hashedPassword = getHashedPassword(username);
        if (hashedPassword.isPresent()) {
            return Optional.of(getHashedPassword(username).get().equals(hashing.hashString(password, java.nio.charset.StandardCharsets.UTF_8).toString()));
        }
        return Optional.empty();
    }

    //TODO: Implement this method
    @Override
    public Optional<Boolean> authenticate(String username, java.security.PublicKey publicKey) {
        return Optional.empty();
    }

    @Override
    public boolean isCreationPossible() {
        return true;
    }

    @Override
    public void createUser(String username, String password) {
        createUserWithHashedPassword(username, hashing.hashString(password, java.nio.charset.StandardCharsets.UTF_8).toString());
    }
}
