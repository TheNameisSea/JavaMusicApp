import javax.swing.plaf.synth.SynthTextAreaUI;
import java.util.*;
import java.io.File;


class SongMatch implements Comparable<SongMatch> {
    Song song;
    int score;

    public SongMatch(Song song, int score) {
        this.song = song;
        this.score = score;
    }

    @Override
    public int compareTo(SongMatch other) {
        return Integer.compare(this.score, other.score);
    }
}

class ClosestTracker {
    Node pred = null;
    Node succ = null;
}

class Node {
    Song key;
    File path;
    int height;
    Node left, right;

    Node(Song key) {
        this.key = key;
        this.height = 1;
    }
}

public class SongTree implements Comparator<Song> {

    Node root;

    // Get the height of the node
    int height(Node node) {
        if (node == null)
            return 0;
        return node.height;
    }

    // Get maximum of two integers
    int max(int a, int b) {
        return (a > b) ? a : b;
    }

    // Get min of two integers
    int min(int a, int b) {
        return (a < b) ? a : b;
    }

    @Override
    public int compare(Song s1, Song s2) {
        return normalize(s1.getSongTitle()).compareTo(normalize(s2.getSongTitle()));
    }


    public int compare(String s1, Song s2) {
        String toLower = s1.toLowerCase();
        if (toLower.contains("the ")
                || toLower.contains("a ")
                || toLower.contains("an ")) {
            return toLower.compareTo(s2.getSongTitle().toLowerCase().trim());
        }
        return normalize(s1).compareTo(normalize(s2.getSongTitle()));
    }

    // Ignore capitalization and articles like "The", "A", or "An" when sorting
    private String normalize(String title) {
        title = title.toLowerCase().trim();
        if (title.startsWith("the ")) {
            return title.substring(4);
        } else if (title.startsWith("a ")) {
            return title.substring(2);
        } else if (title.startsWith("an ")) {
            return title.substring(3);
        }
        return title;
    }

    // Right rotate subtree rooted with node
    Node rightRotate(Node node) {
        Node leftChild = node.left;
        Node temp = leftChild.right;

        // Perform rotation
        leftChild.right = node;
        node.left = temp;

        // Update heights
        node.height = max(height(node.left), height(node.right)) + 1;
        leftChild.height = max(height(leftChild.left), height(leftChild.right)) + 1;

        // Return new root
        return leftChild;
    }

    // Left rotate subtree rooted with node
    Node leftRotate(Node node) {
        Node rightChild = node.right;
        Node temp = rightChild.left;

        // Perform rotation
        rightChild.left = node;
        node.right = temp;

        // Update heights
        node.height = max(height(node.left), height(node.right)) + 1;
        rightChild.height = max(height(rightChild.left), height(rightChild.right)) + 1;

        // Return new root
        return rightChild;
    }

    // Get balance factor of node
    int getBalance(Node node) {
        if (node == null)
            return 0;
        return height(node.left) - height(node.right);
    }

    // Insert a key into the AVL tree and return the new root of the subtree
    Node insert(Node root, Song key) {
        if (root == null)
            return new Node(key);


        if (compare(key, root.key) < 0)
            root.left = insert(root.left, key);
        else if (compare(key, root.key) > 0)
            root.right = insert(root.right, key);
        else
            return root;

        // Update height of root
        root.height = 1 + max(height(root.left), height(root.right));

        // Get balance factor
        int balance = getBalance(root);

        // Left Left Case
        if (balance > 1 && compare(key, root.left.key) < 0)
            return rightRotate(root);

        // Right Right Case
        if (balance < -1 && compare(key, root.right.key) > 0)
            return leftRotate(root);

        // Left Right Case
        if (balance > 1 && compare(key, root.left.key) > 0) {
            root.left = leftRotate(root.left);
            return rightRotate(root);
        }

        // Right Left Case
        if (balance < -1 && compare(key, root.right.key) < 0) {
            root.right = rightRotate(root.right);
            return leftRotate(root);
        }

        return root;
    }

    public void insert(Song key){
        root = insert(root, key);
    }

    // Find the node with the minimum key in a subtree
    Node minValueNode(Node node) {
        Node current = node;
        while (current.left != null)
            current = current.left;
        return current;
    }

    // Find the node with the minimum key in a subtree
    Node maxValueNode(Node node) {
        Node current = node;
        while (current.right != null)
            current = current.right;
        return current;
    }

    // Delete a key from the AVL tree and return the new root of the subtree
    Node delete(Node root, Song key) {
        if (root == null)
            return root;
        // Standard BST delete
        if (compare(key, root.key) < 0)
            root.left = delete(root.left, key);
        else if (compare(key, root.key) > 0)
            root.right = delete(root.right, key);
        else {
            // Node with only one child or no child
            if (root.left == null)
                return root.right;
            else if (root.right == null)
                return root.left;

            // Node with two children
            Node temp = minValueNode(root.right);
            root.key = temp.key;
            root.right = delete(root.right, temp.key);
        }
        // Update height of the current node
        root.height = 1 + max(height(root.left), height(root.right));
        // Get balance factor
        int balance = getBalance(root);

        // Left Left Case
        if (balance > 1 && getBalance(root.left) >= 0)
            return rightRotate(root);

        // Left Right Case
        if (balance > 1 && getBalance(root.left) < 0) {
            root.left = leftRotate(root.left);
            return rightRotate(root);
        }

        // Right Right Case
        if (balance < -1 && getBalance(root.right) <= 0)
            return leftRotate(root);

        // Right Left Case
        if (balance < -1 && getBalance(root.right) > 0) {
            root.right = rightRotate(root.right);
            return leftRotate(root);
        }
        return root;
    }

