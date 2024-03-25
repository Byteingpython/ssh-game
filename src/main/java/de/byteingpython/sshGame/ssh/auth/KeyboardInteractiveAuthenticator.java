package de.byteingpython.sshGame.ssh.auth;

import org.apache.sshd.server.auth.keyboard.InteractiveChallenge;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KeyboardInteractiveAuthenticator implements org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AuthProvider authProvider;

    public KeyboardInteractiveAuthenticator(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public InteractiveChallenge generateChallenge(ServerSession session, String username, String lang, String subMethods) throws Exception {
        if (authProvider.doesUserExist(username)) {
            return loginChallenge();
        } else if (authProvider.isCreationPossible()) {
            return createAccountChallenge();
        }
        return null;
    }

    @Override
    public boolean authenticate(ServerSession session, String username, List<String> responses) throws Exception {
        if (responses.size() == 1) {
            return authProvider.authenticate(username, responses.get(0)).orElse(false);
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
        challenge.setInteractionName("Welcome to ssh-game");
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
}
