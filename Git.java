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
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Git {
    public static void main(String[] args) throws FileNotFoundException {

    }

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
            ;
        }
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            String data = new String(bytes, StandardCharsets.UTF_8);
            String name = hashFunction(data);
            File file = new File("git/objects/" + name);
            String s = new String();
            file.createNewFile();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            while (br.ready()) {
                s = s + (br.readLine());
            }
            br.close();
            BufferedWriter wr = new BufferedWriter(new FileWriter("git/objects/" + name));
            wr.write(s);
            wr.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void updateIndex(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        String data = new String(bytes, StandardCharsets.UTF_8);
        String name = hashFunction(data);
        File index = new File("git/index");
        boolean check = index.exists() && index.length() > 0;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(index, true));
            if (check == true) {
                bw.newLine();
            }
            bw.write(name + " " + filePath);
            bw.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static String createTree(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new IOException("directory path does not lead to a directory");
        }
        StringBuilder data = new StringBuilder();
        File[] children = directory.listFiles();
        if (children.length > 0 && children != null) {
            for (File child : children) {
                if (child.isFile()) {
                    createBlob(child.getPath());
                    String sha = hash.get(getFilePath(child.getPath()));
                    if (sha != null) {
                        data.append("blob " + sha + " " + child.getName() + "\n");
                    }
                    // createBlob(Git.getFilePath(directoryPath + "/" + child));
                    // byte[] bytes = Files.readAllBytes(Paths.get(directoryPath + "/" + child));
                    // String data = new String(bytes, StandardCharsets.UTF_8);
                    // String name = hashFunction(data);
                    // BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
                    // bw.write("blob " + name + " " + getFilePath(directoryPath + "/" + child));
                } else {
                    String sha = createTree(child.getPath());
                    data.append("tree " + sha + " " + child.getName() + "\n");

                }

            }

        }
        String treeInfo = data.toString();
        String treeHash = hashFunction(treeInfo);
        File tree = new File("git/objects/" + treeHash);
        if (!tree.exists()) {
            tree.createNewFile();

        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(tree));
        bw.write(treeInfo);
        bw.close();

        return treeHash;
    }


    public static String createIndexTree() throws IOException {
        File index = new File("git/index");
        File workingList = new File("git/workingList");
        ArrayList<String> newLines = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get("git/index"))) {
            String[] parts = line.split(" ");
            String hash = parts[0];
            String filePath = parts[1];
            newLines.add("blob " + hash + " " + filePath);
        }
        Collections.sort(newLines);
        Files.write(Paths.get("git/workingList"), newLines);

        if (!index.exists()) {
            throw new IOException("index file not found");
        }

    }



}
