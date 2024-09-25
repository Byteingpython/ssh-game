package de.byteingpython.sshGame.friends;

public abstract class FriendRequest {
    private final String source;
    private final String target;

    protected FriendRequest(String source, String target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Get the name of the player who this request is sent to
     * @return The name of the player who
     */
    public String getTarget() {
        return target;
    }

    /**
     * Get the name of the player who sent the request
     * @return The name of the player who sent the request
     */
    public String getSource() {
        return source;
    }

    public abstract void accept();

    public abstract void decline();
}
