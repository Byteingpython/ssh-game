package de.byteingpython.sshGame.utils;

/**
 * Describes a Message displayed to the Player
 *
 * @param message  The message to be shown
 * @param duration The duration that this message shall be shown for in milliseconds
 */
public record Message(String message, long duration) {
}
