package de.byteingpython.sshGame.games.tictactoe;

import de.byteingpython.sshGame.event.InputListener;
import de.byteingpython.sshGame.games.Game;
import de.byteingpython.sshGame.lobby.Lobby;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.utils.EscapeCodeUtils;
import de.byteingpython.sshGame.utils.RandomBoolean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TicTacToe implements Game, InputListener {


    private List<Lobby> lobbies = new ArrayList<>();

    private Board board;

    private final List<Player> players = new ArrayList<>();

    @Override
    public String getName() {
        return "Tic Tac Toe";
    }

    @Override
    public String getDescription() {
        return "A simple game of Tic Tac Toe against players in your lobby.";
    }

    @Override
    public String getId() {
        return "tic_tac_toe";
    }

    @Override
    public int getMaxLobbySize() {
        return 2;
    }

    private static void endGame(List<Lobby> lobbies) {
        for (Lobby lobby : lobbies) {
            lobby.setPlaying(false);
        }
        for (Lobby lobby : lobbies) {
            new Thread(() -> lobby.getEndCallback().run()).start();
        }
    }

    @Override
    public int getMinLobbyCount() {
        return 1;
    }

    @Override
    public int getMinLobbySize() {
        return 1;
    }

    @Override
    public int getMinTotalPlayers() {
        return 2;
    }

    @Override
    public int getMaxTotalPlayers() {
        return 2;
    }

    @Override
    public int getMaxLobbyCount() {
        return 2;
    }

    @Override
    public void startGame(List<Lobby> lobbies) {
        this.lobbies = lobbies;
        for (Lobby lobby : lobbies) {
            players.addAll(lobby.getPlayers());
            lobby.setPlaying(true);
        }
            if (RandomBoolean.getRandomBoolean()) {
                board = new Board(players.get(0), players.get(1));
            } else {
                board = new Board(players.get(1), players.get(0));
            }
            board.getCurrentPlayer().getEventHandler().registerListener(this);
            renderAll();
    }


    private void renderAll() {
        for (Player player : players) {
            try {
                player.getOutputStream().write(EscapeCodeUtils.CLEAR_SCREEN.getBytes());
                player.getOutputStream().write(board.render(player).getBytes());
                player.getOutputStream().flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onInput(int input) {
        if (input == 3) {
            endGame(lobbies);
            return;
        }
        if (input < 49 || input > 57) {
            return;
        }
        if (board.isSet(input - 49)) {
            return;
        }
        board.setField(board.getCurrentPlayer(), input - 49);
        if (board.checkWin(input - 49)) {
            endGame(lobbies);
            return;
        }
        board.getCurrentPlayer().getEventHandler().registerListener(this);
        board.getOtherPlayer().getEventHandler().unregisterListener(this);
        renderAll();
    }
}
