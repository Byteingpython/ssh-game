package de.byteingpython.sshGame.utils.throttling;

import redis.clients.jedis.Jedis;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RedisThrottler implements Throttler {

    private final Jedis jedis;
    private final int maxRequests;
    private final long timeFrame;

    private final String keyPrefix;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public RedisThrottler(Jedis jedis, int maxRequests, long timeFrame) {
        this.jedis = jedis;
        this.maxRequests = maxRequests;
        this.timeFrame = timeFrame;
        this.keyPrefix = "throttle:";
    }

    public RedisThrottler(Jedis jedis, int maxRequests, long timeFrame, String keyPrefix) {
        this.jedis = jedis;
        this.maxRequests = maxRequests;
        this.timeFrame = timeFrame;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public boolean isAllowed(String key) {
        String currentValue = jedis.get(keyPrefix + key);
        if (currentValue == null) {
            return true;
        } else {
            int current = Integer.parseInt(currentValue);
            return current < maxRequests;
        }
    }

    @Override
    public void throttle(String key) {
        String currentValue = jedis.get(keyPrefix + key);
        if (currentValue == null) {
            jedis.set(keyPrefix + key, "1");
        } else {
            int current = Integer.parseInt(currentValue);
            if (current < maxRequests) {
                jedis.incr(keyPrefix + key);
            }
        }
        scheduler.schedule(() -> jedis.decr(keyPrefix + key), timeFrame, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
