import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
}
