import java.util.ArrayList;

// AVL Tree Node
class AVLNode {
    String key;
    AVLNode left, right;
    int height;

    AVLNode(String key) {
        this.key = key;
        height = 1;
    }
}

// AVL Tree implementation
class AVLTree {
    private AVLNode root;

    public void insert(String key) {
        root = insert(root, key);
    }

    private AVLNode insert(AVLNode node, String key) {
        if (node == null) return new AVLNode(key);

        if (key.compareToIgnoreCase(node.key) < 0)
            node.left = insert(node.left, key);
        else if (key.compareToIgnoreCase(node.key) > 0)
            node.right = insert(node.right, key);
        else
            return node;

        node.height = 1 + Math.max(height(node.left), height(node.right));

        int balance = getBalance(node);

        // Rotations
        if (balance > 1 && key.compareToIgnoreCase(node.left.key) < 0)
            return rotateRight(node);

        if (balance < -1 && key.compareToIgnoreCase(node.right.key) > 0)
            return rotateLeft(node);

        if (balance > 1 && key.compareToIgnoreCase(node.left.key) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        if (balance < -1 && key.compareToIgnoreCase(node.right.key) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    public boolean search(String key) {
        return search(root, key);
    }

    private boolean search(AVLNode node, String key) {
        if (node == null) return false;
        if (key.equalsIgnoreCase(node.key)) return true;
        if (key.compareToIgnoreCase(node.key) < 0)
            return search(node.left, key);
        else
            return search(node.right, key);
    }

    private int height(AVLNode node) {
        return node == null ? 0 : node.height;
    }

    private int getBalance(AVLNode node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }

    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }


    public ArrayList<String> searchContains(String keyword) {
        ArrayList<String> result = new ArrayList<>();
        searchContains(root, keyword.toLowerCase(), result);
        return result;
    }

    private void searchContains(AVLNode node, String keyword, ArrayList<String> result) {
        if (node == null) return;

        searchContains(node.left, keyword, result);

        if (node.key.toLowerCase().contains(keyword)) {
            result.add(node.key);
        }

        searchContains(node.right, keyword, result);
    }
}
