package de.byteingpython.sshGame.lobby;

import de.byteingpython.sshGame.games.Game;
import de.byteingpython.sshGame.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalLobby implements Lobby {
    private final List<Player> players = new ArrayList<>();
    private final UUID id = UUID.randomUUID();
    private Game game;
    private boolean playing = false;

    @Override
    public void addPlayer(Player player) {
        player.setLobby(this);
        players.add(player);
    }

    @Override
    public void removePlayer(Player player) {
        players.remove(player);
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public Runnable getEndCallback() {
       return new Runnable() {
           @Override
           public void run() {
                playing = false;
                for (Player player : players) {
                    player.getEndCallback().run();
                }
           }
       };
    }
}
