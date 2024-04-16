package de.byteingpython.sshGame.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LocalLobbyMananger implements LobbyManager{

    private Map<UUID, Lobby> lobbies;

    public LocalLobbyMananger() {
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
