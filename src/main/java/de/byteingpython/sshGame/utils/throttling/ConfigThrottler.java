package de.byteingpython.sshGame.utils.throttling;

import de.byteingpython.sshGame.config.ConfigurationProvider;
import de.byteingpython.sshGame.database.redis.ConfigJedisPool;

public class ConfigThrottler implements Throttler {
    private Throttler throttler;

    public ConfigThrottler(ConfigurationProvider configurationProvider, int maxRequests, long timeFrame, String name) {
        if (configurationProvider.getString("REDIS_HOST").isPresent()) {
            try (ConfigJedisPool jedisPool = new ConfigJedisPool(configurationProvider)) {
                throttler = new RedisThrottler(jedisPool.getResource(), maxRequests, timeFrame, name + ":");
            } catch (Exception e) {
                throttler = new LocalThrottler(maxRequests, timeFrame);
            }
        } else {
            throttler = new LocalThrottler(maxRequests, timeFrame);
        }
    }

    @Override
    public boolean isAllowed(String key) {
        return throttler.isAllowed(key);
    }

    @Override
    public void throttle(String key) {
        throttler.throttle(key);
    }
}
