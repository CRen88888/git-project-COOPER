import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Git {
    public static void main(String[] args) {
        GitTester.repositoryCycles();

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
        }


        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
