package de.byteingpython.sshGame.games;

import java.util.List;
import java.util.UUID;

public interface LobbyManager {
    public Lobby createLobby();
    public void removeLobby(Lobby lobby);
    public Lobby getLobby(UUID id);
    public List<Lobby> getLobbies();
}
