package de.byteingpython.sshGame.event;

public interface InputEventHandler {
    void registerListener(InputListener listener);

    void unregisterListener(InputListener listener);
}
