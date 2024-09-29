package de.byteingpython.sshGame.friends;

import de.byteingpython.sshGame.event.Event;

public class FriendUpdateEvent implements Event {
    private final String friendName;
    private final FriendUpdateEventType type;


    public FriendUpdateEvent(String friendName, FriendUpdateEventType type) {
        this.friendName = friendName;
        this.type = type;
    }

    public String getFriendName() {
        return friendName;
    }

    public FriendUpdateEventType getType() {
        return type;
    }
}

