package de.byteingpython.sshGame.ssh;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.ssh.auth.AuthProvider;
import de.byteingpython.sshGame.ssh.auth.KeyboardInteractiveAuthenticator;
import de.byteingpython.sshGame.ssh.auth.PasswordAuthenticator;
import de.byteingpython.sshGame.ssh.keys.ConfigKeyPairProvider;
import de.byteingpython.sshGame.ssh.shell.ShellFactory;
import de.byteingpython.sshGame.utils.throttling.ConfigThrottler;
import de.byteingpython.sshGame.utils.throttling.Throttler;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

public class SshGameServerBuilder {
    private final SshServer sshServer;
    private final ConfigurationProvider configurationProvider;

    public SshGameServerBuilder(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
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
        Throttler throttler = new ConfigThrottler(configurationProvider, 5, 60 * 1000 * 30, "loginTries");
        this.sshServer.setKeyboardInteractiveAuthenticator(new KeyboardInteractiveAuthenticator(authProvider, throttler));
        this.sshServer.setPasswordAuthenticator(new PasswordAuthenticator(authProvider, throttler));
        return this;
    }

    public SshServer build() {
        return sshServer;
    }
}
