import java.io.File;

public class Git {
    public static void main(String[] args) {
        createRepository();

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
}
