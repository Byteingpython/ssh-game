package de.byteingpython.sshGame.event;


import de.byteingpython.sshGame.player.Player;

import java.io.IOException;
import java.util.ArrayList;

public class StreamReaderInputHandler implements InputEventHandler {

    private final ArrayList<InputListener> listeners = new ArrayList<>();

    public StreamReaderInputHandler(Player player) {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    int input = player.getInputStream().read();
                    for (InputListener listener : (ArrayList<InputListener>)listeners.clone()) {
                        listener.onInput(input);
                    }
                    player.getEventHandler().handle(new InputEvent(player, input));
                    if(input == -1){
                        break;
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
