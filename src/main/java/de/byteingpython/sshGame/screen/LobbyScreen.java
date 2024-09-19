package de.byteingpython.sshGame.screen;


import de.byteingpython.sshGame.event.EventListener;
import de.byteingpython.sshGame.event.InputListener;
import de.byteingpython.sshGame.friends.FriendManager;
import de.byteingpython.sshGame.games.Game;
import de.byteingpython.sshGame.games.GameManager;
import de.byteingpython.sshGame.lobby.Lobby;
import de.byteingpython.sshGame.lobby.LobbyManager;
import de.byteingpython.sshGame.matchmaking.Matchmaker;
import de.byteingpython.sshGame.player.LocalPlayer;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.player.PlayerManager;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;
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
import java.util.Timer;
import java.util.TimerTask;

public class LobbyScreen implements Command, InputListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LobbyManager lobbyManager;
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    private final FriendManager friendManager;
    private final Matchmaker matchmaker;
    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private Optional<TextInputScreen> inviteTextInput = Optional.empty();
    private Optional<TextInputScreen> addFriendTextInput = Optional.empty();
    private String message = "Welcome to the game";

    private Player player;

    public LobbyScreen(LobbyManager lobbyManager, GameManager gameManager, Matchmaker matchmaker, PlayerManager playerManager, FriendManager friendManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
        this.matchmaker = matchmaker;
        this.playerManager = playerManager;
        this.friendManager = friendManager;
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
        this.player = new LocalPlayer(channel.getSession().getUsername(), out, err, in, this::render);
        try {
            playerManager.registerPlayer(player);
        } catch (IllegalArgumentException e) {
            player.getOutputStream().write(("Unable to register Player. " + e.getMessage() + "\n\r").getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
            callback.onExit(0);
            return;
        }
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
        message = s;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (message.equals(s)) {
                    message = "";
                }
            }
        }, duration);
        render();
    }

    /**
     * Reregister the InputListener
     */
    private void reregisterListener() {
        player.getInputEventHandler().registerListener(this);
        player.getEventHandler().unregisterListeners(this);
        render();
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
            fourthLine.append("  Join   ");
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

    /**
     * Assembles the Lobby screen from different string segments, clears the screen of the player and the sends the newly assembled screen
     */
    private void render() {
        try {
            player.getOutputStream().write(EscapeCodeUtils.CLEAR_SCREEN.getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();


            String sb = "╔════════════════════════════════════════════╗\n\r" +
                    "║ Settings ^s                     ^f Friends ║\n\r" +
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

            sb += StringUtils.centerText(message, 44) +
                    "║\n\r" +
                    "║ ┏╺╺╺╺╺┓";
            //This monster is here to adjust the size of the Box that shows the game to the size of the name of the game
            sb += " ".repeat(33 - player.getLobby().getGame().getName().length()) + "┏" + "╺".repeat(player.getLobby().getGame().getName().length()) + "┓";
            sb += " ║\n\r║ ╏queue╏^q";
            sb += " ".repeat(29 - player.getLobby().getGame().getName().length());
            sb += "^m╏" + player.getLobby().getGame().getName() + "╏ ║\n\r║ ┗╺╺╺╺╺┛";
            sb += " ".repeat(33 - player.getLobby().getGame().getName().length()) + "┗" + "╺".repeat(player.getLobby().getGame().getName().length()) + "┛";
            sb += " ║\n\r" + "╚════════════════════════════════════════════╝";

            player.getOutputStream().write(sb.getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().write(EscapeCodeUtils.HIDE_CURSOR.getBytes(StandardCharsets.UTF_8));
            player.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Show a list with all the players friends, options for each friend and the option to add a new friend
     */
    public void showFriendMenu() {
        SelectScreen<String> friendSelectScreen = new SelectScreen<>(player);
        List<String> friends = friendManager.getFriends(player);
        for(String friend: friends){
            String friendOption = friend;
            if(playerManager.getPlayer(friend).isPresent()){
                friendOption += " - Online";
            }
            friendSelectScreen.addOption(friendOption, friend);
        }
        friendSelectScreen.addOption("Add friend", "");
        player.getInputEventHandler().unregisterListener(this);
        LobbyScreen lobbyScreen = this;
        friendSelectScreen.selectOption(new Runnable() {
            @Override
            public void run() {
                LoggerFactory.getLogger(this.getClass()).info("select screen ended");
                reregisterListener();
                if(friendSelectScreen.getSelected().isEmpty()){
                    return;
                }
                if(friendSelectScreen.getSelected().get().isEmpty()){
                    player.getInputEventHandler().unregisterListener(lobbyScreen);
                    try {
                        lobbyScreen.addFriendTextInput=  Optional.of(new TextInputScreen(() -> {
                            friendManager.addFriend(player, addFriendTextInput.get().getInput());
                            reregisterListener();
                        }, player, "Enter the name of your friend"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }

                SelectScreen<String> friendOptionSelectScreen = new SelectScreen<>(player);

                Optional<Player> selectedFriendPlayer = playerManager.getPlayer(friendSelectScreen.getSelected().get());
                if(selectedFriendPlayer.isPresent()){
                    Lobby selectedFriendLobby = selectedFriendPlayer.get().getLobby();
                    if(selectedFriendLobby.getPlayers().size()<player.getLobby().getGame().getMaxLobbySize()&&!selectedFriendLobby.isPlaying()){
                        friendOptionSelectScreen.addOption("Join Lobby", "join");
                    }
                }

                friendOptionSelectScreen.addOption("Remove friend", "remove");

                player.getInputEventHandler().unregisterListener(lobbyScreen);
                friendOptionSelectScreen.selectOption("Options for "+friendSelectScreen.getSelected().get(), () -> {
                    reregisterListener();
                    if(friendOptionSelectScreen.getSelected().isEmpty()){
                        showFriendMenu();
                        return;
                    }
                    if(friendOptionSelectScreen.getSelected().get().equals("remove")){
                        friendManager.removeFriend(player, friendSelectScreen.getSelected().get());
                    }
                    if(friendOptionSelectScreen.getSelected().get().equals("join")){
                        joinLobby(selectedFriendPlayer);
                    }
                });

            }
        });
    }

    @Override
    public void destroy(ChannelSession channel) {
        callback.onExit(0, "Goodbye");
    }

    @Override
    public void onInput(int input) {
        if (!player.getLobby().isPlaying()) {
            try {
                logger.info("Received input: " + input);
                if (input == 19) {
                    out.write("-> Settings".getBytes());
                    out.flush();
                }
                //Show a List with friends and offer Options
                if (input == 6) {
                    showFriendMenu();
                    return;
                }

                //Start matchmaking
                if (input == 17) {
                    try {
                        matchmaker.matchmake(player.getLobby());
                        if (!player.getLobby().isPlaying()) {
                            showMessage("Matchmaking started", 2999);
                        }
                    } catch (IllegalArgumentException e) {
                        showMessage(e.getMessage(), 2999);
                        throw e;
                    }
                }

                //Select a game
                if (input == 13) {
                    SelectScreen<Game> selectScreen = new SelectScreen<>(player);
                    gameManager.getGames().forEach(game -> selectScreen.addOption(game.getName(), game));
                    player.getInputEventHandler().unregisterListener(this);
                    selectScreen.selectOption("Select gamemode", () -> {
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

                }

                //Leave the current lobby
                if (input == 12) {
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
                    for(Player player:oldLobby.getPlayers()){
                        player.getEventHandler().handle(new ScreenUpdateEvent());
                    }
                }

                //Join the lobby of another player
                if (input == 10) {
                    player.getInputEventHandler().unregisterListener(this);
                    inviteTextInput = Optional.of(new TextInputScreen(new Runnable() {
                        @Override
                        public void run() {
                            reregisterListener();
                            LoggerFactory.getLogger(this.getClass()).info(inviteTextInput.get().getInput());
                            Optional<Player> invitedPlayer = playerManager.getPlayer(inviteTextInput.get().getInput());
                            inviteTextInput = Optional.empty();
                            if (invitedPlayer.isEmpty()) {
                                showMessage("This player does not exist", 3000);
                                return;
                            }
                            if (invitedPlayer.get() == player) {
                                showMessage("Nice try! Nope this isn't that easy to fool", 3000);
                            }
                            joinLobby(invitedPlayer);
                        }
                    }, player, "Input Player name"));
                    LoggerFactory.getLogger(this.getClass()).info("Started Text Input");
                    return;
                }

                if(input==3){
                    out.write(EscapeCodeUtils.SWITCH_TO_MAIN_SCREEN.getBytes(StandardCharsets.UTF_8));
                    out.write(EscapeCodeUtils.SHOW_CURSOR.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    out.write("\nGoodbye\n".getBytes());
                    out.flush();
                    callback.onExit(-1, "Goodbye");
                    return;
                }
                //Leave the game
                if (input == -1) {
                    playerManager.unregisterPlayer(player);
                    if (player.getLobby().getPlayers().size() <= 1) {
                        lobbyManager.removeLobby(player.getLobby());
                    }
                    player.getLobby().removePlayer(player);
                    player.getInputEventHandler().unregisterListener(this);
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
            if(originalLobby.getPlayers().isEmpty()){
                lobbyManager.removeLobby(originalLobby);
            }
        } catch (IllegalStateException e) {
            showMessage(e.getMessage(), 3000);
            originalLobby.addPlayer(player);
        }
        render();
        invitedPlayer.get().getEventHandler().handle(new ScreenUpdateEvent());
    }

    @EventListener
    public void onUpdate(ScreenUpdateEvent event) {
        render();
    }
}