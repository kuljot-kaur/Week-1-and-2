import java.util.*;

public class socialmediausernamechecker {

    // username -> userId
    private HashMap<String, Integer> userDatabase;

    // username -> attempt count
    private HashMap<String, Integer> attemptFrequency;

    public socialmediausernamechecker() {
        userDatabase = new HashMap<>();
        attemptFrequency = new HashMap<>();
    }

    // Register a user
    public void registerUser(String username, int userId) {
        userDatabase.put(username, userId);
    }

    // Check username availability (O(1))
    public boolean checkAvailability(String username) {

        // Track how many times username was attempted
        attemptFrequency.put(username,
                attemptFrequency.getOrDefault(username, 0) + 1);

        return !userDatabase.containsKey(username);
    }

    // Suggest alternative usernames
    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        // Add numbers to username
        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!userDatabase.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        // Replace underscore with dot
        if (username.contains("_")) {
            String suggestion = username.replace("_", ".");
            if (!userDatabase.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {

        String mostAttempted = null;
        int maxAttempts = 0;

        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxAttempts) {
                maxAttempts = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }

        return mostAttempted + " (" + maxAttempts + " attempts)";
    }

    // Main method for testing
    public static void main(String[] args) {

        socialmediausernamechecker checker = new socialmediausernamechecker();

        checker.registerUser("john_doe", 1);
        checker.registerUser("admin", 2);

        System.out.println("john_doe available? " + checker.checkAvailability("john_doe"));
        System.out.println("jane_smith available? " + checker.checkAvailability("jane_smith"));

        System.out.println("Suggestions for john_doe: "
                + checker.suggestAlternatives("john_doe"));

        // Simulate many attempts
        for (int i = 0; i < 10; i++) {
            checker.checkAvailability("admin");
        }

        System.out.println("Most attempted username: " + checker.getMostAttempted());
    }
}