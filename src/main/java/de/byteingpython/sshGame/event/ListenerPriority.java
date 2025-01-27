package de.byteingpython.sshGame.event;

/**
 * Marks when an EventListener should be called. The higher the priority, the earlier it will be executed
 */
public enum ListenerPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST
}
