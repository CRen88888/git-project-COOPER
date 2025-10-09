
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitTester {


    public static void main(String[] args) throws IOException {
        reset();
        createIndexTreeTest();



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

    public static void createBlobTest() throws IOException {
        File file = new File("sample/file.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        Git.createBlob(file.toPath().toString());

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

    public static void indexTest() {
        try {
            Git.createRepository();
            String[] files = {"test1.txt", "test2.txt", "test3.txt", "Hello.txt", "Hello.txt"};
            Files.writeString(Paths.get("test1.txt"), "I love Joseph Baca");
            Files.writeString(Paths.get("test2.txt"), "I love Darren Yilmaz");
            Files.writeString(Paths.get("test3.txt"), "I love Angus Norden");
            Files.writeString(Paths.get(files[3]), "I am Cooper");
            Files.writeString(Paths.get(files[4]), "I am Joseph");
            String[] content = {"I love Joseph Baca", "I love Darren Yilmaz", "I love Angus Norden",
                    "I am Cooper", "I am Joseph"};
            for (int i = 0; i < files.length; i++) {
                String data = content[i];
                String name = Git.hashFunction(data);
                Git.createBlob(Paths.get(files[i]).toString());
                Files.writeString(Paths.get("git/objects/" + name), data);
                Git.updateIndex(files[i]);
                System.out.println("New BLOB creates");

            }

            System.out.println("Verification:\n");

            String[] indexLines = Files.readAllLines(Paths.get("git/index"), StandardCharsets.UTF_8)
                    .toArray(new String[0]);

            for (String line : indexLines) {
                String[] section = line.split(" ");
                String hash = section[0];
                String name = section[1];
                String[] dirArray = name.split("/");
                String fileName = dirArray[dirArray.length - 1];
                byte[] bytes = Files.readAllBytes(Paths.get(fileName));
                String data = new String(bytes, StandardCharsets.UTF_8);
                String newhash = Git.hashFunction(data);
                if (hash.equals(newhash)) {
                    System.out.println(name + " verified");
                } else {
                    System.out.println(name + " is not the same");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void robustReset() throws IOException {
        File directory = new File(".");
        resetAllFiles(directory);
        File index = new File("git/index");
        index.delete();
        index.createNewFile();

    }

    public static void resetAllFiles(File directory) {
        File[] list = directory.listFiles();
        for (File f : list) {
            if (!f.getName().equals("README.md") && f.getName().charAt(0) != '.'
                    && !f.getName().contains(".java") && list != null && !f.getName().equals("HEAD")
                    && !f.getName().equals("index")) {
                if (f.isDirectory()) {
                    resetAllFiles(f);

                } else {
                    f.delete();
                }


            }
        }
    }

    public static void createIndexTreeTest() throws IOException {
        File index = new File("git/index");
        File sample = new File("sample");
        File dir2 = new File("sample/dir2");
        File a = new File("sample/a.txt");
        File b = new File("sample/dir2/b.txt");
        sample.mkdir();
        dir2.mkdir();
        a.createNewFile();
        b.createNewFile();
        String aContent = "hello\n";
        String bContent = "world\n";
        Files.write(a.toPath(), aContent.getBytes(StandardCharsets.UTF_8));
        Files.write(b.toPath(), bContent.getBytes(StandardCharsets.UTF_8));
        Git.createBlob(a.getPath());
        Git.createBlob(b.getPath());
        String rootTree = Git.createIndexTree();
        System.out.println("Root tree hash: " + rootTree);

    }



}
