package de.byteingpython.sshGame.ssh.auth;

import de.byteingpython.sshGame.utils.throttling.Throttler;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PasswordAuthenticator implements org.apache.sshd.server.auth.password.PasswordAuthenticator {
    private final AuthProvider authProvider;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Throttler throttler;

    public PasswordAuthenticator(AuthProvider authProvider, Throttler throttler) {
        this.authProvider = authProvider;
        this.throttler = throttler;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession serverSession) throws PasswordChangeRequiredException, AsyncAuthException {
        if (!throttler.isAllowed(username)) {
            logger.trace("User " + username + " is throttled");
            return false;
        }
        logger.trace("Authenticating user " + username);
        Optional<Boolean> result = authProvider.authenticate(username, password);
        logger.trace("User " + username + " authenticated: " + result.orElse(false));
        if (result.isEmpty() && authProvider.isCreationPossible()) {
            logger.trace("User " + username + " does not exist, creating user");
            authProvider.createUser(username, password);
            return true;
        }
        if (!result.orElse(false)) {
            throttler.throttle(username);
        }
        return result.orElse(false);
    }
}
