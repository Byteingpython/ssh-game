package de.byteingpython.sshGame.ssh;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.surrealdb.driver.model.QueryResult;
import de.byteingpython.sshGame.Main;
import de.byteingpython.sshGame.types.User;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class PasswordAuthenticator implements org.apache.sshd.server.auth.password.PasswordAuthenticator {

    private final Main main;
    private final HashFunction hashing=Hashing.sha256();

    public PasswordAuthenticator(Main main) {
        this.main = main;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession serverSession) throws PasswordChangeRequiredException, AsyncAuthException {
        main.getLogger().info("Authenticating user "+username);
        List<QueryResult<User>> users = main.getDatabaseDriver().query("SELECT passwordHash FROM user WHERE name=$name", Map.of("name", username), User.class);
        String hashedPassword= hashing.hashString(password, StandardCharsets.UTF_8).toString();
        if(users.get(0).getResult().isEmpty()){
            main.getLogger().info("User "+username+" not found, creating new user");
            main.getDatabaseDriver().create("user", new User(username, hashedPassword));
            return true;
        }
        return users.get(0).getResult().get(0).getPasswordHash().equals(hashedPassword);
    }
}
