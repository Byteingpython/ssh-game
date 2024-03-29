package de.byteingpython.sshGame.ssh.auth;

import org.slf4j.Logger;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.security.PublicKey;
import java.util.Optional;

public abstract class CredentialAuthProvider implements CredentialProvider, AuthProvider {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
    private final Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(32, 64, 1, 19 * 1024, 2);

    @Override
    public Optional<Boolean> authenticate(String username, String password) {
        Optional<String> hashedPassword = getHashedPassword(username);
        if (hashedPassword.isPresent()) {
            return Optional.of(argon2PasswordEncoder.matches(password, hashedPassword.get()));
        }
        return Optional.empty();
    }

    //TODO: Implement this method
    @Override
    public Optional<Boolean> authenticate(String username, PublicKey publicKey) {
        return Optional.empty();
    }

    @Override
    public boolean isCreationPossible() {
        return true;
    }

    @Override
    public void createUser(String username, String password) {
        createUserWithHashedPassword(username, argon2PasswordEncoder.encode(password));
    }
}
