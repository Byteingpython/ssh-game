package de.byteingpython.sshGame.friends;

import de.byteingpython.sshGame.player.Player;

import java.util.List;

public interface FriendManager {
    /**
     * Get the friends of this player
     *
     * @param player The player whose friends should be queried
     * @return A list of the names of friends
     */
    public List<String> getFriends(Player player);

    /**
     * Get the friends of this player
     *
     * @param playerName The player whose friends should be queried
     * @return A list of the names of friends
     */
    public List<String> getFriends(String playerName);

    /**
     * Add a friend to a player
     *
     * @param player The player whose friend should be added
     * @param friend The name of the friend to be added
     * @throws IllegalArgumentException Is thrown when the friend does not exist
     */
    public void addFriend(Player player, String friend) throws IllegalArgumentException;

    /**
     * Add a friend to a player
     *
     * @param player The player whose friend should be added
     * @param friend The friend to be added
     */
    public void addFriend(Player player, Player friend);

    /**
     * Remove a friend of a player
     *
     * @param player The player whose friend should be removed
     * @param friend The name of the friend to be removed
     * @throws IllegalArgumentException Is thrown if the friend does not exist
     */
    public void removeFriend(Player player, String friend) throws IllegalArgumentException;

    /**
     * Remove a friend of a player
     *
     * @param player The player whose friend should be removed
     * @param friend The name of the friend to be removed
     * @throws IllegalArgumentException Is thrown if the friend does not exist
     */
    public void removeFriend(Player player, Player friend) throws IllegalArgumentException;
}
