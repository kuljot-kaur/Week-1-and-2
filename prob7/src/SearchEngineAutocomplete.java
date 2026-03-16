import java.util.*;

class TrieNode {

    Map<Character, TrieNode> children = new HashMap<>();

    // queries passing through this prefix
    Map<String, Integer> queries = new HashMap<>();

    boolean isEnd = false;
}

class AutocompleteSystem {

    private TrieNode root = new TrieNode();

    // Global query frequency
    private HashMap<String, Integer> frequencyMap = new HashMap<>();


    // Add query to system
    public void addQuery(String query, int freq) {

        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + freq);

        TrieNode node = root;

        for (char c : query.toCharArray()) {

            node.children.putIfAbsent(c, new TrieNode());

            node = node.children.get(c);

            node.queries.put(query, frequencyMap.get(query));
        }

        node.isEnd = true;
    }


    // Update frequency when a user searches
    public void updateFrequency(String query) {

        int newFreq = frequencyMap.getOrDefault(query, 0) + 1;

        addQuery(query, 1);

        System.out.println("Updated Frequency: " + newFreq);
    }


    // Get Top K suggestions for prefix
    public List<String> search(String prefix, int k) {

        TrieNode node = root;

        for (char c : prefix.toCharArray()) {

            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }

            node = node.children.get(c);
        }

        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : node.queries.entrySet()) {

            minHeap.offer(entry);

            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        List<String> result = new ArrayList<>();

        while (!minHeap.isEmpty()) {
            result.add(minHeap.poll().getKey());
        }

        Collections.reverse(result);

        return result;
    }
}

public class SearchEngineAutocomplete {

    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        system.addQuery("java tutorial", 1234567);
        system.addQuery("javascript", 987654);
        system.addQuery("java download", 456789);
        system.addQuery("java 21 features", 1000);
        system.addQuery("java interview questions", 800000);

        System.out.println("Suggestions for 'jav':");

        List<String> suggestions = system.search("jav", 10);

        for (String s : suggestions) {
            System.out.println(s);
        }

        system.updateFrequency("java 21 features");
    }
}