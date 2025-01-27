package de.byteingpython.sshGame.screen;


import de.byteingpython.sshGame.event.EventListener;
import de.byteingpython.sshGame.event.InputListener;
import de.byteingpython.sshGame.friends.FriendManager;
import de.byteingpython.sshGame.friends.FriendUpdateEvent;
import de.byteingpython.sshGame.games.Game;
import de.byteingpython.sshGame.games.GameManager;
import de.byteingpython.sshGame.lobby.Lobby;
import de.byteingpython.sshGame.lobby.LobbyManager;
import de.byteingpython.sshGame.matchmaking.Matchmaker;
import de.byteingpython.sshGame.player.LocalPlayer;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.player.PlayerManager;
import de.byteingpython.sshGame.ssh.auth.CredentialAuthProvider;
import de.byteingpython.sshGame.ssh.shell.WindowChangeEvent;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;
import de.byteingpython.sshGame.utils.Message;
import de.byteingpython.sshGame.utils.MessageQueue;
import de.byteingpython.sshGame.utils.StringUtils;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class LobbyScreen implements Command, InputListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LobbyManager lobbyManager;
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    private final FriendManager friendManager;
    private final Matchmaker matchmaker;
    private final CredentialAuthProvider credentialAuthProvider;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private Optional<TextInputScreen> inviteTextInput = Optional.empty();
    private boolean active = true;
    private Player player;    private final MessageQueue messageQueue = new MessageQueue(this::render);
    private final Thread shutdownHook = new Thread(() -> {
        unregister();
        exit();
    });


    public LobbyScreen(LobbyManager lobbyManager, GameManager gameManager, Matchmaker matchmaker, PlayerManager playerManager, FriendManager friendManager, CredentialAuthProvider credentialAuthProvider) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
        this.matchmaker = matchmaker;
        this.playerManager = playerManager;
        this.friendManager = friendManager;
        this.credentialAuthProvider = credentialAuthProvider;
    }

    /**
     * Reregister the InputListener
     */
    private void reregisterListener() {
        active = true;
        player.getInputEventHandler().registerListener(this);
        player.getEventHandler().registerListeners(this);
        render();
    }

    /**
     * Assembles the Lobby screen from different string segments, clears the screen of the player and the sends the newly assembled screen
     */
    private void render() {
        if (player.getLobby().isPlaying() || !active) return;
        try {
            player.getOutputStream().write(EscapeCodeUtils.CLEAR_SCREEN.getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().write(EscapeCodeUtils.HIDE_CURSOR.getBytes(StandardCharsets.UTF_8));


            String sb = "╔════════════════════════════════════════════╗\n\r" +
                    "║ " + StringUtils.alignToSides(player.getLocale().getString("settings") + " ^s", player.getLocale().getString("help") + " ^h", "^f " + player.getLocale().getString("friends"), 42) + " ║\n\r" +
                    "║" +
                    StringUtils.centerText(player.getLobby().getPlayers().size() + "/" + player.getLobby().getGame().getMaxLobbySize(), 44) +
                    "║\n\r" +
                    "║";

            if (player.getLobby().getPlayers().size() > 1) {
                sb += StringUtils.centerText("Leave ^l", 44);
            } else {
                sb += "                                            ";
            }
            sb += "║\n\r";

            sb += renderPlayerCarousel() + "║";

            sb += StringUtils.centerText(messageQueue.getCurrentText(), 44) +
                    "║\n\r";


            //This monster is here to adjust the size of the Box that shows the game to the size of the name of the game
            String queueText = player.getLocale().getString("queue");
            String gameName = player.getLobby().getGame().getName();
            sb += "║ " + StringUtils.alignToSides("┏" + "╺".repeat(queueText.length()) + "┓  ",
                    "  ┏" + "╺".repeat(gameName.length()) + "┓",
                    42) + " ║\n\r";
            sb += "║ " + StringUtils.alignToSides("╏" + queueText + "╏^q",
                    "^m╏" + gameName + "╏",
                    42) + " ║\n\r";
            sb += "║ " + StringUtils.alignToSides("┗" + "╺".repeat(queueText.length()) + "┛  ",
                    "  ┗" + "╺".repeat(gameName.length()) + "┛",
                    42) + " ║\n\r";
            sb += "╚════════════════════════════════════════════╝";

            player.getOutputStream().write(StringUtils.centerInTerminal(sb, player.getWindowSize()).getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        logger.info("Setting output stream");
        this.out = out;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        this.player = new LocalPlayer(channel.getSession().getUsername(), out, err, in, this::render, env);
        try {
            playerManager.registerPlayer(player);
        } catch (IllegalArgumentException e) {
            player.getOutputStream().write((player.getLocale().getString("registration_error") + e.getMessage() + "\n\r").getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
            callback.onExit(0);
            return;
        }
        messageQueue.setPlaceholder(player.getLocale().getString("welcome_message"));
        Lobby lobby = lobbyManager.createLobby();
        lobby.addPlayer(player);

        List<Game> games = gameManager.getGames();
        if (!games.isEmpty()) {
            Game game = games.get(0);
            lobby.setGame(game);
        }
        try {
            player.getOutputStream().write(EscapeCodeUtils.SWITCH_TO_ALTERNATE_SCREEN.getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().write(EscapeCodeUtils.HIDE_CURSOR.getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
        } catch (IOException e) {
            logger.error(e.toString());
            channel.close();
            return;
        }
        logger.trace("Starting lobby");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        player.getInputEventHandler().registerListener(this);
        player.getEventHandler().registerListeners(this);
        render();
    }

    /**
     * Shows a message for a certain duration.
     * There can only be one message at a time. If a new message is shown, the old one is overwritten.
     *
     * @param s        the message to be shown
     * @param duration the duration in milliseconds
     */
    private void showMessage(String s, long duration) {
        messageQueue.addMessage(new Message(s, duration));
        render();
    }

    private void unregisterListeners(LobbyScreen listener) {
        active = false;
        player.getInputEventHandler().unregisterListener(this);
        player.getEventHandler().unregisterListeners(this);
        try {
            player.getEventHandler().registerListener(this, this.getClass().getMethod("onMessage", LobbyScreenMessageEvent.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is responsible for assembling the player carousel in the middle of the lobby screen and was separated from render() because of its length
     *
     * @return The assembled player carousel
     */
    private String renderPlayerCarousel() {
        StringBuilder firstLine = new StringBuilder();
        StringBuilder secondLine = new StringBuilder();
        StringBuilder thirdLine = new StringBuilder();
        StringBuilder fourthLine = new StringBuilder();

        if (player.getLobby().getPlayers().size() < player.getLobby().getGame().getMaxLobbySize()) {
            firstLine.append("  ╭───╮  ");
            secondLine.append("^j│ + │  ");
            thirdLine.append("  ╰───╯  ");
            fourthLine.append(StringUtils.centerText(player.getLocale().getString("join"), 9));
        }
        for (Player iterPlayer : player.getLobby().getPlayers()) {
            firstLine.append("  ╭───╮  ");
            secondLine.append("  │ ");
            secondLine.append(iterPlayer.getName().charAt(0));
            secondLine.append(" │  ");
            thirdLine.append("  ╰───╯  ");
            fourthLine.append(StringUtils.centerText(iterPlayer.getName(), 10));
        }


        return "║" +
                StringUtils.centerText(firstLine.toString(), 44) +
                "║" +
                "\n\r" +
                "║" +
                StringUtils.centerText(secondLine.toString(), 44) +
                "║" +
                "\n\r" +
                "║" +
                StringUtils.centerText(thirdLine.toString(), 44) +
                "║" +
                "\n\r" +
                "║" +
                StringUtils.centerText(fourthLine.toString(), 44) +
                "║" +
                "\n\r";
    }

    @EventListener
    public void onFriendUpdate(FriendUpdateEvent event) {
        if (player.getLobby().isPlaying()) return;
        String messageText = event.getFriendName() + " " + player.getLocale().getString(switch (event.getType()) {
            case ADDED -> "friend_added";
            case REMOVED -> "friend_removed";
            case REQUESTED -> "friend_requested";
        });
        messageQueue.addMessage(new Message(messageText, 2000));
    }

    /**
     * Show a list with all the players friends, options for each friend and the option to add a new friend
     */
    public void showFriendMenu() {
        unregisterListeners(this);
        FriendMenuScreen friendMenuScreen = new FriendMenuScreen(player, friendManager, playerManager);
        friendMenuScreen.show(this::reregisterListener);
    }

    @Override
    public void destroy(ChannelSession channel) {
        callback.onExit(0, "");
    }

    @Override
    public void onInput(int input) {
        if (!player.getLobby().isPlaying()) {
            try {
                logger.info("Received input: {}", input);

                switch (input) {
                    //Show a List with friends and offer Options
                    case 6:
                        showFriendMenu();
                        return;
                    case 8:
                        unregisterListeners(this);
                        TextDisplayScreen.fromTranslation("help_text", player.getLocale()).show(player, this::reregisterListener);
                        return;

                    //Start matchmaking
                    case 17:
                        try {
                            if (!matchmaker.isMatchmaking(player.getLobby())) {
                                matchmaker.matchmake(player.getLobby());
                                if (!player.getLobby().isPlaying()) {
                                    showMessage(player.getLocale().getString("matchmaking_start"), 2999);
                                }
                            } else {
                                matchmaker.cancelMatchmaking(player.getLobby());
                                showMessage(player.getLocale().getString("matchmaking_cancel"), 2999);
                            }
                        } catch (IllegalArgumentException e) {
                            showMessage(e.getMessage(), 2999);
                            throw e;
                        }
                        break;
                    //Select a game
                    case 13:
                        SelectScreen<Game> selectScreen = new SelectScreen<>(player);
                        gameManager.getGames().forEach(game -> selectScreen.addOption(game.getName(), game));
                        unregisterListeners(this);
                        selectScreen.selectOption(player.getLocale().getString("select_gamemode"), () -> {
                            reregisterListener();
                            selectScreen.getSelected().ifPresent(game -> {
                                try {
                                    player.getLobby().setGame(game);
                                } catch (IllegalStateException e) {
                                    showMessage(e.getMessage(), 3000);
                                }
                            });
                        });
                        return;
                    //Leave the current lobby
                    case 12:
                        if (player.getLobby().getPlayers().size() <= 1) {
                            return;
                        }
                        Lobby newLobby = lobbyManager.createLobby();
                        Lobby oldLobby = player.getLobby();
                        player.getLobby().removePlayer(player);
                        newLobby.addPlayer(player);
                        List<Game> games = gameManager.getGames();
                        if (!games.isEmpty()) {
                            Game game = games.get(0);
                            newLobby.setGame(game);
                        }
                        //Notify the other players that a rerender is necessary
                        for (Player player : oldLobby.getPlayers()) {
                            player.getEventHandler().handle(new ScreenUpdateEvent());
                        }
                        break;
                    //Join the lobby of another player
                    case 10:
                        unregisterListeners(this);
                        inviteTextInput = Optional.of(new TextInputScreen(new Runnable() {
                            @Override
                            public void run() {
                                reregisterListener();
                                LoggerFactory.getLogger(this.getClass()).info(inviteTextInput.get().getInput());
                                Optional<Player> invitedPlayer = playerManager.getPlayer(inviteTextInput.get().getInput());
                                inviteTextInput = Optional.empty();
                                if (invitedPlayer.isEmpty()) {
                                    showMessage(player.getLocale().getString("player_not_exist"), 3000);
                                    return;
                                }
                                if (invitedPlayer.get() == player) {
                                    showMessage(player.getLocale().getString("fraud_attempt"), 3000);
                                }
                                joinLobby(invitedPlayer);
                            }
                        }, player, player.getLocale().getString("invite_title")));
                        return;
                        // Open Settings screen
                    case 19:
                        unregisterListeners(this);
                        new SettingsScreen(player, credentialAuthProvider, playerManager.getLocaleManager()).show(this::reregisterListener);
                        break;
                    case 3:
                        Runtime.getRuntime().removeShutdownHook(shutdownHook);
                        unregister();
                        exit();
                        return;

                    //Leave the game
                    case -1:
                        Runtime.getRuntime().removeShutdownHook(shutdownHook);
                        unregister();
                        return;
                }

            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        if (!player.getLobby().isPlaying()) {
            render();
        }
    }

    private void joinLobby(Optional<Player> invitedPlayer) {
        Lobby originalLobby = player.getLobby();
        player.getLobby().removePlayer(player);
        try {
            invitedPlayer.get().getLobby().addPlayer(player);
            if (originalLobby.getPlayers().isEmpty()) {
                lobbyManager.removeLobby(originalLobby);
            }
        } catch (IllegalStateException e) {
            showMessage(e.getMessage(), 3000);
            originalLobby.addPlayer(player);
        }
        // Update all  the players screens so the new player is displayed
        for (Player lobbyPlayer : player.getLobby().getPlayers()) {
            lobbyPlayer.getEventHandler().handle(new ScreenUpdateEvent());
        }
    }

    @EventListener
    public void onUpdate(ScreenUpdateEvent event) {
        if (player.getLobby().isPlaying() || !active) return;
        render();
    }

    @EventListener
    public void onMessage(LobbyScreenMessageEvent event) {
        this.messageQueue.addMessage(event.getMessage());
    }

    private void exit() {
        try {
            out.write(EscapeCodeUtils.SWITCH_TO_MAIN_SCREEN.getBytes(StandardCharsets.UTF_8));
            out.write(EscapeCodeUtils.SHOW_CURSOR.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.write(("\n\r" + player.getLocale().getString("exit_message") + "\n\r").getBytes());
            out.flush();
            callback.onExit(-1, player.getLocale().getString("exit_message"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void unregister() {
        playerManager.unregisterPlayer(player);
        if (player.getLobby().getPlayers().size() <= 1) {
            lobbyManager.removeLobby(player.getLobby());
        }
        player.getLobby().removePlayer(player);
        unregisterListeners(this);
    }

    @EventListener
    public void onWindowChange(WindowChangeEvent event) {
        render();
    }



}