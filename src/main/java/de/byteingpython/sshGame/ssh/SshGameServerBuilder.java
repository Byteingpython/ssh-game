package de.byteingpython.sshGame.ssh;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

public class SshGameServerBuilder {

    private AuthProvider authProvider;
    private final ConfigurationProvider configurationProvider;

    public SshGameServerBuilder(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public SshGameServerBuilder setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
        return this;
    }

    public SshServer build() {
        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(configurationProvider.getInt("SSH_PORT").orElse(22));
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshServer.setShellFactory(new ShellFactory(configurationProvider));
        //TODO implement public key authentication
        if (authProvider != null) {
            sshServer.setPasswordAuthenticator(new PasswordAuthenticator(authProvider));

        }
        return sshServer;
    }
}
