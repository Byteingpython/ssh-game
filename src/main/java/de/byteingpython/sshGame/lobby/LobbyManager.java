package de.byteingpython.sshGame.lobby;

import java.util.List;
import java.util.UUID;

public interface LobbyManager {
    Lobby createLobby();

    void removeLobby(Lobby lobby);

    Lobby getLobby(UUID id);

    List<Lobby> getLobbies();
}
