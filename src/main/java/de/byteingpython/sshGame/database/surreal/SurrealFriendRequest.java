package de.byteingpython.sshGame.database.surreal;

import com.surrealdb.driver.SyncSurrealDriver;
import de.byteingpython.sshGame.friends.FriendRequest;

import java.util.Map;

public class SurrealFriendRequest extends FriendRequest {
    private final SyncSurrealDriver driver;

    protected SurrealFriendRequest(String source, String target, SyncSurrealDriver driver) {
        super(source, target);
        this.driver = driver;
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
    }

    @Override
    public void decline() {
        driver.query("DELETE friend_request WHERE in.name=$source && out.name=$target", Map.of("source", getSource(), "target", getTarget()), Object.class);
    }
}
