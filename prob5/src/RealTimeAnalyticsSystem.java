import java.util.*;
import java.util.concurrent.*;

class PageEvent {
    String url;
    String userId;
    String source;

    public PageEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class AnalyticsDashboard {

    // pageUrl -> total visits
    private ConcurrentHashMap<String, Integer> pageViews = new ConcurrentHashMap<>();

    // pageUrl -> unique users
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // traffic source -> count
    private ConcurrentHashMap<String, Integer> trafficSources = new ConcurrentHashMap<>();

    // Process incoming page event
    public void processEvent(PageEvent event) {

        // Update page views
        pageViews.merge(event.url, 1, Integer::sum);

        // Update unique visitors
        uniqueVisitors.putIfAbsent(event.url, ConcurrentHashMap.newKeySet());
        uniqueVisitors.get(event.url).add(event.userId);

        // Update traffic source
        trafficSources.merge(event.source, 1, Integer::sum);
    }

    // Get Top N pages
    private List<Map.Entry<String, Integer>> getTopPages(int n) {

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {

            pq.offer(entry);

            if (pq.size() > n) {
                pq.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>();

        while (!pq.isEmpty()) {
            result.add(pq.poll());
        }

        Collections.reverse(result);

        return result;
    }

    // Display dashboard
    public void getDashboard() {

        System.out.println("\n===== REAL-TIME DASHBOARD =====");

        List<Map.Entry<String, Integer>> topPages = getTopPages(10);

        System.out.println("Top Pages:");

        int rank = 1;

        for (Map.Entry<String, Integer> entry : topPages) {

            String page = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.get(page).size();

            System.out.println(rank + ". " + page +
                    " - " + views + " views (" + unique + " unique)");

            rank++;
        }

        System.out.println("\nTraffic Sources:");

        int total = trafficSources.values().stream().mapToInt(i -> i).sum();

        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {

            double percent = (entry.getValue() * 100.0) / total;

            System.out.println(entry.getKey() + ": "
                    + String.format("%.1f", percent) + "%");
        }

        System.out.println("===============================");
    }
}

public class RealTimeAnalyticsSystem {

    public static void main(String[] args) {

        AnalyticsDashboard dashboard = new AnalyticsDashboard();

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);

        // Dashboard refresh every 5 seconds
        scheduler.scheduleAtFixedRate(
                dashboard::getDashboard,
                5,
                5,
                TimeUnit.SECONDS
        );

        // Simulate streaming traffic
        String[] pages = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai-revolution",
                "/world/elections"
        };

        String[] sources = {
                "google",
                "facebook",
                "direct",
                "twitter"
        };

        Random random = new Random();

        while (true) {

            String url = pages[random.nextInt(pages.length)];
            String user = "user_" + random.nextInt(10000);
            String source = sources[random.nextInt(sources.length)];

            dashboard.processEvent(new PageEvent(url, user, source));

            try {
                Thread.sleep(50); // simulate incoming traffic
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}