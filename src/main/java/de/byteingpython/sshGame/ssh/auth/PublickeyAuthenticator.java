package de.byteingpython.sshGame.ssh.auth;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.session.ServerSession;

import java.security.PublicKey;

public class PublickeyAuthenticator implements org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator {
    private final AuthProvider authProvider;

    public PublickeyAuthenticator(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public boolean authenticate(String s, PublicKey publicKey, ServerSession serverSession) throws AsyncAuthException {
        return authProvider.authenticate(s, publicKey).orElse(false);
    }
}
