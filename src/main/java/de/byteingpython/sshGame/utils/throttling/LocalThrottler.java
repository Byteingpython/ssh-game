package de.byteingpython.sshGame.utils.throttling;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class LocalThrottler implements Throttler {
    private final int maxRequests;
    private final long timeFrame;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, Integer> requestCounter = new ConcurrentHashMap<>();

    /**
     * Constructor for LocalThrottler
     *
     * @param maxRequests Maximum number of requests
     * @param timeFrame   Time frame in milliseconds
     */
    public LocalThrottler(int maxRequests, long timeFrame) {
        this.maxRequests = maxRequests;
        this.timeFrame = timeFrame;
    }


    @Override
    public boolean isAllowed(String key) {
        return requestCounter.getOrDefault(key, 0) < maxRequests;
    }

    @Override
    public void throttle(String key) {
        requestCounter.put(key, requestCounter.getOrDefault(key, 0) + 1);
        scheduler.schedule(() -> requestCounter.put(key, requestCounter.get(key) - 1), timeFrame, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
