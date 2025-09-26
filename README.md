Initializing the Repository:
The method createRepository is the method that initializes the repository, "Git". It starts by checking whether all the files and directories necessary are present. If they are, it lets the user know they all exist. If they aren't, it creates the missing files or directories. The files and directories present are:

git: the main directory
objects: a directory in git
HEAD: A file in git
index: A file in git

No difficulties with this part

SHA-1:
I utilized the format for initializing a SHA-1 from the GeeksforGeeks website. It helped me create a SHA-1 in my code. Now, this hash will serve as the unique identifier for a String file input.

GitTester:
This class is used to check the functionality of the Git class. It checks to verify if all the files are present, it deletes all the files and directories present, and it runs multiple initialization/cleanup cycles to confirm functionality.

boolean repositoryCheck():
This method confirms that the directories git and objects and files head and index are present. If they are, it returns true. Else, it returns false

void cleanup():
This method deletes the directories git and objects and files head and index. It is essentially a reset.

void repositoryCycles():
This method runs 10 initialization/cleanup cycles to ensure both methods function correctly.

I tested these methods in the main of Git to ensure that all three functions work. My code works on edge cases.

void createBlob():
This method takes in the String filePath and then generates a new BLOB where the contents of the file is the name and the contents of it are identical to the original file. If the filepath is null, this method does nothing.
