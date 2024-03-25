package de.byteingpython.sshGame.ssh;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.ssh.auth.AuthProvider;
import de.byteingpython.sshGame.ssh.auth.KeyboardInteractiveAuthenticator;
import de.byteingpython.sshGame.ssh.auth.PasswordAuthenticator;
import de.byteingpython.sshGame.ssh.keys.ConfigKeyPairProvider;
import de.byteingpython.sshGame.ssh.shell.ShellFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

public class SshGameServerBuilder {
    private final SshServer sshServer;

    public SshGameServerBuilder(ConfigurationProvider configurationProvider) {
        this.sshServer = SshServer.setUpDefaultServer();
        this.sshServer.setPort(configurationProvider.getInt("SSH_PORT").orElse(22));
        this.sshServer.setShellFactory(new ShellFactory(configurationProvider));
        this.sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        if (configurationProvider.getString("SSH_PUBLIC_KEY").isPresent() && configurationProvider.getString("SSH_PRIVATE_KEY").isPresent()) {
            this.sshServer.setKeyPairProvider(new ConfigKeyPairProvider(configurationProvider));
        }
    }

    //TODO implement public key authentication
    public SshGameServerBuilder setAuthProvider(AuthProvider authProvider) {
        this.sshServer.setKeyboardInteractiveAuthenticator(new KeyboardInteractiveAuthenticator(authProvider));
        this.sshServer.setPasswordAuthenticator(new PasswordAuthenticator(authProvider));
        return this;
    }

    public SshServer build() {
        return sshServer;
    }
}
