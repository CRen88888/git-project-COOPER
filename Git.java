import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
// import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Git {
    public static void main(String[] args) throws FileNotFoundException {

    }

    static HashMap<String, String> hash = new HashMap<>();

    /**
     * Initializes a Git repository by creating the necessary directory structure and files. Creates
     * the git directory, objects subdirectory, index file, and HEAD file if they don't exist. If
     * the repository already exists, it prints a message indicating so.
     */
    public static void createRepository() {
        File git = new File("git");
        File objects = new File(git, "objects");
        File index = new File(git, "index");
        File head = new File(git, "HEAD");
        if (git.exists() && objects.exists() && index.exists() && head.exists()) {
            System.out.println("Git Repository Already Exists");
        } else {
            if (!git.exists()) {
                git.mkdir();
            }
            if (!objects.exists()) {
                objects.mkdir();
            }
            try {
                if (!index.exists()) {
                    index.createNewFile();
                }
                if (!head.exists()) {
                    head.createNewFile();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            // System.out.println("Git Repository Created");
        }
    }

    /**
     * Generates a SHA-1 hash for the given input string. Uses Java's MessageDigest to create a
     * 40-character hexadecimal hash.
     * 
     * @param input the string to hash
     * @return a 40-character SHA-1 hash string
     */
    public static String hashFunction(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 40) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads and returns the complete contents of a file as a string. Uses BufferedReader to read
     * character by character from the file.
     * 
     * @param file the file to read from
     * @return the complete file contents as a string
     * @throws IOException if there's an error reading the file
     */
    public static String contents(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder s = new StringBuilder();
        while (reader.ready()) {
            s.append((char) reader.read());
        }
        reader.close();
        return s.toString();
    }

    /**
     * Creates a blob object from a file and stores it in the git objects directory. The blob is
     * named using the SHA-1 hash of its contents. Also updates the index file to track the blob.
     * 
     * @param filePath the path to the file to create a blob from
     * @throws IOException if there's an error reading the file or creating the blob
     */
    public static void createBlob(String filePath) throws IOException {
        if (filePath == null) {
            return;
        }
        try {
            File index = new File("git/index");
            File test = new File(filePath);
            if (!test.exists() || !test.isFile()) {
                throw new FileNotFoundException("File not found: " + filePath);
            }
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            String data = new String(bytes, StandardCharsets.UTF_8);
            String name = hashFunction(data);
            File file = new File("git/objects/" + name);
            if (contents(index).contains(name + " " + getFilePath(file))) {
                return;
            }
            StringBuilder s = new StringBuilder();
            file.createNewFile();
            hash.put(getFilePath(new File(filePath)), name);
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            while (br.ready()) {
                s.append((br.readLine()));
            }
            br.close();
            BufferedWriter wr = new BufferedWriter(new FileWriter("git/objects/" + name));
            wr.write(s.toString());
            wr.close();
            updateIndex(filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Updates the git index file with all tracked files and their corresponding blob hashes.
     * Recreates the index file with entries in the format: "hash filepath". Uses the global hash
     * map to track file paths and their blob hashes.
     * 
     * @param filePath the file path to update in the index (parameter used for consistency)
     * @throws IOException if there's an error writing to the index file
     */
    public static void updateIndex(String filePath) throws IOException {
        File index = new File("git/index");
        index.delete();
        index.createNewFile();
        int counter = 0;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(index, true));
            for (Map.Entry<String, String> entry : hash.entrySet()) {
                if (counter != 0) {
                    bw.write('\n');
                }
                String key = entry.getKey();
                String value = entry.getValue();
                bw.write(value + " " + key);
                counter++;
            }

            bw.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Extracts the relative file path from an absolute file path. Returns the portion of the path
     * starting from "git-project" to make paths relative.
     * 
     * @param file the file object to extract the relative path from
     * @return the relative file path starting from "git-project"
     */
    public static String getFilePath(File file) {
        return file.getAbsolutePath().substring(file.getAbsolutePath().indexOf("git-project"));
    }

    /**
     * Creates a tree object from a directory structure. Recursively processes files and
     * subdirectories, creating blob objects for files and tree objects for subdirectories. Stores
     * everything in the git objects directory.
     * 
     * @param directoryPath the path to the directory to create a tree from
     * @return the SHA-1 hash of the created tree object
     * @throws IOException if there's an error reading the directory or creating objects
     */
    public static String createTree(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new IOException("directory path does not lead to a directory");
        }
        BufferedReader br = new BufferedReader(new FileReader("git/index"));
        StringBuilder s = new StringBuilder();
        while (br.ready()) {
            s.append(br.readLine());
        }
        br.close();
        String index = s.toString();
        File[] children = directory.listFiles();
        List<String> entries = new ArrayList<>();
        if (children != null && children.length > 0) {
            for (File child : children) {
                if (index.contains(getFilePath(child))) {
                    if (child.isFile()) {
                        createBlob(child.getPath());
                        byte[] bytes = Files.readAllBytes(Paths.get(child.getPath()));
                        String name = new String(bytes, StandardCharsets.UTF_8);
                        String sha = hashFunction(name);
                        if (sha != null) {
                            entries.add("blob " + sha + " " + child.getName());
                        }
                        // }
                    } else {
                        String sha = createTree(child.getPath());
                        entries.add("tree " + sha + " " + child.getName());

                    }
                } else {
                    continue;
                }



            }

        }
        String treeInfo = String.join("\n", entries);
        String treeHash = hashFunction(treeInfo);
        Path tree = Paths.get("git", "objects", treeHash);
        if (!Files.exists(tree)) {
            Files.write(tree, treeInfo.getBytes(StandardCharsets.UTF_8));

        }
        return treeHash;
    }

    /**
     * Counts the number of forward slashes in the file path portion of an index entry. Used for
     * sorting entries by directory depth (deeper paths first).
     * 
     * @param line an index entry in the format "type hash path"
     * @return the number of slashes in the path portion
     */
    public static int slashCount(String line) {
        String[] parts = line.split(" ", 3);
        String path = parts[2];
        int count = 0;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                count++;
            }
        }
        return count;
    }

    /**
     * Sorts a list of index entries by directory depth (descending) and alphabetically. Entries
     * with more slashes (deeper paths) come first. For entries with the same depth, sorts
     * alphabetically by path.
     * 
     * @param lines the list of index entries to sort
     */
    public static void sortSlashCountDescend(List<String> lines) {
        for (int i = 0; i < lines.size() - 1; i++) {
            for (int j = 0; j < lines.size() - i - 1; j++) {
                String first = lines.get(j);
                String second = lines.get(j + 1);
                int firstSlash = slashCount(first);
                int secondSlash = slashCount(second);
                if (secondSlash > firstSlash || (secondSlash == firstSlash
                        && pathLine(second).compareTo(pathLine(first)) < 0)) {
                    String temp = lines.get(j);
                    lines.set(j, lines.get(j + 1));
                    lines.set(j + 1, temp);
                }
            }
        }
    }

    /**
     * Extracts the file path portion from an index entry line. Parses a line in format "type hash
     * path" and returns just the path portion.
     * 
     * @param line the index entry line to parse
     * @return the file path portion of the line
     */
    private static String pathLine(String line) {
        if (line == null)
            return "";
        int space = 0;
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ') {
                space++;
            }
            if (space >= 2) {
                s.append(c);
            }
        }
        return s.toString();
    }


    /**
     * Creates a tree structure from the git index file. Processes all indexed files and creates a
     * hierarchical tree structure. Uses a working list to track progress and recursively builds
     * trees from deepest to shallowest.
     * 
     * @return the SHA-1 hash of the root tree object
     * @throws IOException if there's an error reading the index or creating tree objects
     */
    public static String createIndexTree() throws IOException {
        File index = new File("git/index");
        if (!index.exists()) {
            throw new IOException("index file not found");
        }
        File workingList = new File("git/objects/workingList");
        if (!workingList.exists()) {
            workingList.createNewFile();
        }
        ArrayList<String> newLines = new ArrayList<>();
        for (String line : Files.readAllLines(index.toPath(), StandardCharsets.UTF_8)) {
            String[] parts = line.split(" ");
            String hash = parts[0];
            String filePath = parts[1];
            newLines.add("blob " + hash + " " + filePath);
        }

        sortSlashCountDescend(newLines);
        Files.write(workingList.toPath(), newLines, StandardCharsets.UTF_8);
        if (Files.readAllLines(Paths.get(workingList.getPath())).size() == 1) {

        }
        while (Files.readAllLines(Paths.get(workingList.getPath())).size() > 1) {
            createIndexTreeHelper();
        }
        List<String> root = Files.readAllLines(workingList.toPath(), StandardCharsets.UTF_8);
        if (root.size() == 1) {
            String[] partitions = root.get(0).split(" ", 3);
            if (partitions[0].equals("blob")) {
                String treeHash = hashFunction(root.get(0));
                workingList.delete();
                workingList.createNewFile();
                String treePath = partitions[2].substring(0, partitions[2].lastIndexOf("/"));
                ArrayList<String> treeLine = new ArrayList<>();
                treeLine.add("tree " + treeHash + " " + treePath);
                Files.write(workingList.toPath(), treeLine, StandardCharsets.UTF_8);
                return treeHash;
            }
            if (!partitions[0].equals("tree")) {
                throw new IOException("Splitting did not work");
            }
            return partitions[1];
        }
        return null;


    }

    /**
     * Helper method for createIndexTree that processes the deepest entries in the working list.
     * Takes the deepest file path, creates a tree for its parent directory, and updates the working
     * list with the new tree entry.
     * 
     * @throws IOException if there's an error processing entries or creating tree objects
     */
    private static void createIndexTreeHelper() throws IOException {
        File workingList = new File("git/objects/workingList");
        List<String> entries = Files.readAllLines(workingList.toPath(), StandardCharsets.UTF_8);
        if (entries.isEmpty()) {
            return;
        }
        String deepestLine = entries.get(0);
        String[] parts = deepestLine.split(" ", 3);
        File deepestFile = new File(parts[2]);
        File targetDirectory = deepestFile.getParentFile();
        targetDirectory = new File(
                targetDirectory.getPath().substring(targetDirectory.getPath().indexOf("/") + 1));
        if (!targetDirectory.exists()) {
            targetDirectory.mkdir();
        }
        if (targetDirectory == null || !targetDirectory.isDirectory()) {
            throw new IOException("Unresolvable path: " + parts[2]);
        }
        String treeHash = createTree(targetDirectory.getPath());
        workingList.delete();
        workingList.createNewFile();
        ArrayList<String> keep = new ArrayList<>();

        for (String entry : entries) {
            String[] splits = entry.split(" ", 3);
            File file = new File(splits[2].substring(splits[2].indexOf("/") + 1));
            if (file.getParentFile() == null || !file.getParentFile().equals(targetDirectory)) {
                keep.add(entry);
            }
        }
        if (!keep.contains("tree " + treeHash + " " + getFilePath(targetDirectory))) {
            keep.add("tree " + treeHash + " " + getFilePath(targetDirectory));
        }
        sortSlashCountDescend(keep);
        Files.write(Paths.get("git/objects/workingList"), keep, StandardCharsets.UTF_8);
    }

    /**
     * Validates that the git repository structure was created correctly. Checks that all referenced
     * objects in tree files actually exist in the objects directory.
     * 
     * @return true if the repository structure is valid, false otherwise
     */
    public static boolean isCreatedCorrectly() {
        File objects = new File("git/objects");
        File[] things = objects.listFiles();
        try {
            for (File file : things) {
                if (new String(Files.readAllBytes(Paths.get("git/objects/" + file.getName())))
                        .contains("(root)")) {
                    return isCreatedCorrectlyHelper("git/objects/" + file.getName());
                }
            }
            return isCreatedCorrectlyHelper(null);
        } catch (Exception e) {
            // TODO: handle exception
            System.err.println(e);
            // System.out.println("sigmanite");
            return false;
        }
    }

    /**
     * Creates a SHA-1 hash of a file's contents. Handles BOM (Byte Order Mark) removal and line
     * ending normalization.
     * 
     * @param filePath the path to the file to hash
     * @return the SHA-1 hash of the file contents, or null if file doesn't exist
     */
    public static String hashFile(String filePath) {
        File testFile = new File(filePath);
        if (testFile.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                String content = new String(bytes, StandardCharsets.UTF_8);
                if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
                    content = content.substring(1);
                }
                content = content.replace("\r\n", "\n").replace("\r", "\n");
                byte[] normalizedBytes = content.getBytes(StandardCharsets.UTF_8);
                MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
                byte[] hashedBytes = mDigest.digest(normalizedBytes);
                StringBuilder hashedString = new StringBuilder();
                for (byte b : hashedBytes) {
                    String hexB = Integer.toHexString(0xff & b);
                    if (hexB.length() == 1) {
                        hashedString.append("0");
                    }
                    hashedString.append(hexB);
                }
                return hashedString.toString();
            } catch (IOException | NoSuchAlgorithmException e) {
                System.err.println(e);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Recursive helper method for isCreatedCorrectly that validates tree objects. Checks that all
     * blob and tree references in a tree file point to existing objects.
     * 
     * @param path the path to the tree file to validate
     * @return true if all referenced objects exist, false otherwise
     */
    private static boolean isCreatedCorrectlyHelper(String path) {
        try {
            ArrayList<String> lines = new ArrayList<String>(Files.readAllLines(Paths.get(path)));
            for (String line : lines) {
                if (line.contains("tree")) {
                    if (!isCreatedCorrectlyHelper("git/objects/" + line.substring(5, 45))) {
                        // System.out.println(line);
                        // System.out.println(line.substring(5, 45));
                        // System.out.println("sigmaniter");
                        return false;
                    }
                } else {
                    if (!Files.exists(Paths.get("git/objects/" + line.substring(5, 45)))) {
                        // System.out.println(line);
                        // System.out.println(line.substring(5, 45));
                        // System.out.println("sigmanites");
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // System.out.println("sigmaniting");
            return false;
        }
        return true;
    }

    /**
     * Creates a commit object with the specified author and message. Includes the current tree
     * hash, parent commit (if any), timestamp, and metadata. Updates the HEAD file to point to the
     * new commit.
     * 
     * @param author the name of the commit author
     * @param message the commit message
     */
    public static void commit(String author, String message) {
        // Scanner scanner = new Scanner(System.in);
        // System.out.print("Enter name of commit author: ");
        // String author = scanner.nextLine();
        // System.out.print("Enter commmit message: ");
        // String message = scanner.nextLine();
        // scanner.close();
        try {
            String rootTree = createIndexTree();
            String parent = new String(Files.readAllBytes(Paths.get("git/HEAD")));
            String timestamp = "" + System.currentTimeMillis();
            StringBuilder str = new StringBuilder();
            str.append("tree: ");
            str.append(rootTree);
            if (!parent.equals("")) {
                str.append("\nparent: ");
                str.append(parent);
            }
            str.append("\nauthor: ");
            str.append(author);
            str.append("\ndate: ");
            str.append(timestamp);
            str.append("\nmessage: ");
            str.append(message);
            String commit = str.toString();
            String commitHash = hashFunction(commit);
            File commitFile = new File("git/objects/" + commitHash);
            commitFile.createNewFile();
            Files.write(Paths.get("git/objects/" + commitHash),
                    commit.getBytes(StandardCharsets.UTF_8));
            Files.write(Paths.get("git/HEAD"), commitHash.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // TODO: handle exception
            System.err.println(e);
        }
    }
}
