package de.byteingpython.sshGame.games;

import java.util.Optional;

public interface PlayerManager {
    Optional<Player> getPlayer(String name);

    void registerPlayer(Player player);

    void unregisterPlayer(Player player);
}
