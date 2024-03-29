package de.byteingpython.sshGame.utils.throttling;

public interface Throttler {
    /**
     * Check if a key is allowed to make a request
     *
     * @param key The key to check
     */
    boolean isAllowed(String key);

    /**
     * Increase the request counter for a key
     *
     * @param key The key to increase the counter for
     */
    void throttle(String key);
}
