package de.byteingpython.sshGame;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.config.EnvConfigurationProvider;
import de.byteingpython.sshGame.database.surreal.SurrealCredentialProvider;
import de.byteingpython.sshGame.ssh.SshGameServerBuilder;
import org.apache.sshd.server.SshServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ConfigurationException;
import java.io.IOException;

public class Main {

    private final Logger logger = LoggerFactory.getLogger(Main.class);
    private final ConfigurationProvider config;
    private SshServer sshServer;


    public Main() throws ConfigurationException, IOException {
        config = new EnvConfigurationProvider();
        try {
            SurrealCredentialProvider provider = new SurrealCredentialProvider(config);
            sshServer = new SshGameServerBuilder(config, provider).build();
            sshServer.start();
        } catch (Exception e) {
            LoggerFactory.getLogger(Main.class).error("Failed to connect to database", e);
        }
    }

    public static void main(String[] args) throws ConfigurationException, IOException {
        new Main();
    }
}
