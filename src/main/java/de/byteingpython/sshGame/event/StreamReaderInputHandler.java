package de.byteingpython.sshGame.event;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class StreamReaderInputHandler implements InputEventHandler {

    private final ArrayList<InputListener> listeners = new ArrayList<>();

    public StreamReaderInputHandler(InputStream inputStream) {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    int input = inputStream.read();
                    for (InputListener listener : (ArrayList<InputListener>)listeners.clone()) {
                        listener.onInput(input);
                    }
                }
            } catch (IOException e) {
                   throw new RuntimeException(e);
            }
        });
        thread.start();
    }

    @Override
    public void registerListener(InputListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(InputListener listener) {
        listeners.remove(listener);
    }
}
