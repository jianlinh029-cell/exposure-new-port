package io.github.mortuusars.exposure.util;

public class RateLimiter {
    private final int maxTokens;
    private final int refillRate;
    private double currentTokens;
    private long lastRefillTimestamp;

    public RateLimiter(int maxTokens, int refillRatePerSecond) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRatePerSecond;
        this.currentTokens = maxTokens;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    private void refillTokens() {
        long now = System.currentTimeMillis();
        long timeElapsed = now - lastRefillTimestamp;
        double tokensToAdd = (timeElapsed / 1000.0) * refillRate;
        currentTokens = Math.min(maxTokens, currentTokens + tokensToAdd);
        lastRefillTimestamp = now;
    }

    public synchronized boolean tryConsume() {
        refillTokens();
        if (currentTokens >= 1) {
            currentTokens -= 1;
            return true;
        }
        return false;
    }
}