import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MerkleTree {
    private Set<String> leaves;
    private String root;

    public MerkleTree() {
        leaves = new LinkedHashSet<>();
        root = null;
    }

    public void addLeaf(String data) {
        String hashedData = hash(data); // Hash the command before storing
        if (leaves.add(hashedData)) { // Only add unique hashes
            root = calculateRoot(); // Recalculate the root if a new hash is added
        }
    }

    private String calculateRoot() {
        List<String> currentLevel = new ArrayList<>(leaves);

        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();
            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = i + 1 < currentLevel.size() ? currentLevel.get(i + 1) : left;
                nextLevel.add(hash(left + right));
            }
            currentLevel = nextLevel;
        }

        return currentLevel.isEmpty() ? null : currentLevel.get(0);
    }
//Long Hash code
//    private String hash(String data) {
//        try {
//            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
//            byte[] hashBytes = md.digest(data.getBytes());
//            StringBuilder sb = new StringBuilder();
//            for (byte b : hashBytes) {
//                sb.append(String.format("%02x", b));
//            }
//            return sb.toString();
//        } catch (Exception e) {
//            throw new RuntimeException("Error hashing data", e);
//        }
//    }

//    2 letter hash
private String hash(String data) {
    try {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(data.getBytes());
        StringBuilder sb = new StringBuilder();

        // Take the first 2 bytes of the hash and convert to a readable string
        for (int i = 0; i < 2; i++) {
            sb.append(String.format("%02x", hashBytes[i]));
        }
        return sb.toString();
    } catch (Exception e) {
        throw new RuntimeException("Error hashing data", e);
    }
}
    public String getRoot() {
        return root;
    }
    public List<String> getLeaves() {
        return new ArrayList<>(leaves); // Return a copy to avoid modification of internal state
    }

    public List<String> getProof(String data) {
        // Implement proof generation logic
        return new ArrayList<>();
    }

//    public String visualizeTree() {
//        List<String> currentLevel = new ArrayList<>(leaves);
//        StringBuilder visualization = new StringBuilder();
//
//        while (!currentLevel.isEmpty()) {
//            visualization.append(String.join(" | ", currentLevel)).append("\n");
//            List<String> nextLevel = new ArrayList<>();
//
//            for (int i = 0; i < currentLevel.size(); i += 2) {
//                String left = currentLevel.get(i);
//                String right = i + 1 < currentLevel.size() ? currentLevel.get(i + 1) : left;
//                nextLevel.add(hash(left + right));
//            }
//
//            currentLevel = nextLevel;
//        }
//
//        return visualization.toString();
//    }

//    public String visualizeTree() {
//        List<String> currentLevel = new ArrayList<>(leaves);
//        StringBuilder visualization = new StringBuilder();
//
//        while (!currentLevel.isEmpty()) {
//            // Limit visualization size for extremely large trees
//            if (visualization.length() > 10_000) {
//                visualization.append("... (tree too large to display fully)").append("\n");
//                break;
//            }
//
//            // Append the current level to visualization
//            visualization.append(String.join(" | ", currentLevel)).append("\n");
//
//            List<String> nextLevel = new ArrayList<>();
//            for (int i = 0; i < currentLevel.size(); i += 2) {
//                String left = currentLevel.get(i);
//                String right = i + 1 < currentLevel.size() ? currentLevel.get(i + 1) : left;
//                nextLevel.add(hash(left + right));
//            }
//
//            currentLevel = nextLevel;
//        }
//
//        return visualization.toString();
//    }

//    public void visualizeTreeAsImage(List<String> leaves, String fileName) throws IOException {
//        int width = 800;
//        int height = 600;
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g = image.createGraphics();
//
//        // Background
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, width, height);
//
//        // Set font and color
//        g.setFont(new Font("Arial", Font.PLAIN, 12));
//        g.setColor(Color.BLACK);
//
//        // Draw the tree
//        drawTree(g, leaves, 0, 0, width, height / (int) Math.ceil(Math.log(leaves.size()) / Math.log(2)));
//
//        // Dispose and save
//        g.dispose();
//        ImageIO.write(image, "png", new File(fileName));
//    }
//


