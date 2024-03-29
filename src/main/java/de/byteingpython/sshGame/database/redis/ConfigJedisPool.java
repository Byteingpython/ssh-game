package de.byteingpython.sshGame.database.redis;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import redis.clients.jedis.JedisPool;

import java.util.NoSuchElementException;

public class ConfigJedisPool extends JedisPool {
    public ConfigJedisPool(ConfigurationProvider configurationProvider) throws NoSuchElementException {
        super(configurationProvider.getString("REDIS_HOST").orElseThrow(), configurationProvider.getInt("REDIS_PORT").orElseThrow());
    }
}
