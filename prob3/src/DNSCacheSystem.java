import java.util.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime;

    DNSEntry(String domain, String ipAddress, int ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

class DNSCache {

    private final int capacity;

    // LRU Cache using LinkedHashMap
    private LinkedHashMap<String, DNSEntry> cache;

    private int hits = 0;
    private int misses = 0;

    public DNSCache(int capacity) {
        this.capacity = capacity;

        cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        startCleanupThread();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {

        long startTime = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null) {

            if (!entry.isExpired()) {
                hits++;
                long time = (System.nanoTime() - startTime) / 1_000_000;
                System.out.println("Cache HIT → " + entry.ipAddress + " (" + time + " ms)");
                return entry.ipAddress;
            } else {
                System.out.println("Cache EXPIRED → " + domain);
                cache.remove(domain);
            }
        }

        misses++;

        // Simulate upstream DNS query
        String newIp = queryUpstreamDNS(domain);

        DNSEntry newEntry = new DNSEntry(domain, newIp, 5); // TTL = 5s for demo
        cache.put(domain, newEntry);

        System.out.println("Cache MISS → Query upstream → " + newIp);
        return newIp;
    }

    // Simulated DNS lookup
    private String queryUpstreamDNS(String domain) {
        Random r = new Random();
        return "172.217.14." + r.nextInt(255);
    }

    // Cache statistics
    public void getCacheStats() {
        int total = hits + misses;

        double hitRate = total == 0 ? 0 : ((double) hits / total) * 100;

        System.out.println("Hits: " + hits);
        System.out.println("Misses: " + misses);
        System.out.println("Hit Rate: " + hitRate + "%");
    }

    // Background cleanup thread
    private void startCleanupThread() {

        Thread cleaner = new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(3000);

                    synchronized (this) {
                        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

                        while (it.hasNext()) {
                            Map.Entry<String, DNSEntry> entry = it.next();

                            if (entry.getValue().isExpired()) {
                                it.remove();
                                System.out.println("Removed expired entry: " + entry.getKey());
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        });

        cleaner.setDaemon(true);
        cleaner.start();
    }
}

public class DNSCacheSystem {

    public static void main(String[] args) throws Exception {

        DNSCache dnsCache = new DNSCache(5);

        dnsCache.resolve("google.com");
        dnsCache.resolve("google.com");

        Thread.sleep(6000);

        dnsCache.resolve("google.com");

        dnsCache.getCacheStats();
    }
}