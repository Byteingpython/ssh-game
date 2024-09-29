package de.byteingpython.sshGame.screen;

import de.byteingpython.sshGame.event.Event;
import de.byteingpython.sshGame.utils.Message;

public class LobbyScreenMessageEvent implements Event {
    private final Message message;

    public LobbyScreenMessageEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
