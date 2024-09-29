package de.byteingpython.sshGame.database.surreal;

import com.surrealdb.driver.SyncSurrealDriver;
import de.byteingpython.sshGame.friends.FriendRequest;
import de.byteingpython.sshGame.friends.FriendUpdateEvent;
import de.byteingpython.sshGame.friends.FriendUpdateEventType;
import de.byteingpython.sshGame.player.Player;
import de.byteingpython.sshGame.player.PlayerManager;

import java.util.Map;
import java.util.Optional;

public class SurrealFriendRequest extends FriendRequest {
    private final SyncSurrealDriver driver;
    private final PlayerManager playerManager;

    protected SurrealFriendRequest(String source, String target, SyncSurrealDriver driver, PlayerManager playerManager) {
        super(source, target);
        this.driver = driver;
        this.playerManager = playerManager;
    }

    @Override
    public void accept() {
        String sql = """
            BEGIN TRANSACTION;
            RELATE (SELECT VALUE id FROM user WHERE name=$target)->friend_of->(SELECT VALUE id FROM user WHERE name=$source);
            DELETE friend_request WHERE (in.name=$source && out.name=$target)||(in.name=$target && out.name=$source);
            COMMIT TRANSACTION;
        """;
        driver.query(sql, Map.of("source", getSource(), "target", getTarget()), Object.class);
        Optional<Player> sourcePlayer = playerManager.getPlayer(getSource());
        sourcePlayer.ifPresent(player -> player.getEventHandler().handle(new FriendUpdateEvent(getTarget(), FriendUpdateEventType.ADDED)));
    }

    @Override
    public void decline() {
        driver.query("DELETE friend_request WHERE in.name=$source && out.name=$target", Map.of("source", getSource(), "target", getTarget()), Object.class);
    }
}
