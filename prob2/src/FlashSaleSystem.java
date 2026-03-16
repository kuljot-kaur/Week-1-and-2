import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class InventoryManager {

    // productId -> stockCount
    private ConcurrentHashMap<String, AtomicInteger> inventory;

    // productId -> waiting list (FIFO)
    private ConcurrentHashMap<String, LinkedHashMap<Integer, Integer>> waitingList;

    public InventoryManager() {
        inventory = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    // Add product with stock
    public void addProduct(String productId, int stock) {
        inventory.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new LinkedHashMap<>());
    }

    // Check stock availability
    public String checkStock(String productId) {
        AtomicInteger stock = inventory.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        return stock.get() + " units available";
    }

    // Purchase item (Thread-safe)
    public synchronized String purchaseItem(String productId, int userId) {

        AtomicInteger stock = inventory.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        if (stock.get() > 0) {
            int remaining = stock.decrementAndGet();
            return "Success, " + remaining + " units remaining";
        } else {

            LinkedHashMap<Integer, Integer> queue = waitingList.get(productId);
            int position = queue.size() + 1;
            queue.put(userId, position);

            return "Added to waiting list, position #" + position;
        }
    }

    // Print waiting list
    public void printWaitingList(String productId) {
        LinkedHashMap<Integer, Integer> queue = waitingList.get(productId);

        System.out.println("Waiting list for " + productId + ":");

        for (Map.Entry<Integer, Integer> entry : queue.entrySet()) {
            System.out.println("User " + entry.getKey() + " -> Position " + entry.getValue());
        }
    }
}

public class FlashSaleSystem {

    public static void main(String[] args) throws InterruptedException {

        InventoryManager manager = new InventoryManager();

        manager.addProduct("IPHONE15_256GB", 100);

        System.out.println(manager.checkStock("IPHONE15_256GB"));

        // Simulating concurrent users
        List<Thread> users = new ArrayList<>();

        for (int i = 1; i <= 105; i++) {
            int userId = i;

            Thread t = new Thread(() -> {
                String result = manager.purchaseItem("IPHONE15_256GB", userId);
                System.out.println("User " + userId + ": " + result);
            });

            users.add(t);
            t.start();
        }

        for (Thread t : users) {
            t.join();
        }

        manager.printWaitingList("IPHONE15_256GB");
    }
}