package de.byteingpython.sshGame.event;

public interface InputEventHandler {
    public void registerListener(InputListener listener);
    public void unregisterListener(InputListener listener);
}
