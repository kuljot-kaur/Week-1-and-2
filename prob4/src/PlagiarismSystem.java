import java.util.*;

class PlagiarismDetector {

    // n-gram -> set of document IDs containing that n-gram
    private HashMap<String, Set<String>> ngramIndex = new HashMap<>();

    // document -> list of its n-grams
    private HashMap<String, List<String>> documentNgrams = new HashMap<>();

    private int N = 5; // 5-grams

    // Add document to database
    public void addDocument(String documentId, String content) {

        List<String> ngrams = generateNgrams(content);
        documentNgrams.put(documentId, ngrams);

        for (String gram : ngrams) {
            ngramIndex.putIfAbsent(gram, new HashSet<>());
            ngramIndex.get(gram).add(documentId);
        }
    }

    // Generate n-grams
    private List<String> generateNgrams(String text) {

        List<String> grams = new ArrayList<>();

        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {

            StringBuilder gram = new StringBuilder();

            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }

            grams.add(gram.toString().trim());
        }

        return grams;
    }

    // Analyze a new document
    public void analyzeDocument(String documentId, String content) {

        List<String> newDocNgrams = generateNgrams(content);

        System.out.println("Extracted " + newDocNgrams.size() + " n-grams");

        HashMap<String, Integer> matchCounts = new HashMap<>();

        for (String gram : newDocNgrams) {

            if (ngramIndex.containsKey(gram)) {

                for (String doc : ngramIndex.get(gram)) {

                    matchCounts.put(doc, matchCounts.getOrDefault(doc, 0) + 1);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {

            String docId = entry.getKey();
            int matches = entry.getValue();

            double similarity = (matches * 100.0) / newDocNgrams.size();

            System.out.println("Found " + matches + " matching n-grams with \"" + docId + "\"");
            System.out.println("Similarity: " + String.format("%.2f", similarity) + "%");

            if (similarity > 60) {
                System.out.println("⚠ PLAGIARISM DETECTED");
            } else if (similarity > 10) {
                System.out.println("⚠ Suspicious similarity");
            }

            System.out.println();
        }
    }
}

public class PlagiarismSystem {

    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        String essay1 = "machine learning is a method of data analysis that automates analytical model building";
        String essay2 = "machine learning is a method of data analysis that automates model building using algorithms";

        detector.addDocument("essay_089.txt", essay1);
        detector.addDocument("essay_092.txt", essay2);

        String newEssay = "machine learning is a method of data analysis that automates analytical model building";

        detector.analyzeDocument("essay_123.txt", newEssay);
    }
}