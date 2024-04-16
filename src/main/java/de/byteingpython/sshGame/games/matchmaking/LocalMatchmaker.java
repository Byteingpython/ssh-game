package de.byteingpython.sshGame.games.matchmaking;

import de.byteingpython.sshGame.games.Game;
import de.byteingpython.sshGame.games.Lobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalMatchmaker implements Matchmaker {

    Map<String, List<Lobby>> lobbies = new HashMap<>();

    @Override
    public void matchmake(Lobby lobby) throws IllegalArgumentException {
        Game game = lobby.getGame();
        if (game == null) {
            throw new IllegalArgumentException("Lobby has no game");
        }
        if (lobby.getPlayers().size() > game.getMaxLobbySize()) {
            throw new IllegalArgumentException("Lobby is too big for this Game");
        }
        if (lobby.getPlayers().size() < game.getMinLobbySize()) {
            addLobbyForGame(game, lobby);
            return;
        }
        if (getLobbiesForGame(game).size() + 1 < game.getMinLobbyCount()) {
            addLobbyForGame(game, lobby);
            return;
        }
        List<Lobby> selectedLobbies = new ArrayList<>();
        selectedLobbies.add(lobby);
        int totalPlayers = lobby.getPlayers().size();
        for (Lobby l : List.copyOf(getLobbiesForGame(game))) {
            if (totalPlayers + l.getPlayers().size() > game.getMaxTotalPlayers()) {
                continue;
            }
            selectedLobbies.add(l);
            totalPlayers += l.getPlayers().size();
            if (totalPlayers > game.getMinTotalPlayers()) {
                break;
            }
            getLobbiesForGame(game).remove(l);
        }
        if (totalPlayers < game.getMinTotalPlayers()) {
            addLobbysForGame(game, selectedLobbies);
            return;
        }
        lobby.getGame().startGame(selectedLobbies);
    }

    private List<Lobby> getLobbiesForGame(Game game) {
        return lobbies.computeIfAbsent(game.getId(), k -> new ArrayList<>());
    }

    private void addLobbysForGame(Game game, List<Lobby> lobbies) {
        for (Lobby l : lobbies) {
            addLobbyForGame(game, l);
        }
    }

    private void removeLobbyForGame(Game game, Lobby lobby) {
        List<Lobby> lobbiesForGame = lobbies.get(game.getId());
        lobbiesForGame.remove(lobby);
        lobbies.put(game.getId(), lobbiesForGame);
    }

    private void addLobbyForGame(Game game, Lobby lobby) {
        List<Lobby> lobbiesForGame = lobbies.get(game.getId());
        lobbiesForGame.add(lobby);
        lobbies.put(game.getId(), lobbiesForGame);
    }

    @Override
    public void cancelMatchmaking(Lobby lobby) {
        removeLobbyForGame(lobby.getGame(), lobby);
    }
}
