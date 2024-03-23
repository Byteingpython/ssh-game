package de.byteingpython.sshGame.ssh;

import de.byteingpython.sshGame.Main;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;

public class ShellFactory implements org.apache.sshd.server.shell.ShellFactory {

    private final Main main;

    public ShellFactory(Main main) {
        this.main = main;
    }

    @Override
    public Command createShell(ChannelSession channel) throws IOException {
        return new ShellCommand(main);
    }
}
