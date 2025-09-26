import java.io.File;

public class GitTester {
    public static void main(String[] args) {
        System.out.println(repositoryCheck());
    }

    public static boolean repositoryCheck() {
        File git = new File("git");
        File objects = new File(git, "objects");
        File index = new File(git, "index");
        File head = new File(git, "HEAD");
        if (git.isDirectory() && objects.isDirectory() && index.isDirectory()
                && head.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

    public static void cleanup() {
        File git = new File("git");
        File objects = new File(git, "objects");
        File index = new File(git, "index");
        File head = new File(git, "HEAD");
        File[] files = objects.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        head.delete();
        index.delete();
        objects.delete();
        git.delete();
        System.out.println("Cleanup occurred");
    }


    public static void repositoryCycles() {
        for (int i = 0; i < 10; i++) {
            Git.createRepository();
            cleanup();
        }
        System.out.println("You're awesome. This code works, you genius.");

    }
}
