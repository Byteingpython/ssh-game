package de.byteingpython.sshGame;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.config.EnvConfigurationProvider;
import de.byteingpython.sshGame.database.surreal.SurrealCredentialProvider;
import de.byteingpython.sshGame.database.surreal.SurrealFriendManager;
import de.byteingpython.sshGame.friends.FriendManager;
import de.byteingpython.sshGame.ssh.SshGameServerBuilder;
import org.apache.sshd.server.SshServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ConfigurationException;
import java.io.IOException;

public class Main {

    private final Logger logger = LoggerFactory.getLogger(Main.class);
    private final SshServer sshServer;
    private final ConfigurationProvider config;


    public Main() throws ConfigurationException, IOException {
        config = new EnvConfigurationProvider();
        sshServer = new SshGameServerBuilder(config).setAuthProvider(new SurrealCredentialProvider(config)).build();
        sshServer.start();
    }

    public static void main(String[] args) throws ConfigurationException, IOException {
        new Main();
    }
}
