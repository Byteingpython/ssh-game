package de.byteingpython.sshGame.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessageQueue {
    private final Timer timer = new Timer();
    private final List<Message> messages = new ArrayList<>();
    private String placeholder;
    private boolean busy = false;

    public MessageQueue(String placeholder) {
        this.placeholder = placeholder;
    }

    public MessageQueue() {
        placeholder = "";
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    private void startNextMessage() {
        Message message = messages.get(0);
        busy = true;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                messages.remove(message);
                if (messages.isEmpty()) {
                    busy = false;
                    return;
                }
                startNextMessage();
            }
        };
        timer.schedule(task, message.duration());
    }

    public String getCurrentText() {
        if (!busy) {
            if (!messages.isEmpty()) {
                startNextMessage();
            } else {
                return placeholder;
            }
        }
        return messages.get(0).message();
    }
}
