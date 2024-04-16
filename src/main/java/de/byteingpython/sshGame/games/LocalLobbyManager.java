package de.byteingpython.sshGame.games;

import java.util.*;

public class LocalLobbyManager implements LobbyManager {

    private Map<UUID, Lobby> lobbies;

    public LocalLobbyManager() {
        this.lobbies = new HashMap<>();
    }
    @Override
    public Lobby createLobby() {
        Lobby newLobby = new LocalLobby();
        lobbies.put(newLobby.getId(), newLobby);
        return newLobby;
    }

    @Override
    public void removeLobby(Lobby lobby) {
        lobbies.remove(lobby.getId());
    }

    @Override
    public Lobby getLobby(UUID id) {
        return lobbies.get(id);
    }

    @Override
    public List<Lobby> getLobbies() {
        return new ArrayList<>(lobbies.values());
    }
}
