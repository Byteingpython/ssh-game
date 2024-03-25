package de.byteingpython.sshGame.ssh.auth;

import de.byteingpython.sshGame.ssh.auth.AuthProvider;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PasswordAuthenticator implements org.apache.sshd.server.auth.password.PasswordAuthenticator {
    private final AuthProvider authProvider;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PasswordAuthenticator(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession serverSession) throws PasswordChangeRequiredException, AsyncAuthException {
        logger.trace("Authenticating user " + username);
        Optional<Boolean> result = authProvider.authenticate(username, password);
        logger.trace("User " + username + " authenticated: " + result.orElse(false));
        if (result.isEmpty() && authProvider.isCreationPossible()) {
            logger.trace("User " + username + " does not exist, creating user");
            authProvider.createUser(username, password);
            return true;
        }
        return result.orElse(false);
    }
}
