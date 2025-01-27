package de.byteingpython.sshGame.database.surreal;

import java.util.ArrayList;
import java.util.List;

public class DatabaseFriendRequest {
    List<String> requests;

    public DatabaseFriendRequest() {
        this.requests = new ArrayList<>();
    }

    public List<String> getRequests() {
        return requests;
    }
}
