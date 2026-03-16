import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-Level Cache System
 * L1: In-Memory (10,000 videos)
 * L2: SSD-backed simulated (100,000 videos)
 * L3: Database (slow)
 */
public class MultiLevelCacheDemo {

    /* ===================== VIDEO DATA ===================== */
    static class VideoData {
        String videoId;
        String content;

        VideoData(String videoId, String content) {
            this.videoId = videoId;
            this.content = content;
        }
    }

    /* ===================== CACHE STATS ===================== */
    static class CacheStats {
        long hits = 0;
        long misses = 0;
        long totalTimeMs = 0;

        void hit(long time) {
            hits++;
            totalTimeMs += time;
        }

        void miss(long time) {
            misses++;
            totalTimeMs += time;
        }

        double hitRate() {
            long total = hits + misses;
            return total == 0 ? 0 : (double) hits / total * 100;
        }

        double avgTime() {
            long total = hits + misses;
            return total == 0 ? 0 : (double) totalTimeMs / total;
        }
    }

    /* ===================== LRU CACHE ===================== */
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        LRUCache(int capacity) {
            super(capacity, 0.75f, true); // access-order
            this.capacity = capacity;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    /* ===================== DATABASE (L3) ===================== */
    static class Database {
        private final Map<String, VideoData> store = new ConcurrentHashMap<>();

        Database() {
            for (int i = 1; i <= 1_000_000; i++) {
                store.put("video_" + i,
                        new VideoData("video_" + i, "CONTENT_" + i));
            }
        }

        VideoData fetch(String videoId) {
            sleep(150); // simulate DB latency
            return store.get(videoId);
        }

        void update(VideoData video) {
            store.put(video.videoId, video);
        }
    }

    /* ===================== MULTI LEVEL CACHE ===================== */
    static class MultiLevelCache {

        private final LRUCache<String, VideoData> l1 =
                new LRUCache<>(10_000);

        private final LRUCache<String, VideoData> l2 =
                new LRUCache<>(100_000);

        private final Database database = new Database();
        private final Map<String, Integer> accessCount = new ConcurrentHashMap<>();

        private final CacheStats l1Stats = new CacheStats();
        private final CacheStats l2Stats = new CacheStats();
        private final CacheStats l3Stats = new CacheStats();

        private static final int PROMOTION_THRESHOLD = 3;

        synchronized VideoData getVideo(String videoId) {

            long start;

            // -------- L1 --------
            start = System.currentTimeMillis();
            VideoData data = l1.get(videoId);
            if (data != null) {
                l1Stats.hit(System.currentTimeMillis() - start);
                return data;
            }
            l1Stats.miss(1);

            // -------- L2 --------
            start = System.currentTimeMillis();
            data = l2.get(videoId);
            if (data != null) {
                l2Stats.hit(System.currentTimeMillis() - start);
                promote(videoId, data);
                return data;
            }
            l2Stats.miss(5);

            // -------- L3 --------
            start = System.currentTimeMillis();
            data = database.fetch(videoId);
            l3Stats.hit(System.currentTimeMillis() - start);

            if (data != null) {
                l2.put(videoId, data);
                accessCount.put(videoId, 1);
            }
            return data;
        }

        private void promote(String videoId, VideoData data) {
            int count = accessCount.getOrDefault(videoId, 0) + 1;
            accessCount.put(videoId, count);

            if (count >= PROMOTION_THRESHOLD) {
                l1.put(videoId, data);
            }
        }

        synchronized void invalidate(String videoId) {
            l1.remove(videoId);
            l2.remove(videoId);
            accessCount.remove(videoId);
        }

        synchronized void update(VideoData video) {
            database.update(video);
            invalidate(video.videoId);
        }

        void printStats() {
            System.out.printf("L1: Hit Rate %.2f%%, Avg Time %.2fms%n",
                    l1Stats.hitRate(), l1Stats.avgTime());
            System.out.printf("L2: Hit Rate %.2f%%, Avg Time %.2fms%n",
                    l2Stats.hitRate(), l2Stats.avgTime());
            System.out.printf("L3: Hit Rate %.2f%%, Avg Time %.2fms%n",
                    l3Stats.hitRate(), l3Stats.avgTime());
        }
    }

    /* ===================== DEMO ===================== */
    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();

        cache.getVideo("video_123");
        cache.getVideo("video_123");
        cache.getVideo("video_123");

        cache.getVideo("video_999");
        cache.getVideo("video_999");

        cache.printStats();
    }

    /* ===================== UTIL ===================== */
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}