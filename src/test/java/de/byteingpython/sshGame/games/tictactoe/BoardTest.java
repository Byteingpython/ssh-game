package de.byteingpython.sshGame.games.tictactoe;

import de.byteingpython.sshGame.lobby.Lobby;
import de.byteingpython.sshGame.player.Player;

import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @org.junit.jupiter.api.Test
    void checkWin() {
        Player player1 = new Player() {
            @Override
            public String getName() {
                return "TestPlayer1";
            }

            @Override
            public Lobby getLobby() {
                return null;
            }

            @Override
            public void setLobby(Lobby lobby) {

            }

            @Override
            public Runnable getEndCallback() {
                return null;
            }

            @Override
            public OutputStream getOutputStream() {
                return null;
            }

            @Override
            public OutputStream getErrorStream() {
                return null;
            }

            @Override
            public InputStream getInputStream() {
                return null;
            }
        };
        Player player2 = new Player() {
            @Override
            public String getName() {
                return "TestPlayer1";
            }

            @Override
            public Lobby getLobby() {
                return null;
            }

            @Override
            public void setLobby(Lobby lobby) {

            }

            @Override
            public Runnable getEndCallback() {
                return null;
            }

            @Override
            public OutputStream getOutputStream() {
                return null;
            }

            @Override
            public OutputStream getErrorStream() {
                return null;
            }

            @Override
            public InputStream getInputStream() {
                return null;
            }
        };

        Board board = new Board(player1, player2);
        board.setField(player1, 0);
        board.setField(player2, 1);
        board.setField(player1, 3);
        board.setField(player2, 4);
        board.setField(player1, 6);
        assertTrue(board.checkWin(6));
        board=new Board(player1, player2);
        board.setField(player1, 0);
        board.setField(player2, 1);
        board.setField(player1, 4);
        board.setField(player2, 2);
        board.setField(player1, 8);
        assertTrue(board.checkWin(8));
    }
}