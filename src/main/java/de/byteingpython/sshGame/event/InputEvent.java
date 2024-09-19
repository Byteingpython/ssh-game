package de.byteingpython.sshGame.event;

import de.byteingpython.sshGame.player.Player;

/**
 * An event that is thrown when a player presses an input
 */
public class InputEvent implements Event {
    private final Player player;
    private final int input;

    public InputEvent(Player player, int input) {
        this.player = player;
        this.input = input;
    }

    /**
     * Get the player that pressed the input
     * @return The player that pressed the input
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the input as an ascii integer
     * @return The input
     */
    public int getInput() {
        return input;
    }
}