    public void delete(Song key) {
        root = delete(root, key);
    }

    // Search for song title
    Node search(Node root, String key){
        if (root == null || compare(key, root.key) == 0){
            return root;
        }

        if (compare(key, root.key) < 0){
            return search(root.left, key);
        }

        return search(root.right, key);
    }

    public void search(String key){
        root = search(root, key);
        if (root == null){
            System.out.println("No song found");
        }
        else{
            System.out.println("Found " + root.key);
        }
    }

    void findClosest(Node root, String query, ClosestTracker tracker) {
        if (root == null) return;

        if (compare(query, root.key) < 0) {
            tracker.succ = root;
            findClosest(root.left, query, tracker);
        } else if (compare(query, root.key) > 0) {
            tracker.pred = root;
            findClosest(root.right, query, tracker);
        } else {
            tracker.pred = maxValueNode(root.left);
            tracker.succ = minValueNode(root.right);
        }
    }

    // Search the k nearest lexicographical song compared to the query
    public void searchNearestLexico(String query, int k){
        ClosestTracker tracker = new ClosestTracker();
        findClosest(root, query, tracker);

        List<Song> result = new ArrayList<>();
        if (tracker.pred != null) result.add(tracker.pred.key);
        if (tracker.succ != null) result.add(tracker.succ.key);

        Node pred = tracker.pred;
        Node succ = tracker.succ;

        while (result.size() < k && (pred != null || succ != null)) {
            if (pred != null) {
                pred = getPredecessor(root, pred.key.getSongTitle());
                if (pred != null) result.add(pred.key);
            }
            if (result.size() >= k) break;
            if (succ != null) {
                succ = getSuccessor(root, succ.key.getSongTitle());
                if (succ != null) result.add(succ.key);
            }
        }

        System.out.println(result);
    }

    Node getPredecessor(Node root, String key) {
        Node pred = null;
        while (root != null) {
            if (compare(key, root.key) > 0) {
                pred = root;
                root = root.right;
            } else {
                root = root.left;
            }
        }
        return pred;
    }

    Node getSuccessor(Node root, String key) {
        Node succ = null;
        while (root != null) {
            if (compare(key, root.key) < 0) {
                succ = root;
                root = root.left;
            } else {
                root = root.right;
            }
        }
        return succ;
    }

    // Add the song's score to the priority queue for ranking
    private void collectMatches(Node node, String query, PriorityQueue<SongMatch> pq) {
        if (node == null) return;

        collectMatches(node.left, query, pq);

        int score = similarityScore(query, node.key);
        pq.offer(new SongMatch(node.key, score));

        collectMatches(node.right, query, pq);
    }

    // Levenshtein Distance Algorithm to Get String similarity score
    int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else if (a.charAt(i - 1) == b.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1];
                else
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                            Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }

        return dp[a.length()][b.length()];
    }


    // Score the songs based on each word's similarity using Levenshtein Distance
    private int similarityScore(String query, Song song) {
        String toLower = query.toLowerCase().trim();
        String toLowerSong = song.getSongTitle().toLowerCase().trim();

        if (toLowerSong.equals(toLower)) {
            return 0; // Perfect Match
        }

        String[] words = toLowerSong.split(" ");
        String[] wordsQuery = toLower.split(" ");

        int minValue = Integer.MAX_VALUE;

        for (String q : wordsQuery) {
            for (String word : words) {
                if (word.equals(q)) {
                    return 0;
                }
                if (word.contains(q)) {
                    int value = (word.length() - q.length()) / 2;
                    minValue = min(value, minValue);
                }
                minValue = min(minValue, levenshteinDistance(word, q));
            }
        }

        return minValue;
    }

    // Search the k closest song compared to the query
    public void getClosestSongs(String query, int k){
        PriorityQueue<SongMatch> pq = new PriorityQueue<>();
        collectMatches(root, query, pq);

        List<Song> result = new ArrayList<>();
        int count = 0;
        while (!pq.isEmpty() && count < k) {
            result.add(pq.poll().song);
            count++;
        }

        System.out.println(result);
    }



    // Utility functions for traversal
    void preOrder(Node node) {
        if (node != null) {
            System.out.print(node.key + " ");
            preOrder(node.left);
            preOrder(node.right);
        }
    }

    void inOrder(Node node) {
        if (node != null) {
            inOrder(node.left);
            System.out.print(node.key + ", ");
            inOrder(node.right);
        }
    }

    void postOrder(Node node) {
        if (node != null) {
            postOrder(node.left);
            postOrder(node.right);
            System.out.print(node.key + " ");
        }
    }

}


