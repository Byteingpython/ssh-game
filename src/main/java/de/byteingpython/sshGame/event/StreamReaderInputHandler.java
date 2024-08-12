package de.byteingpython.sshGame.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StreamReaderInputHandler implements InputEventHandler {

    private Thread thread;
    private List<InputListener> listeners = new ArrayList<>();

    public StreamReaderInputHandler(InputStream inputStream) {
        this.thread = new Thread(() -> {
            try {
                while (true) {
                    int input = inputStream.read();
                    for (InputListener listener : listeners) {
                        listener.onInput(input);
                    }
                }
            } catch (IOException e) {
                   throw new RuntimeException(e);
            }
        });
        this.thread.start();
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
