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
        if(game!=null){
            if(players.size()>=game.getMaxLobbySize()){
                throw new IllegalStateException("Lobby is full!");
            }
            if(players.contains(player)){
                throw new IllegalArgumentException("This player is already in the lobby!");
            }
        }
        player.setLobby(this);
        players.add(player);
    }

    @Override
    public void removePlayer(Player player) {
        if(!players.contains(player)){
            throw new IllegalArgumentException("This player is not in the lobby!");
        }
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
        if(game.getMaxLobbySize()<players.size()){
            throw new IllegalStateException("Lobby is too big for this Game");
        }
        if(players.size()< game.getMinLobbySize()){
            throw new IllegalStateException("Lobby is too small for this Game");
        }
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
