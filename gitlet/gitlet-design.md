# Gitlet Design Document
author: Dana Li, Evan Lim

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely
format and style a text file. Organize your design document in a way that
will make it easy for you or a course-staff member to read.

## 1. Classes and Data Structures

### Main.java
Processes commands and calls them in Repository.java.
#### Fields
1. File REPOSITORY: A persistent file containing a serialized Repository object.
2. Repository repo: The Repository instance in which the local data is stored.

### Repository.java
General working area that calls methods and stores data structures.
#### Fields
1. ArrayList<File> _stagingArea: A copy (not shallow!) of StagingArea’s static ArrayList.
2. TreeMap<String, Commit> _branches: A copy (not shallow!) of branches from the Commit class.
3. TreeMap<String, Commit> _commitTree: A copy (not shallow!) of commitTree from the Commit class.

### StagingArea.java
Checks if files need to be staged/unstaged, stages appropriate files to be committed, and creates blobs for files. Implements Serializable.
#### Fields
1. static ArrayList<File> stagingArea: An ArrayList of  all files ready to be committed.

### Commit.java
Captures snapshots of files and tracks them.
#### Fields
1. static TreeMap<String, Commit> commitTree: A TreeMap that contains all Commits in the Branch, whose keys are each Commit’s unique hashcode.
2. static TreeMap<String, Commit> branches: A TreeMap that holds the Commits that each branch points to as values, with each branch’s name as the key.
3. String branchName: Name of the branch that contains the Commit (master by default).
4. Commit parent: A Commit instance’s parent.
5. Commit secondParent: A Commit instance’s parent (null by default).
6. Time timestamp: The time when a  Commit instance is created.
7. String logMessage: The message displayed for a particular Commit when “log” is called.
8. ArrayList<Blob> blobs: An ArrayList of Blob instances that correspond to each file’s state.
9. static Commit HEAD: The current Head Commit.
10. static Commit Master: The rightmost Commit of the master Branch.

### Blob.java
Contains serialized file and stores the collection of blobs.
#### Fields
1. static TreeMap blobTree: A TreeMap that contains all Blobs, whose keys are each Blob’s unique hashcode.
2. static String content: A String representation of the  serialized file.


## 2. Algorithms

### Main.java
1. init()
  1. It creates a new version-control system in the current directory if there isn't one already. It only has the master branch that points to this initial commit. All repositories share this commit and all commits in all repositories trace back to it.2. add(String file) It adds/stages the file, overwriting nonidentical files if necessary. Exits if file does not exist.
2. add(String fileName):
  1. It adds the file to stagingArea, overwriting nonidentical files if necessary.
  2. It exits if file does not exist.
  3. It creates a blob.
3. commit(String message):
  1. It calls the commit method in the repository class.
4. status():
  1. It displays branches, staged files, removed files, modifications not staged for commit, and untracked files.
    1. Modifications not staged for commit: files that have been tracked, changed, but not staged OR files that have been staged for addition but with different contents from working directory or have been deleted in working directory OR files that have not been staged for removal but tracked in current commit and deleted from working directory.
    2. Untracked files: files that are present but have not been staged for addition nor tracked. Includes files that have been staged for removal but have been re-created.
5. checkout():
  1. Overwrites file in working directory with file from HEAD commit in working directory.
  2. New file not staged.
  3. Errors if file does not if file is not in previous commit.
6. checkout(String commitID):
  1. Overwrites file in working directory with commit file that has the same commit id.
  2. New file not staged.
  3. Errors if commit does not exist.
  4. Errors if file does not exist in commit.
7. checkout(String branchName):
  1. Prints out message and exits if working file is untracked in current branch and could be overwritten by checkout.
  2. Overwrites files in working directory with all files at the commit in HEAD of branch.
  3. Files tracked in the current branch but not in the checked-out branch are deleted.
  4. Staging area cleared if checked out branch is not the current branch.
  5. This branch becomes the current branch.
  6. Errors if branch does not exist.
  7. Prints out message if branch is current branch.

### Repository.java
1. commit(String message):
  1. It adds a new node to commitTree.
  2. It copies the parent commit and updates it with additions/removals.
  3. It clears the stagingArea.
  4. Call branches.set(self.branchName, self)
  5. It updates parent and HEAD.
  6. It updates logMessage.
  7. It adds blobs.
  8. It stops if there is no file in the stagingArea.
  9. If raises an error if the file has a blank message.
2. log():
  1. It displays information (commit id, timestamp, message) about every commit in reverse order, ignoring second parents.
3. global-log():
  1. It displays information about all commits, order does not matter.
4. find(String message):
  1. It prints all commit ids of the commits that have the same commit message.
5. branch(String branchName):
  1. Adds branchName to TreeMap of branches with HEAD as the value.
  2. Errors if branch already exists.
  3. TreeMap of branches only changes if checkout has been called on branch.
6. rm-branch(String branchName):
  1. Removes branch with branchName by deleting pointer, does not delete any commits.
  2. Errors if branch with branchName does not exist.
  3. Errors if branchName is current working branch.
7. merge(String branchName):
  1. Merge files from branchName to current branch.
  2. Unmodified files in the current branch should be updated to modified versions in given branch AND automatically staged.
  3. Files not present at split point and are present in given branch should be checked out and staged.
  4. Files present at split point, unmodified in current branch, and absent in given branch should be removed and untracked.
  5. Replaces contents of files in conflict(modified in different ways in current and given branch) and stages result.
  6. If split point is current branch, then check out given branch.
  7. Errors if there are staged additions or removals.
  8. Errors if branchName does not exist.
  9. Errors if trying to merge a branch with itself.
  10. Errors if untracked file in current commit would be overwritten or deleted by merge.

### StagingArea.java
1. rm(String fileName):
  1. It unstages the file if file is in stagingArea.
  2. It stages the file for removal and removes it from the working directory ONLY IF the file is in the current commit.
  3. Errors if branch with branchName does not exist.
  4. Errors if branchName is current working branch.


### Commit.java


### Blob.java


This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

  * Checking if a merge is necessary.
  * Determining which files (if any) have a conflict.
  * Representing the conflict in the file.

* Try to clearly mark titles or names of classes with white space or
  some other symbols.

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
  `java gitlet.Main add wug.txt`,
  on the next execution of
  `java gitlet.Main commit -m “modify wug.txt”`,
  the correct commit will be made.

* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.

* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to
visualize the structure and workflow of your program.