//    BottomUp Approach Tree
//    public void visualizeTreeAsImage(List<String> leaves, String fileName) throws IOException {
//        int nodeWidth = 50;
//        int nodeHeight = 20;
//        int horizontalSpacing = 20;
//        int verticalSpacing = 50;
//        int treeHeight = (int) Math.ceil(Math.log(leaves.size()) / Math.log(2)) + 1;
//
//        int canvasWidth = Math.max((nodeWidth + horizontalSpacing) * leaves.size(), 800);
//        int canvasHeight = (verticalSpacing + nodeHeight) * treeHeight;
//
//        BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g = image.createGraphics();
//
//        // Background
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, canvasWidth, canvasHeight);
//
//        // Font settings
//        g.setFont(new Font("Arial", Font.PLAIN, 12));
//        g.setColor(Color.BLACK);
//
//        // Compute tree structure
//        List<List<String>> treeLevels = computeTreeLevels(leaves);
//
//        // Draw nodes
//        for (int level = 0; level < treeLevels.size(); level++) {
//            List<String> currentLevel = treeLevels.get(level);
//            int y = verticalSpacing + level * (nodeHeight + verticalSpacing);
//            int nodesAtLevel = currentLevel.size();
//            int xSpacing = canvasWidth / (nodesAtLevel + 1);
//
//            for (int i = 0; i < nodesAtLevel; i++) {
//                int x = xSpacing * (i + 1);
//                String value = currentLevel.get(i);
//
//                // Draw the node
//                g.drawRect(x - nodeWidth / 2, y - nodeHeight / 2, nodeWidth, nodeHeight);
//                g.drawString(value, x - nodeWidth / 2 + 5, y);
//
//                // Draw connecting lines to children
//                if (level < treeLevels.size() - 1) {
//                    int childIndexLeft = i * 2;
//                    int childIndexRight = i * 2 + 1;
//
//                    int childY = verticalSpacing + (level + 1) * (nodeHeight + verticalSpacing);
//                    if (childIndexLeft < treeLevels.get(level + 1).size()) {
//                        int childXLeft = xSpacing * (childIndexLeft + 1);
//                        g.drawLine(x, y + nodeHeight / 2, childXLeft, childY - nodeHeight / 2);
//                    }
//                    if (childIndexRight < treeLevels.get(level + 1).size()) {
//                        int childXRight = xSpacing * (childIndexRight + 1);
//                        g.drawLine(x, y + nodeHeight / 2, childXRight, childY - nodeHeight / 2);
//                    }
//                }
//            }
//        }
//
//        g.dispose();
//        ImageIO.write(image, "png", new File(fileName));
//    }


//    Top down approach tree
public void visualizeTreeAsImage(List<String> leaves, String fileName) throws IOException {
    int nodeWidth = 50;
    int nodeHeight = 20;
    int horizontalSpacing = 20;
    int verticalSpacing = 50;
    int treeHeight = (int) Math.ceil(Math.log(leaves.size()) / Math.log(2)) + 1;

    int canvasWidth = Math.max((nodeWidth + horizontalSpacing) * leaves.size(), 800);
    int canvasHeight = (verticalSpacing + nodeHeight) * treeHeight;

    BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();

    // Background
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, canvasWidth, canvasHeight);

    // Font settings
    g.setFont(new Font("Arial", Font.PLAIN, 12));
    g.setColor(Color.BLACK);

    // Compute tree structure
    List<List<String>> treeLevels = computeTreeLevels(leaves);

    // Reverse the order of levels so the root is drawn at the top
    for (int level = 0; level < treeLevels.size(); level++) {
        List<String> currentLevel = treeLevels.get(treeLevels.size() - 1 - level); // Reverse level order
        int y = verticalSpacing + level * (nodeHeight + verticalSpacing);
        int nodesAtLevel = currentLevel.size();
        int xSpacing = canvasWidth / (nodesAtLevel + 1);

        for (int i = 0; i < nodesAtLevel; i++) {
            int x = xSpacing * (i + 1);
            String value = currentLevel.get(i);

            // Draw the node
            g.drawRect(x - nodeWidth / 2, y - nodeHeight / 2, nodeWidth, nodeHeight);
            g.drawString(value, x - nodeWidth / 2 + 5, y);

            // Draw connecting lines to children
//            if (level < treeLevels.size() - 1) {
//                int parentIndex = i / 2;
//                int parentXSpacing = canvasWidth / (treeLevels.get(treeLevels.size() - 1 - (level + 1)).size() + 1);
//                int parentX = parentXSpacing * (parentIndex + 1);
//                int parentY = y - (nodeHeight + verticalSpacing);
//                g.drawLine(x, y - nodeHeight / 2, parentX, parentY + nodeHeight / 2);
//            }
        }
    }

    g.dispose();
    ImageIO.write(image, "png", new File(fileName));
}

    private List<List<String>> computeTreeLevels(List<String> leaves) {
        List<List<String>> levels = new ArrayList<>();
        levels.add(new ArrayList<>(leaves));

        List<String> currentLevel = leaves;
        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();
            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = i + 1 < currentLevel.size() ? currentLevel.get(i + 1) : left;
                nextLevel.add(hash(left + right));
            }
            levels.add(nextLevel);
            currentLevel = nextLevel;
        }

        return levels;
    }

    private void drawTree(Graphics2D g, List<String> leaves, int x, int y, int width, int levelHeight) {
        if (leaves.isEmpty()) return;

        List<String> currentLevel = new ArrayList<>(leaves);
        int yOffset = y;

        while (!currentLevel.isEmpty()) {
            int nodeCount = currentLevel.size();
            int nodeWidth = width / (nodeCount + 1);
            int xOffset = x + nodeWidth / 2;

            for (int i = 0; i < nodeCount; i++) {
                String node = currentLevel.get(i);
                g.drawString(node, xOffset + (i * nodeWidth), yOffset);
                if (i > 0) { // Draw connecting lines
                    g.drawLine(
                            xOffset + ((i - 1) * nodeWidth), yOffset - levelHeight,
                            xOffset + (i * nodeWidth), yOffset
                    );
                }
            }

            yOffset += levelHeight;

            // Calculate the next level of nodes
            List<String> nextLevel = new ArrayList<>();
            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = i + 1 < currentLevel.size() ? currentLevel.get(i + 1) : left;
                nextLevel.add(hash(left + right));
            }
            currentLevel = nextLevel;
        }
    }




}
