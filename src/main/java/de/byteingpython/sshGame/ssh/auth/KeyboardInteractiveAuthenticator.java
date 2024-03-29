package de.byteingpython.sshGame.ssh.auth;

import de.byteingpython.sshGame.utils.throttling.Throttler;
import org.apache.sshd.server.auth.keyboard.InteractiveChallenge;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KeyboardInteractiveAuthenticator implements org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator {
    private final AuthProvider authProvider;

    private final Throttler throttler;

    public KeyboardInteractiveAuthenticator(AuthProvider authProvider, Throttler throttler) {
        this.authProvider = authProvider;
        this.throttler = throttler;
    }

    @Override
    public InteractiveChallenge generateChallenge(ServerSession session, String username, String lang, String subMethods) {
        if (!throttler.isAllowed(username)) {
            return throttledChallenge();
        }
        if (authProvider.doesUserExist(username)) {
            return loginChallenge();
        } else if (authProvider.isCreationPossible()) {
            return createAccountChallenge();
        }
        return null;
    }

    @Override
    public boolean authenticate(ServerSession session, String username, List<String> responses) {
        if (responses.size() == 1) {
            if (!throttler.isAllowed(username)) {
                return false;
            }
            boolean authenticated = authProvider.authenticate(username, responses.get(0)).orElse(false);
            if (!authenticated) {
                throttler.throttle(username);
            }
            return authenticated;
        } else if (responses.size() == 2) {
            if (responses.get(0).equals(responses.get(1))) {
                authProvider.createUser(username, responses.get(0));
                return true;
            }
        }
        return false;
    }

    private InteractiveChallenge createAccountChallenge() {
        InteractiveChallenge challenge = new InteractiveChallenge();
        challenge.setInteractionName("Register");
        challenge.setInteractionInstruction("Create an account or cancel with Ctrl+C");
        challenge.addPrompt("Please enter your Password: ", false);
        challenge.addPrompt("Please enter your Password again: ", false);
        challenge.setLanguageTag("en");
        return challenge;
    }

    private InteractiveChallenge loginChallenge() {
        InteractiveChallenge challenge = new InteractiveChallenge();
        challenge.setInteractionName("Login");
        challenge.addPrompt("Please enter your Password: ", false);
        challenge.setLanguageTag("en");
        return challenge;
    }

    private InteractiveChallenge throttledChallenge() {
        InteractiveChallenge challenge = new InteractiveChallenge();
        challenge.setInteractionName("Throttled");
        challenge.setInteractionInstruction("Too many attempts. Please wait a bit before trying again");
        challenge.setLanguageTag("en");
        return challenge;
    }
}
