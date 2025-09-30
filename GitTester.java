import java.io.File;
import java.io.FileNotFoundException;

public class GitTester {
    public static void main(String[] args) throws FileNotFoundException {
        Git.createRepository();
        reset();
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

    public static boolean blobTest() {
        File objects = new File("git/objects/");
        if (!objects.exists() || objects.listFiles().length == 0 || objects.listFiles() == null) {
            return false;
        } else {
            File[] files = objects.listFiles();
            System.out.println("Objects contains: ");
            for (File file : files)
                System.out.println(file.getName() + " ");

            return true;
        }

    }

    public static void reset() {
        File git = new File("git");
        deleteGit(git);
        Git.createRepository();
        System.out.println("Reset successful");


    }

    public static void deleteGit(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children.length > 0 && children != null) {
                for (File child : children) {
                    deleteGit(child);
                }
            }
        }
        file.delete();
    }
}
