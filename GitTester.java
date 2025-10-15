import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
// import java.nio.file.Path;
import java.nio.file.Paths;

public class GitTester {


    public static void main(String[] args) throws IOException {
        GitWrapper finalTester = new GitWrapper();
        finalTester.init();
        finalTester.add("ThisIsItBoys");
        finalTester.commit("JP Baca", "I am so happy that I was able to Git this done on time.");
        // reset();
        // createIndexTreeTest();

        // Uncomment the line below to run all Git class tests
        // runAllTests();
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
        // File index = new File("git/index");
        File sample = new File("sample");
        File dir2 = new File("sample/dir2");
        File a = new File("sample/a.txt");
        File b = new File("sample/dir2/b.txt");
        File c = new File("sample/c.txt");
        sample.mkdir();
        dir2.mkdir();
        a.createNewFile();
        b.createNewFile();
        c.createNewFile();
        String aContent = "hello\n";
        String bContent = "world\n";
        Files.write(a.toPath(), aContent.getBytes(StandardCharsets.UTF_8));
        Files.write(b.toPath(), bContent.getBytes(StandardCharsets.UTF_8));
        Git.createBlob(a.getPath());
        Git.createBlob(b.getPath());
        Git.createBlob(c.getPath());
        String rootTree = Git.createIndexTree();
        System.out.println("Root tree hash: " + rootTree);

    }

    // Test method for createRepository()
    public static void createRepositoryTest() {
        System.out.println("Testing createRepository()...");
        Git.createRepository();
        boolean exists = repositoryCheck();
        System.out.println("Repository created successfully: " + exists);
    }

    // Test method for hashFunction()
    public static void hashFunctionTest() {
        System.out.println("Testing hashFunction()...");
        String testInput = "Hello, World!";
        String hash = Git.hashFunction(testInput);
        System.out.println("Input: " + testInput);
        System.out.println("Hash: " + hash);
        System.out.println("Hash length: " + hash.length());
    }

    // Test method for contents()
    public static void contentsTest() throws IOException {
        System.out.println("Testing contents()...");
        try {
            File testFile = new File("test_contents.txt");
            Files.writeString(testFile.toPath(), "This is a test file content.");
            String content = Git.contents(testFile);
            System.out.println("File content: " + content);
            testFile.delete();
        } catch (IOException e) {
            System.out.println("Error in contents test: " + e.getMessage());
        }
    }

    // Test method for createBlob() - Simple version
    public static void createBlobSimpleTest() throws IOException {
        System.out.println("Testing createBlob()...");
        try {
            File testFile = new File("test_blob.txt");
            Files.writeString(testFile.toPath(), "Blob test content");
            Git.createBlob(testFile.getPath());
            System.out.println("Blob created successfully");
            testFile.delete();
        } catch (IOException e) {
            System.out.println("Error in createBlob test: " + e.getMessage());
        }
    }

    // Test method for updateIndex()
    public static void updateIndexTest() throws IOException {
        System.out.println("Testing updateIndex()...");
        try {
            Git.updateIndex("test_file.txt");
            System.out.println("Index updated successfully");
        } catch (IOException e) {
            System.out.println("Error in updateIndex test: " + e.getMessage());
        }
    }

    // Test method for getFilePath()
    public static void getFilePathTest() {
        System.out.println("Testing getFilePath()...");
        File testFile = new File("git-project-COOPER/test.txt");
        String path = Git.getFilePath(testFile);
        System.out.println("File path: " + path);
    }

    // Test method for createTree()
    public static void createTreeTest() throws IOException {
        System.out.println("Testing createTree()...");
        try {
            File testDir = new File("test_directory");
            testDir.mkdir();
            File testFile = new File(testDir, "test.txt");
            Files.writeString(testFile.toPath(), "Tree test content");

            String treeHash = Git.createTree(testDir.getPath());
            System.out.println("Tree created with hash: " + treeHash);

            // Cleanup
            testFile.delete();
            testDir.delete();
        } catch (IOException e) {
            System.out.println("Error in createTree test: " + e.getMessage());
        }
    }

    // Test method for slashCount()
    public static void slashCountTest() {
        System.out.println("Testing slashCount()...");
        String testLine = "blob abc123 path/to/file.txt";
        int count = Git.slashCount(testLine);
        System.out.println("Line: " + testLine);
        System.out.println("Slash count: " + count);
    }

    // Test method for isCreatedCorrectly()
    public static void isCreatedCorrectlyTest() {
        System.out.println("Testing isCreatedCorrectly()...");
        boolean isCorrect = Git.isCreatedCorrectly();
        System.out.println("Repository is created correctly: " + isCorrect);
    }

    // Test method for hashFile()
    public static void hashFileTest() throws IOException {
        System.out.println("Testing hashFile()...");
        try {
            File testFile = new File("test_hash_file.txt");
            Files.writeString(testFile.toPath(), "Content to hash");
            String fileHash = Git.hashFile(testFile.getPath());
            System.out.println("File hash: " + fileHash);
            testFile.delete();
        } catch (IOException e) {
            System.out.println("Error in hashFile test: " + e.getMessage());
        }
    }

    // Test method for commit()
    public static void commitTest() {
        System.out.println("Testing commit()...");
        try {
            Git.commit("Test Author", "Test commit message");
            System.out.println("Commit created successfully");
        } catch (Exception e) {
            System.out.println("Error in commit test: " + e.getMessage());
        }
    }

    // Main method to run all tests
    public static void runAllTests() throws IOException {
        System.out.println("=== Running All Git Class Tests ===");

        createRepositoryTest();
        System.out.println();

        hashFunctionTest();
        System.out.println();

        contentsTest();
        System.out.println();

        createBlobSimpleTest();
        System.out.println();

        updateIndexTest();
        System.out.println();

        getFilePathTest();
        System.out.println();

        createTreeTest();
        System.out.println();

        slashCountTest();
        System.out.println();

        isCreatedCorrectlyTest();
        System.out.println();

        hashFileTest();
        System.out.println();

        commitTest();
        System.out.println();

        System.out.println("=== All Tests Completed ===");
    }



}
