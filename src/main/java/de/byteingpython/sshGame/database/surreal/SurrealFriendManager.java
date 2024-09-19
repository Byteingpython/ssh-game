package de.byteingpython.sshGame.database.surreal;

import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.friends.FriendManager;
import de.byteingpython.sshGame.player.Player;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SurrealFriendManager implements FriendManager {
    private final SyncSurrealDriver driver;

    public SurrealFriendManager(ConfigurationProvider config) throws ConfigurationException {
        driver = new ConfigSurrealDriver(config);
    }

    @Override
    public List<String> getFriends(Player player) {
        return getFriends(player.getName());
    }

    @Override
    public List<String> getFriends(String playerName) throws IllegalArgumentException {
        List<QueryResult<FriendList>> friendsQueryResult = driver.query("SELECT friends.name FROM user WHERE name=$name", Map.of("name", playerName), FriendList.class);
        if(friendsQueryResult.get(0).getResult().isEmpty()) {
            throw new IllegalArgumentException("This player does not exist");
        }
        FriendList friendList = friendsQueryResult.get(0).getResult().get(0);
        return friendList.getFriends().getNames();
    }

    @Override
    public void addFriend(Player player, String friend) throws IllegalArgumentException {
        if(player.getName().equals(friend)){
            throw new IllegalArgumentException("You cannot be friends with yourself!");
        }
        driver.query("UPDATE user SET friends+=(SELECT VALUE id FROM user WHERE name=$friendName) WHERE name=$playerName", Map.of("friendName", friend, "playerName", player.getName()), Object.class);
    }

    @Override
    public void addFriend(Player player, Player friend) {
        addFriend(player, friend.getName());
    }

    @Override
    public void removeFriend(Player player, String friend) throws IllegalArgumentException {
        if(player.getName().equals(friend)){
            throw new IllegalArgumentException("You cannot breakup with yourself!");
        }
        driver.query("UPDATE user SET friends-=(SELECT VALUE id FROM user WHERE name=$friendName) WHERE name=$playerName", Map.of("friendName", friend, "playerName", player.getName()), User.class);
    }

    @Override
    public void removeFriend(Player player, Player friend) throws IllegalArgumentException {
        removeFriend(player, friend.getName());
    }
}
