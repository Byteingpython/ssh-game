package de.byteingpython.sshGame.ssh.shell;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;

public class ShellFactory implements org.apache.sshd.server.shell.ShellFactory {

    private final ConfigurationProvider configurationProvider;

    public ShellFactory(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public Command createShell(ChannelSession channel) throws IOException {
        return new ShellCommand(configurationProvider);
    }
}
