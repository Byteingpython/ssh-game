package de.byteingpython.sshGame.database.surreal;

import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.ssh.auth.CredentialAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ConfigurationException;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SurrealCredentialProvider extends CredentialAuthProvider {
    private final SyncSurrealDriver driver;
    private final Logger logger = LoggerFactory.getLogger(SurrealCredentialProvider.class);

    public SurrealCredentialProvider(ConfigurationProvider config) throws ConfigurationException {
        driver = new ConfigSurrealDriver(config);
    }


    @Override
    public Optional<String> getHashedPassword(String username) {
        List<QueryResult<User>> passwordQueryResult = driver.query("SELECT * FROM user WHERE name=$name", Map.of("name", username), User.class);
        if (!passwordQueryResult.get(0).getResult().isEmpty()) {
            return Optional.ofNullable(passwordQueryResult.get(0).getResult().get(0).getPasswordHash());
        }
        return Optional.empty();
    }

    @Override
    public Optional<PublicKey> getPublicKey(String username) {
        return Optional.empty();
    }

    @Override
    public boolean doesUserExist(String username) {
        List<QueryResult<User>> userQueryResult = driver.query("SELECT * FROM user WHERE name=$name", Map.of("name", username), User.class);
        return !userQueryResult.get(0).getResult().isEmpty();
    }

    @Override
    public void createUserWithHashedPassword(String username, String passwordHash) {
        if (doesUserExist(username)) {
            throw new IllegalArgumentException("User already exists");
        }
        User user = new User(username, passwordHash);
        driver.create("user", user);
    }

    @Override
    public void createUser(String username, PublicKey publicKey) {
        if (doesUserExist(username)) {
            throw new IllegalArgumentException("User already exists");
        }
        User user = new User(username, publicKey);
        driver.create("user", user);
    }

    @Override
    public void updatePasswordHash(String username, String passwordHash) {
        if (!doesUserExist(username)) {
            throw new IllegalArgumentException("User does not exist");
        }
        driver.query("UPDATE user SET passwordHash=$password WHERE name=$name", Map.of("password", passwordHash, "name", username), User.class);
    }

    @Override
    public void updateUserKey(String username, PublicKey publicKey) {
        if (!doesUserExist(username)) {
            throw new IllegalArgumentException("User does not exist");
        }
        User user = driver.select("user:" + username, User.class).get(0);
        user.setPublicKey(publicKey);
        //TODO: Implement this
    }
}
