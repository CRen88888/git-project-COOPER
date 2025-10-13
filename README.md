GIT CONTENT:

No difficulties with this part

createRepository():
The method createRepository is the method that initializes the repository, "Git". It starts by checking whether all the files and directories necessary are present. If they are, it lets the user know they all exist. If they aren't, it creates the missing files or directories. The files and directories present are:

git: the main directory
objects: a directory in git
HEAD: A file in git
index: A file in git

String hashFunction(String input):
I utilized the format for initializing a SHA-1 from the GeeksforGeeks website. It helped me create a SHA-1 in my code. Now, this hash will serve as the unique identifier for a String file input. It returns the sha of a String

void createBlob(String filePath):
This method takes in the String filePath and then generates a new BLOB where the contents of the file is the name and the contents of it are identical to the original file. If the filepath is null, this method does nothing. It also updates the index with the new blob. 

void updateIndex(String filePath):
This method adds BLOB and file entries to the index with this format: blob 4377a91cdfd44db9a9bbf056849c7da0fc6cc7be myProgram/README.md
blob 0a4d55a8d778e5022fab701977c5d840bbc486d0 myProgram/Hello.txt
blob 0acc46ad73849ea9832f600de83a014c9db9cdf0 myProgram/scripts/Cat.java
Each line contains the SHA1 hash followed by a single-space and then the original filename. It uses a bufferedwriter to write to the index file. It handles duplicate file names, files with the same contents, and updates files as they change.

String getFilePath(File file):
This method returns the path to a file relative to your project’s parent directory rather than the absolute path, as we deem those excessively long.

int slashCount(String line):
This method returns the number of slashes present in the relative path of a tree or blob. It is called in createIndexTree() and createIndexTreeHelper() and is useful for sorting based on most slashes first in the working list. it returns the number of slashes in a relative path.

String pathLine(String line):
This method returns the relative path of a blob or tree by parsing the line into three different parts. The last part is the relative path, which it returns. This is useful for the sortSlashCountDescend(List<String> lines) method, as it helps organize the order of the lines. It returns the relative path of a blob or tree.

void sortSlashCountDescend(List<String> lines):
This method organizes a list of lines based on the number of slashes and alphabetically for the relative path of a tree or blob. It puts the blobs or trees with the most slashes at the front, alphabatizing the order in case some have the same number of slashes. This is useful in the createIndexTree() and createIndexTreeHelper() methods to keep the working list organized and updated.

String createTree(String directoryPath):
This method creates a tree file containing references to all files and subdirectories. It generates blob objects for files and recursively creates tree files for subdirectories. Everything is stored in the objects directory with unique SHA-1 hashes. It returns the SHA-1 hash of the generated tree based on its contents.

String createIndexTree():
This method uses the index file to generate trees recursively. It creates a temporary "working list" file that duplicates the contents of the index file, with 'blob' prefixed on each line. It parses each line into 'blob', 'sha', and 'path'. It then organizes the lines so that the ones with the most slashes are at the front of the working list. It calls on createIndexTreeHelper() until there is only one directory left also known as the root directory. It returns the hash of this root directory.

void createIndexTreeHelper():
This method is the helper method for createIndexTree(). It merges the blob(s) deepest in the tree into a subdirectory or tree from the working list. It is called in a while loop in createIndexTree() until there is only a root directory present in the working list. It parses lines into three parts: type, sha, and relative path. It picks the current deepest entry and determines its target directory. From that, it makes a tree object for the parent directory via CreateTree(). It then replaces all entries from that directory with a single consolidated line with information of the tree and resorts the remaining blobs and trees in the working list. Repeating this helper collapses siblings into their parent directory, then parents into their ancestors, until only one line remains—the root tree—whose hash is the final output of createIndexTree().

String hashFile():
This method hashes the contents of a file using a SHA1 hash and returns the result.

boolean isCreatedCorrectly():
This method first finds the root index, then calls the recursive isCreatedCorrectlyHelper() to verify if all trees and BLOBs that were supposed to be created, according to the index, have been.

boolean isCreatedCorrectlyHelper():
This method finds all BLOBs and trees that a given file in the bjects folder references, and calls itself to see if the files it references exist. 

void commit():
This method prompts the user for an author name and message, before creating a commit object that is stored in git\objects. If a parent exists in the HEAD file, it is included in the message. The current root hash and the time are also stored. 


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

void createIndexTreeTest():
This method tests the createIndexTree method. It creates two files, a and b inside a sample directory. The b file is in a sub directory called dir2. It then sysouts the root hash at the end of the root tree. 
