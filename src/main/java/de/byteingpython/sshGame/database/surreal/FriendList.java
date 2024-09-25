package de.byteingpython.sshGame.database.surreal;

import java.util.ArrayList;
import java.util.List;

public class FriendList {
    List<String> friends;
    public FriendList(){
        friends = new ArrayList<>();
    }

    public List<String> getFriends() {
        return friends;
    }
}
