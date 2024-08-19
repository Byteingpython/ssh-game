package de.byteingpython.sshGame.database.surreal;

public class FriendList {
    Friends friends;
    public FriendList(){
        friends = new Friends();
    }

    public Friends getFriends() {
        return friends;
    }
}
