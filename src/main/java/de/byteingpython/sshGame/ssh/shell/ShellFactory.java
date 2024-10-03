package de.byteingpython.sshGame.ssh.shell;

import com.surrealdb.driver.SyncSurrealDriver;
import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.database.surreal.ConfigSurrealDriver;
import de.byteingpython.sshGame.database.surreal.SurrealFriendManager;
import de.byteingpython.sshGame.database.surreal.SurrealStatisticsManager;
import de.byteingpython.sshGame.friends.FriendManager;
import de.byteingpython.sshGame.games.LocalGameManager;
import de.byteingpython.sshGame.games.StatisticsManager;
import de.byteingpython.sshGame.games.tictactoe.TicTacToe;
import de.byteingpython.sshGame.lobby.LobbyManager;
import de.byteingpython.sshGame.lobby.LocalLobbyManager;
import de.byteingpython.sshGame.matchmaking.LocalMatchmaker;
import de.byteingpython.sshGame.matchmaking.Matchmaker;
import de.byteingpython.sshGame.player.LocalPlayerManager;
import de.byteingpython.sshGame.player.PlayerManager;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import javax.naming.ConfigurationException;

public class ShellFactory implements org.apache.sshd.server.shell.ShellFactory {

    private final ConfigurationProvider configurationProvider;
    private final LobbyManager localLobbyManager = new LocalLobbyManager();
    private final LocalGameManager localGameManager;
    private final Matchmaker localMatchmaker = new LocalMatchmaker();
    private final PlayerManager localPlayerManager = new LocalPlayerManager();
    private final FriendManager surrealFriendManager;


    public ShellFactory(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
        try {
            SyncSurrealDriver driver = new ConfigSurrealDriver(configurationProvider);
            surrealFriendManager = new SurrealFriendManager(driver, configurationProvider, localPlayerManager);
            localGameManager = new LocalGameManager();
            StatisticsManager statisticsManager = new SurrealStatisticsManager(driver, localGameManager);
            localGameManager.add(new TicTacToe(statisticsManager));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Command createShell(ChannelSession channel) {
        return new ShellCommand(configurationProvider, localLobbyManager, localPlayerManager, localGameManager, localMatchmaker, surrealFriendManager);
    }
}
