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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Git {
    public static void main(String[] args) throws FileNotFoundException {

    }

    static HashMap<String, String> hash = new HashMap<>();

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
            System.out.println("Git Repository Created");
        }
    }

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

    public static String contents(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder s = new StringBuilder();
        while (reader.ready()) {
            s.append((char) reader.read());
        }
        reader.close();
        return s.toString();
    }

    public static void createBlob(String filePath) throws FileNotFoundException {
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

    public static void updateIndex(String filePath) throws IOException {
        // byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        // String data = new String(bytes, StandardCharsets.UTF_8);
        // String name = hashFunction(data);
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

    public static String getFilePath(File file) {
        return file.getAbsolutePath().substring(file.getAbsolutePath().indexOf("git-project"));
    }

    public static String createTree(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new IOException("directory path does not lead to a directory");
        }
        File[] children = directory.listFiles();
        List<String> entries = new ArrayList<>();
        if (children != null && children.length > 0) {

            for (File child : children) {
                if (child.isFile()) {
                    createBlob(child.getPath());
                    byte[] bytes = Files.readAllBytes(Paths.get(child.getPath()));
                    String name = new String(bytes, StandardCharsets.UTF_8);
                    String sha = hashFunction(name);
                    if (sha != null) {
                        entries.add("blob " + sha + " " + child.getName());
                    }
                } else {
                    String sha = createTree(child.getPath()); // directoryPath + "/" + child
                    entries.add("tree " + sha + " " + child.getName());

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
        while (Files.readAllLines(Paths.get(workingList.getPath())).size() > 1) {
            createIndexTreeHelper();
        }
        List<String> root = Files.readAllLines(workingList.toPath(), StandardCharsets.UTF_8);
        if (root.size() == 1) {
            String[] partitions = root.get(0).split(" ", 3);
            if (!partitions[0].equals("tree")) {
                throw new IOException("Splitting did not work");
            }
            return partitions[1];
        }
        return null;


    }

    private static void createIndexTreeHelper() throws IOException {
        File workingList = new File("git/objects/workingList");
        List<String> entries = Files.readAllLines(workingList.toPath(), StandardCharsets.UTF_8);
        if (entries.isEmpty()) {
            return;
        }
        String deepestLine = entries.get(0);
        String[] parts = deepestLine.split(" ", 3);
        String type = parts[0];
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
}