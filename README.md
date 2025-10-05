GIT CONTENT:

No difficulties with this part

createRepository():
The method createRepository is the method that initializes the repository, "Git". It starts by checking whether all the files and directories necessary are present. If they are, it lets the user know they all exist. If they aren't, it creates the missing files or directories. The files and directories present are:

git: the main directory
objects: a directory in git
HEAD: A file in git
index: A file in git

hashFunction(String input):
I utilized the format for initializing a SHA-1 from the GeeksforGeeks website. It helped me create a SHA-1 in my code. Now, this hash will serve as the unique identifier for a String file input.

void createBlob(String filePath):
This method takes in the String filePath and then generates a new BLOB where the contents of the file is the name and the contents of it are identical to the original file. If the filepath is null, this method does nothing.

void updateIndex(String filePath):
This method adds BLOB and file entries to the index with this format: blob 4377a91cdfd44db9a9bbf056849c7da0fc6cc7be myProgram/README.md
blob 0a4d55a8d778e5022fab701977c5d840bbc486d0 myProgram/Hello.txt
blob 0acc46ad73849ea9832f600de83a014c9db9cdf0 myProgram/scripts/Cat.java
Each line contains the SHA1 hash followed by a single-space and then the original filename. It uses a bufferedwriter to write to the index file. It handles duplicate file names, files with the same contents, and updates files as they change. 

String getFilePath(String filePath):
This method returns the path to a file relative to your project’s parent directory rather than the absolute path, as we deem those excessively long. 

public static String createTree(String directoryPath):
This method creates a tree file containing references to all files and subdirectories. It generates blob objects for files and recursively creates tree files for subdirectories. Everything is stored in the objects directory with unique SHA-1 hashes. It returns the SHA-1 hash of the generated tree based on its contents.


GITTESTER CONTENT:
This class is used to check the functionality of the Git class. It checks to verify if all the files are present, it deletes all the files and directories present, and it runs multiple initialization/cleanup cycles to confirm functionality.

boolean repositoryCheck():
This method confirms that the directories git and objects and files head and index are present. If they are, it returns true. Else, it returns false

void cleanup():
This method deletes the directories git and objects and files head and index. It is essentially a reset.

void repositoryCycles():
This method runs 10 initialization/cleanup cycles to ensure both methods function correctly.

I tested these methods in the main of Git to ensure that all three functions work. My code works on edge cases.

boolean blobTest():
This method checks whether there are blobs present in the objects directory. If there aren't any, it returns false. However, if there are more than 0 files in the directory, it lists them out and returns true.

void reset():
This method resets all the files used, so that methods can be repeatedly tested again. It uses the helper method of deleteGit(File file). It creates a new repository after.

void deleteGit(File file):
This method deletes everything in the repository to help reset the files. It recursively removes the children of each directory, eventually removing the root directory as well.

void indexTest():
This method creates a sample of text files with different contents, adds entries to the index file for each file, generates the corresponding BLOBs in the objects directory, and verifies that the index entries match the actual files.

void robustReset():
This method deletes all generated files from previous tests, including all objects in the objects directory. It calls on the helper method resetAllFiles(File directory). It deletes the index file and then creates a new one to reset it.

void resetAllFiles(File directory):
This is the helper method for robustReset(). It recursively deletes all generated files from previous tests including all objects in the objects directory.


