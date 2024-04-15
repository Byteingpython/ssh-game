package de.byteingpython.sshGame.games.matchmaking;

import de.byteingpython.sshGame.games.Lobby;
import org.checkerframework.common.returnsreceiver.qual.This;


/**
 * This interface is responsible for matchmaking lobbies and starting games with the required Parameters.
 * It is also responsible for providing an end callback to the game.
 */
public interface Matchmaker {
    /**
     * This method is responsible for matchmaking a lobby
     * @param lobby
     * @throws IllegalArgumentException throws IllegalArgumentException if the lobby is not valid/cannot be matchmaked e.g. to big or to small
     */
    public void matchmake(Lobby lobby) throws IllegalArgumentException;
    public void cancelMatchmaking(Lobby lobby);
}
