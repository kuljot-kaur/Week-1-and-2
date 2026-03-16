import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class TokenBucket {

    private int maxTokens;
    private double refillRate; // tokens per second
    private double tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, int refillPerHour) {
        this.maxTokens = maxTokens;
        this.refillRate = refillPerHour / 3600.0;
        this.tokens = maxTokens;
        this.lastRefillTime = System.nanoTime();
    }

    // Refill tokens based on elapsed time
    private void refill() {

        long now = System.nanoTime();
        double seconds = (now - lastRefillTime) / 1_000_000_000.0;

        double newTokens = seconds * refillRate;

        tokens = Math.min(maxTokens, tokens + newTokens);

        lastRefillTime = now;
    }

    // Try consuming a token
    public synchronized boolean allowRequest() {

        refill();

        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }

        return false;
    }

    public synchronized int getRemainingTokens() {
        refill();
        return (int) tokens;
    }
}

class RateLimiter {

    // clientId -> token bucket
    private ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();

    private int MAX_REQUESTS = 1000;

    public boolean checkRateLimit(String clientId) {

        clients.putIfAbsent(clientId,
                new TokenBucket(MAX_REQUESTS, MAX_REQUESTS));

        TokenBucket bucket = clients.get(clientId);

        boolean allowed = bucket.allowRequest();

        if (allowed) {
            System.out.println("Allowed (" +
                    bucket.getRemainingTokens() + " requests remaining)");
        } else {
            System.out.println("Denied (0 requests remaining)");
        }

        return allowed;
    }

    public void getRateLimitStatus(String clientId) {

        TokenBucket bucket = clients.get(clientId);

        if (bucket == null) {
            System.out.println("Client not found");
            return;
        }

        int remaining = bucket.getRemainingTokens();

        System.out.println("{used: " + (MAX_REQUESTS - remaining)
                + ", limit: " + MAX_REQUESTS
                + ", remaining: " + remaining + "}");
    }
}

public class APIGatewayRateLimiter {

    public static void main(String[] args) {

        RateLimiter limiter = new RateLimiter();

        String client = "abc123";

        for (int i = 0; i < 1005; i++) {
            limiter.checkRateLimit(client);
        }

        limiter.getRateLimitStatus(client);
    }
}