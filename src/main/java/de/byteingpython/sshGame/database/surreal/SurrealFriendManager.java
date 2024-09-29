package de.byteingpython.sshGame.database.surreal;

import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.friends.FriendManager;
import de.byteingpython.sshGame.friends.FriendRequest;
import de.byteingpython.sshGame.friends.FriendUpdateEvent;
import de.byteingpython.sshGame.friends.FriendUpdateEventType;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.player.PlayerManager;
import de.byteingpython.sshGame.screen.LobbyScreenMessageEvent;
import de.byteingpython.sshGame.utils.Message;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SurrealFriendManager implements FriendManager {
    private final SyncSurrealDriver driver;
    private final ConfigurationProvider configurationProvider;
    private final PlayerManager playerManager;

    public SurrealFriendManager(ConfigurationProvider config, PlayerManager playerManager) throws ConfigurationException {
        driver = new ConfigSurrealDriver(config);
        this.configurationProvider = config;
        this.playerManager = playerManager;
        String sql = """
                DEFINE TABLE IF NOT EXISTS friend_of TYPE RELATION IN user OUT user ENFORCED;
                DEFINE TABLE IF NOT EXISTS friend_request IN user OUT user ENFORCED;
                DEFINE INDEX IF NOT EXISTS unique_friend_requests ON TABLE friend_request COLUMNS in, out UNIQUE;
                DEFINE FIELD IF NOT EXISTS key ON TABLE friend_of VALUE <string>array::sort([$this.in, $this.out]);
                DEFINE INDEX IF NOT EXISTS only_one_friendship ON TABLE friend_of
                """;
        driver.query(sql, Map.of(), Object.class);
    }

    @Override
    public List<String> getFriends(Player player) {
        return getFriends(player.getName());
    }

    @Override
    public List<String> getFriends(String playerName) throws IllegalArgumentException {
        List<QueryResult<FriendList>> friendsQueryResult = driver.query("SELECT array::complement(<->friend_of<->user, [id]).name as friends FROM user WHERE name=$name", Map.of("name", playerName), FriendList.class);
        if(friendsQueryResult.get(0).getResult().isEmpty()) {
            throw new IllegalArgumentException("This player does not exist");
        }
        return friendsQueryResult.get(0).getResult().get(0).getFriends();
    }

    @Override
    public void createFriendRequest(Player player, String friend) throws IllegalArgumentException {
        if(player.getName().equals(friend)){
            throw new IllegalArgumentException("You cannot be friends with yourself!");
        }
        if (getFriends(player).contains(friend)) {
            player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message("You are already friends with this player", 3000)));
        }
        try {
            driver.query("RELATE (SELECT VALUE id FROM user WHERE name=$playerName)->friend_request->(SELECT VALUE id FROM user WHERE name=$friendName) SET created=time::now()", Map.of("friendName", friend, "playerName", player.getName()), Object.class);
            Optional<Player> target = playerManager.getPlayer(friend);
            target.ifPresent(value -> value.getEventHandler().handle(new FriendUpdateEvent(player.getName(), FriendUpdateEventType.REQUESTED)));
        } catch (Exception e) {
            player.getEventHandler().handle(new LobbyScreenMessageEvent(new Message("You have already sent this player a request", 3000)));
        }
    }

    @Override
    public void createFriendRequest(Player player, Player friend) {
        createFriendRequest(player, friend.getName());
    }

    @Override
    public void removeFriend(Player player, String friend) throws IllegalArgumentException {
        if(player.getName().equals(friend)){
            throw new IllegalArgumentException("You cannot breakup with yourself!");
        }
        driver.query("DELETE array::at((SELECT VALUE id FROM user WHERE name=$playerName), 0)<->friend_of WHERE in.name=$friendName || out.name=$friendName", Map.of("friendName", friend, "playerName", player.getName()), Object.class);
        Optional<Player> target = playerManager.getPlayer(friend);
        target.ifPresent(value -> value.getEventHandler().handle(new FriendUpdateEvent(player.getName(), FriendUpdateEventType.REMOVED)));
    }

    @Override
    public void removeFriend(Player player, Player friend) throws IllegalArgumentException {
        removeFriend(player, friend.getName());
    }

    @Override
    public List<FriendRequest> getFriendRequests(Player player) {
        String ttl = configurationProvider.getString("FRIEND_REQUEST_TTL").orElse("1w");
        //TODO: Allow for custom ttl
        List<QueryResult<DatabaseFriendRequest>> friendRequests = driver.query("SELECT <-(friend_request WHERE created>time::now()-1w)<-user.name as requests FROM user WHERE name=$playerName", Map.of("playerName", player.getName(), "ttl", ttl), DatabaseFriendRequest.class);
        List<FriendRequest> friendRequestList = new ArrayList<>();
        for(String source:friendRequests.get(0).getResult().get(0).getRequests()){
            friendRequestList.add(new SurrealFriendRequest(source, player.getName(), driver, playerManager));
        }
        return friendRequestList;
    }
}
