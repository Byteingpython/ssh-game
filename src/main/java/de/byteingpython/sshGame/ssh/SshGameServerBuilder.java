package de.byteingpython.sshGame.ssh;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.ssh.auth.*;
import de.byteingpython.sshGame.ssh.keys.ConfigKeyPairProvider;
import de.byteingpython.sshGame.ssh.shell.ShellFactory;
import de.byteingpython.sshGame.utils.throttling.ConfigThrottler;
import de.byteingpython.sshGame.utils.throttling.Throttler;
import org.apache.sshd.server.SshServer;

public class SshGameServerBuilder {
    private final SshServer sshServer;
    private final ConfigurationProvider configurationProvider;

    public SshGameServerBuilder(ConfigurationProvider configurationProvider, CredentialAuthProvider authProvider) {
        this.configurationProvider = configurationProvider;
        this.sshServer = SshServer.setUpDefaultServer();
        this.sshServer.setPort(configurationProvider.getInt("SSH_PORT").orElse(22));
        this.sshServer.setShellFactory(new ShellFactory(configurationProvider, authProvider));
        //this.sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        this.sshServer.setKeyPairProvider(new ConfigKeyPairProvider(configurationProvider));
        if (configurationProvider.getString("SSH_PUBLIC_KEY").isPresent() && configurationProvider.getString("SSH_PRIVATE_KEY").isPresent()) {
            this.sshServer.setKeyPairProvider(new ConfigKeyPairProvider(configurationProvider));
        }
        setAuthProvider(authProvider);
    }

    public SshGameServerBuilder setAuthProvider(AuthProvider authProvider) {
        Throttler throttler = new ConfigThrottler(configurationProvider, 5, 60 * 1000 * 30, "loginTries");
        this.sshServer.setKeyboardInteractiveAuthenticator(new KeyboardInteractiveAuthenticator(authProvider, throttler));
        this.sshServer.setPasswordAuthenticator(new PasswordAuthenticator(authProvider, throttler));
        this.sshServer.setPublickeyAuthenticator(new PublickeyAuthenticator(authProvider));
        return this;
    }

    public SshServer build() {
        return sshServer;
    }
}
