package de.byteingpython.sshGame.ssh.shell;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.games.*;
import de.byteingpython.sshGame.games.matchmaking.LocalMatchmaker;
import de.byteingpython.sshGame.games.matchmaking.Matchmaker;
import de.byteingpython.sshGame.games.tictactoe.TicTacToe;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;

public class ShellFactory implements org.apache.sshd.server.shell.ShellFactory {

    private final ConfigurationProvider configurationProvider;
    private final LobbyManager localLobbyManager = new LocalLobbyManager();
    private final GameManager localGameManager = new LocalGameMananger(new TicTacToe());
    private final Matchmaker localMatchmaker = new LocalMatchmaker();
    private final PlayerManager localPlayerManager = new LocalPlayerManager();


    public ShellFactory(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public Command createShell(ChannelSession channel) {
        return new ShellCommand(configurationProvider, localLobbyManager, localPlayerManager, localGameManager, localMatchmaker);
    }
}
