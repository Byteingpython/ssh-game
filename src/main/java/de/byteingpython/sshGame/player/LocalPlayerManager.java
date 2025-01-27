package de.byteingpython.sshGame.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LocalPlayerManager implements PlayerManager {

    private final Map<String, Player> players = new HashMap<>();
    private final LocaleManager localeManager;

    public LocalPlayerManager(LocaleManager localeManager) {
        this.localeManager = localeManager;
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        return Optional.ofNullable(players.get(name));
    }

    @Override
    public void registerPlayer(Player player) throws IllegalArgumentException {
        if (players.containsKey(player.getName())) {
            throw new IllegalArgumentException("Player already exists: " + player.getName());
        }
        player.setLocale(localeManager.getLocale(player));
        players.put(player.getName(), player);
    }

    @Override
    public void unregisterPlayer(Player player) throws IllegalArgumentException {
        if (!players.containsKey(player.getName())) {
            throw new IllegalArgumentException("Player does not exist: " + player.getName());
        }
        players.remove(player.getName());
    }

    @Override
    public LocaleManager getLocaleManager() {
        return localeManager;
    }
}
