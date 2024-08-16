package de.byteingpython.sshGame.database.surreal;

import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.friends.FriendManager;
import de.byteingpython.sshGame.player.Player;

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
        List<QueryResult<User>> friendsQueryResult = driver.query("SELECT * FROM user WHERE name=$name", Map.of("name", playerName), User.class);
        if(friendsQueryResult.get(0).getResult().isEmpty()) {
            throw new IllegalArgumentException("This player does not exist");
        }
        User user = friendsQueryResult.get(0).getResult().get(0);
        List<String> friends = new ArrayList<>();
        for(User iterUser:user.getFriends()){
            friends.add(iterUser.getName());
        }
        return friends;
    }

    @Override
    public void addFriend(Player player, String friend) throws IllegalArgumentException {
        driver.query("UPDATE user SET friends+=(SELECT VALUE id FROM user WHERE name=$name) WHERE name=$playerName", Map.of("name", friend, "playerName", player.getName()), User.class);
    }

    @Override
    public void addFriend(Player player, Player friend) {

    }

    @Override
    public void removeFriend(Player player, String friend) throws IllegalArgumentException {

    }

    @Override
    public void removeFriend(Player player, Player friend) throws IllegalArgumentException {

    }
}
