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

    public static void createBlob(String filePath) throws FileNotFoundException {
        if (filePath == null) {
            return;
        }
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            String data = new String(bytes, StandardCharsets.UTF_8);
            String name = hashFunction(data);
            File file = new File("git/objects/" + name);
            String s = new String();
            file.createNewFile();
            hash.put(getFilePath(new File(filePath)), name);
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            while (br.ready()) {
                s = s + (br.readLine());
            }
            br.close();
            BufferedWriter wr = new BufferedWriter(new FileWriter("git/objects/" + name));
            wr.write(s);
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
            newLines.add("blob " + hash + " " + getFilePath(new File(filePath)));
        }

        // if (!newLines.isEmpty()) {
        // int lastIndex = newLines.size() - 1;
        // String s = newLines.get(lastIndex);
        // if (s.endsWith("\n")) {
        // newLines.set(lastIndex, s.substring(0, s.length() - 1));
        // }
        // }
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
            // String rootHash = partitions[1];
            // Path mainDir = Paths.get("git", rootHash);
            // if (!Files.exists(mainDir)) {
            // Files.write(mainDir, rootHash.getBytes(StandardCharsets.UTF_8));
            // return rootHash;
            // }
            return partitions[1];
        }
        return null;


    }

    private static void createIndexTreeHelper() throws IOException {
        File workingList = new File("git/objects/workingList");
        List<String> lines = Files.readAllLines(workingList.toPath(), StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            return;
        }
        // int maxSlashes = -1;
        String lowestLine = lines.get(0);
        // for (String line : lines) {
        // String[] parts = line.split(" ", 3);
        // String pathString = parts[2];
        // char[] chars = pathString.toCharArray();
        // int counter = 0;
        // for (char c : chars) {
        // if (c == '/') {
        // counter++;
        // }
        // }
        // if (counter > maxSlashes) {
        // maxSlashes = counter;
        // lowestLine = line;
        // }
        // if (lowestLine == null) {
        // return;
        // }
        // }
        String[] parts = lowestLine.split(" ", 3);
        String type = parts[0];
        File lowestFile = new File(parts[2]);
        File targetDirectory = lowestFile.getParentFile();
        if ("blob".equals(type)) {
            targetDirectory = lowestFile.getParentFile();
        } else {
            targetDirectory = lowestFile;
        }
        if (targetDirectory == null || !targetDirectory.isDirectory()) {
            return;
        }
        String treeHash = createTree(targetDirectory.getPath());
        ArrayList<String> keep = new ArrayList<>();
        for (String line : lines) {
            String[] splits = line.split(" ", 3);
            File file = new File(splits[2]);
            if (file.getParentFile() == null || !file.getParentFile().equals(targetDirectory)) {
                keep.add(line);
            }
        }

        keep.add("tree " + treeHash + " " + getFilePath(targetDirectory));
        sortSlashCountDescend(keep);
        Files.write(Paths.get("git/objects/workingList"), keep, StandardCharsets.UTF_8);
        // try {
        // BufferedReader br = new BufferedReader(new FileReader(workingList));
        // String line = "";
        // while (br.ready()) {
        // line = br.readLine();
        // lines.add(line);
        // }
        // br.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        // String[] originalLines = lines.toArray(new String[0]);
        // workingList.delete();
        // workingList.createNewFile();
        // BufferedWriter bw = new BufferedWriter(new FileWriter(workingList, true));
        // for (String line : originalLines) {
        // String[] partitions = line.split(" ");
        // String path = partitions[2];
        // File file = new File(path);
        // if (!file.getParentFile().equals(lowestFile.getParentFile())) {
        // bw.write(line);
        // }
        // }
        // byte[] content = Files.readAllBytes(Paths.get(workingList.getPath()));
        // if (content.length > 0 && content[content.length - 1] == '\n') {
        // byte[] newContent = new byte[content.length - 1];
        // System.arraycopy(content, 0, newContent, 0, content.length - 1);
        // Files.write(Paths.get(workingList.getPath()), newContent);
        // }
        // bw.write("tree " + treeHash + " " + getFilePath(lowestFile.getParentFile().getPath()));
        // ArrayList<String> newLines = new ArrayList<>();
        // for (String line : Files.readAllLines(Paths.get(workingList.getPath()))) {
        // String[] splits = line.split(" ");
        // String form = splits[0];
        // String hash = splits[1];
        // String relPath = splits[2];
        // newLines.add(form + " " + hash + " " + relPath + "\n");
        // }
        // if (!newLines.isEmpty()) {
        // int lastIndex = newLines.size() - 1;
        // String s = newLines.get(lastIndex);
        // if (s.endsWith("\n")) {
        // newLines.set(lastIndex, s.substring(0, s.length() - 1));
        // }
        // }
        // Collections.sort(newLines);
        // bw.close();
    }
}
