import java.util.*;

class Transaction {

    int id;
    int amount;
    String merchant;
    String account;
    long timestamp;

    public Transaction(int id, int amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }
}

class TransactionAnalyzer {

    private List<Transaction> transactions = new ArrayList<>();


    public void addTransaction(Transaction t) {
        transactions.add(t);
    }


    // 1️⃣ Classic Two-Sum
    public void findTwoSum(int target) {

        Map<Integer, Transaction> map = new HashMap<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {

                Transaction match = map.get(complement);

                System.out.println("Pair Found → (" +
                        match.id + ", " + t.id + ")");
            }

            map.put(t.amount, t);
        }
    }


    // 2️⃣ Two-Sum with Time Window (1 hour)
    public void findTwoSumWithinTime(int target, long windowMillis) {

        Map<Integer, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {

                for (Transaction prev : map.get(complement)) {

                    if (Math.abs(t.timestamp - prev.timestamp) <= windowMillis) {

                        System.out.println("Time Window Pair → (" +
                                prev.id + ", " + t.id + ")");
                    }
                }
            }

            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
    }


    // 3️⃣ K-Sum (Recursive)
    public void findKSum(int k, int target) {

        List<Integer> result = new ArrayList<>();

        kSumHelper(0, k, target, result);
    }

    private void kSumHelper(int start, int k, int target, List<Integer> result) {

        if (k == 0 && target == 0) {
            System.out.println("K-Sum Found → " + result);
            return;
        }

        if (k == 0) return;

        for (int i = start; i < transactions.size(); i++) {

            result.add(transactions.get(i).id);

            kSumHelper(i + 1,
                    k - 1,
                    target - transactions.get(i).amount,
                    result);

            result.remove(result.size() - 1);
        }
    }


    // 4️⃣ Duplicate Payment Detection
    public void detectDuplicates() {

        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {

            String key = t.amount + "_" + t.merchant;

            map.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        for (List<Transaction> list : map.values()) {

            if (list.size() > 1) {

                Set<String> accounts = new HashSet<>();

                for (Transaction t : list) {
                    accounts.add(t.account);
                }

                if (accounts.size() > 1) {

                    System.out.println("Duplicate Suspicious Transactions:");

                    for (Transaction t : list) {
                        System.out.println(
                                "ID: " + t.id +
                                        " Amount: " + t.amount +
                                        " Merchant: " + t.merchant +
                                        " Account: " + t.account
                        );
                    }

                    System.out.println();
                }
            }
        }
    }
}

public class FinancialFraudSystem {

    public static void main(String[] args) {

        TransactionAnalyzer analyzer = new TransactionAnalyzer();

        long now = System.currentTimeMillis();

        analyzer.addTransaction(new Transaction(1, 500, "Store A", "acc1", now));
        analyzer.addTransaction(new Transaction(2, 300, "Store B", "acc2", now + 1000));
        analyzer.addTransaction(new Transaction(3, 200, "Store C", "acc3", now + 2000));
        analyzer.addTransaction(new Transaction(4, 500, "Store A", "acc4", now + 3000));

        System.out.println("Two-Sum:");
        analyzer.findTwoSum(500);

        System.out.println("\nTwo-Sum Within 1 Hour:");
        analyzer.findTwoSumWithinTime(500, 3600000);

        System.out.println("\nK-Sum (k=3, target=1000):");
        analyzer.findKSum(3, 1000);

        System.out.println("\nDuplicate Detection:");
        analyzer.detectDuplicates();
    }
}