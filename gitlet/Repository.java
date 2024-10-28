package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Queue;

/** Repository where everything is done- Gitlet's engine.
 * @author Evan
 */
public class Repository implements Serializable {

    /** bruh. */
    static final File CWD = new File(".");
    /** bruh. */
    static final File GITLET = new File(".gitlet");
    /** bruh. */
    static final File COMMITS = new File(GITLET, "commits");
    /** bruh. */
    static final File CURRENT_BRANCH = new File(GITLET, "currentBranch");
    /** bruh. */
    static final File BRANCHES = new File(GITLET, "branches");
    /** bruh. */
    static final File BLOBS = new File(GITLET, "blobs");
    /** bruh. */
    static final File LOG = new File(GITLET, "log");
    /** bruh. */
    static final File STAGED_ADD = new File(GITLET, "stagedAdd");
    /** bruh. */
    static final File STAGED_REMOVE = new File(GITLET, "stagedRemove");

    /** Key: branch name, value: headCommitID. */
    private static TreeMap<String, String> branches;
    /** bruh. */
    private static String currentBranch;
    /** bruh. */
    private static Commit headCommit;
    /** bruh. */
    private static ArrayList<String> log;
    /** key: file name, value: blob ID. */
    private static TreeMap<String, String> stagedAdd;
    /** elem: file name. */
    private static TreeSet<String> stagedRemove;

    @SuppressWarnings("unchecked")
    public static void runItBack(String command) {
        if (!initialized()) {
            return;
        }
        stagedAdd = Utils.readObject(STAGED_ADD, TreeMap.class);
        stagedRemove = Utils.readObject(STAGED_REMOVE, TreeSet.class);
        branches = Utils.readObject(BRANCHES, TreeMap.class);
        currentBranch = Utils.readObject(CURRENT_BRANCH, String.class);
        headCommit = Utils.readObject(new File(COMMITS,
                branches.get(currentBranch)), Commit.class);
        log = Utils.readObject(LOG, ArrayList.class);
    }

    public static void init() {
        if (initialized()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        }
        GITLET.mkdir();
        COMMITS.mkdir();
        BLOBS.mkdir();
        stagedAdd = new TreeMap<>();
        Utils.writeObject(STAGED_ADD, stagedAdd);
        stagedRemove = new TreeSet<>();
        Utils.writeObject(STAGED_REMOVE, stagedRemove);
        Commit initial = new Commit("initial commit", null);
        File initialCommit = new File(COMMITS, initial.getUID());
        Utils.writeObject(initialCommit, initial);
        log = new ArrayList<String>();
        log.add(initial.getUID());
        Utils.writeObject(LOG, log);
        currentBranch = "master";
        Utils.writeObject(CURRENT_BRANCH, currentBranch);
        branches = new TreeMap<String, String>();
        branches.put(currentBranch, initial.getUID());
        Utils.writeObject(BRANCHES, branches);
    }

    public static void add(String[] args) {
        if (!initialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        } else if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        File file = new File(args[1]);
        String fileName = file.getName();
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        if (headCommit.tracksFile(fileName)
                && headCommit.sameFileContents(file)) {
            if (stagedAdd.containsKey(fileName)) {
                stagedAdd.remove(fileName);
            } else if (stagedRemove.contains(fileName)) {
                stagedRemove.remove(fileName);
            }
        } else {
            Blob newBlob = new Blob(file);
            stagedAdd.put(file.getName(), newBlob.blobID());
            Utils.writeObject(new File(BLOBS, newBlob.blobID()), newBlob);
        }
        Utils.writeObject(STAGED_ADD, stagedAdd);
        Utils.writeObject(STAGED_REMOVE, stagedRemove);
    }

    public static void commit(String[] args) {
        if (stagedAdd.isEmpty() && stagedRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        } else if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        } else if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Commit newCommit = new Commit(args[1], headCommit);
        headCommit = newCommit;
        for (String fileName: stagedAdd.keySet()) {
            headCommit.updateFile(fileName, stagedAdd.get(fileName));
        }
        stagedAdd.clear();
        for (String fileName: stagedRemove) {
            headCommit.removeFile(fileName);
        }
        stagedRemove.clear();
        branches.put(currentBranch, headCommit.getUID());
        File commit = new File(COMMITS, newCommit.getUID());
        Utils.writeObject(commit, newCommit);
        log.add(newCommit.getUID());
        Utils.writeObject(STAGED_ADD, stagedAdd);
        Utils.writeObject(STAGED_REMOVE, stagedRemove);
        Utils.writeObject(LOG, log);
        Utils.writeObject(BRANCHES, branches);
    }

    public static void remove(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        String fileName = args[1];
        if (!headCommit.tracksFile(fileName)
                && !stagedAdd.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (stagedAdd.containsKey(fileName)) {
            stagedAdd.remove(fileName);
        }
        if (headCommit.tracksFile(fileName)) {
            stagedRemove.add(fileName);
            File cwdFile = new File(fileName);
            if (cwdFile.exists()) {
                cwdFile.delete();
            }
        }
        Utils.writeObject(STAGED_ADD, stagedAdd);
        Utils.writeObject(STAGED_REMOVE, stagedRemove);
    }

    public static void log() {
        Commit curr = headCommit;
        while (curr != null) {
            printLog(curr);
            curr = curr.getParent();
        }
    }

    public static void globalLog() {
        for (String fileName: Utils.plainFilenamesIn(COMMITS)) {
            Commit commit = Utils.readObject(new File(COMMITS,
                    fileName), Commit.class);
            printLog(commit);
        }
    }

    private static void printLog(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getUID());
        commit.mergeMessage();
        System.out.println("Date: " + commit.getDate());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public static void find(String[] args) {
        if (args.length != 2) {
            System.out.println("Multiword messages must be "
                    + "surrounded by quotation marks.");
            return;
        }
        boolean found = false;
        for (String fileName: Utils.plainFilenamesIn(COMMITS)) {
            Commit commit = Utils.readObject(new File(COMMITS,
                    fileName), Commit.class);
            if (commit.getMessage().equals(args[1])) {
                System.out.println(fileName);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        if (!initialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println("=== Branches ===");
        for (String branchName: branches.keySet()) {
            if (branchName.equals(currentBranch)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.printf("%n=== Staged Files ===%n");
        for (String fileName: stagedAdd.keySet()) {
            System.out.println(fileName);
        }
        System.out.printf("%n=== Removed Files ===%n");
        for (String fileName: stagedRemove) {
            System.out.println(fileName);
        }

        System.out.printf("%n=== Modifications Not Staged For Commit ===%n");
        System.out.printf("%n=== Untracked Files ===%n");
    }

    public static void checkout(String[] args) {
        if (args.length == 2) {
            branchCheckout(args[1]);
        } else if (args.length == 3) {
            if (args[1].equals("--")) {
                headCheckout(args[2]);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else if (args.length == 4) {
            if (args[2].equals("--")) {
                commitCheckout(args[1], args[3]);
            } else {
                System.out.println("Incorrect operands.");
            }
        }
    }

    private static void headCheckout(String fileName) {
        if (!headCommit.tracksFile(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File current = new File(fileName);
        Utils.writeContents(current, headCommit.getFileAsString(fileName));
    }

    private static void commitCheckout(String commitID, String fileName) {
        File commitFile = abbrCommitFile(commitID);
        if (commitFile == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        if (!commit.tracksFile(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File current = new File(fileName);
        Utils.writeContents(current, commit.getFileAsString(fileName));
    }

    private static void branchCheckout(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        } else if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File commitFile = new File(COMMITS, branches.get(branchName));
        Commit commit = Utils.readObject(commitFile, Commit.class);
        if (oopsUntracked(commit)) {
            System.out.println("There is an untracked file "
                    + "in the way; delete it, or add and commit it first.");
            return;
        }
        for (String fileName: Utils.plainFilenamesIn(CWD)) {
            File current = new File(fileName);
            if (headCommit.tracksFile(fileName)
                    && !commit.tracksFile(fileName)) {
                current.delete();
            }
        }
        for (String fileName: commit.getFileNames()) {
            File current = new File(fileName);
            Utils.writeContents(current, commit.getFileAsString(fileName));
        }
        currentBranch = branchName;
        Utils.writeObject(CURRENT_BRANCH, currentBranch);
        stagedAdd.clear();
        stagedRemove.clear();
    }

    private static boolean oopsUntracked(Commit commit) {
        for (String fileName: Utils.plainFilenamesIn(CWD)) {
            if (!headCommit.tracksFile(fileName)
                    && commit.tracksFile(fileName)) {
                return true;
            }
        }
        return false;
    }

    public static void branch(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        } else if (branches.containsKey(args[1])) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        branches.put(args[1], headCommit.getUID());
        Utils.writeObject(BRANCHES, branches);
    }

    public static void removeBranch(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        } else if (!branches.containsKey(args[1])) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (args[1].equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branches.remove(args[1]);
        Utils.writeObject(BRANCHES, branches);
    }

    public static void reset(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        File commitFile = new File(COMMITS, args[1]);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        if (oopsUntracked(commit)) {
            System.out.println("There is an untracked file "
                    + "in the way; delete it, or add and commit it first.");
            return;
        }
        branches.put(currentBranch, args[1]);
        for (String fileName: headCommit.getFileNames()) {
            if (!commit.tracksFile(fileName)) {
                new File(fileName).delete();
            }
        }
        for (String fileName: commit.getFileNames()) {
            commitCheckout(commit.getUID(), fileName);
        }
        stagedAdd.clear();
        stagedRemove.clear();
        Utils.writeObject(BRANCHES, branches);
        Utils.writeObject(STAGED_ADD, stagedAdd);
        Utils.writeObject(STAGED_REMOVE, stagedRemove);
    }

    public static void merge(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            return;
        } else if (!(stagedAdd.isEmpty() && stagedRemove.isEmpty())) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        String givenBranch = args[1];
        if (!branches.containsKey(givenBranch)) {
            System.out.println(" A branch with that name does not exist.");
            return;
        } else if (givenBranch.equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit given = Utils.readObject(new File(COMMITS,
                branches.get(givenBranch)), Commit.class);
        Commit current = headCommit;
        if (oopsUntracked(given)) {
            System.out.println("There is an untracked file "
                    + "in the way; delete it, or add and commit it first.");
            return;
        }
        Commit splitPoint = closestCommonAncestor(given);
        if (splitPoint.getUID().equals(given.getUID())) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return;
        } else if (splitPoint.equals(headCommit)) {
            branchCheckout(givenBranch);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        mergeHelper(given, current, splitPoint);
        Commit newCommit = new Commit("Merged " + givenBranch
                + " into " + currentBranch + ".", headCommit);
        for (String fileName: stagedAdd.keySet()) {
            headCommit.updateFile(fileName, stagedAdd.get(fileName));
        }
        stagedAdd.clear();
        for (String fileName: stagedRemove) {
            headCommit.removeFile(fileName);
        }
        stagedRemove.clear();
        newCommit.setMergedParent(given.getUID());
        headCommit = newCommit;
        branches.put(currentBranch, headCommit.getUID());
        File commit = new File(COMMITS, newCommit.getUID());
        Utils.writeObject(commit, newCommit);
        log.add(newCommit.getUID());
        Utils.writeObject(STAGED_ADD, stagedAdd);
        Utils.writeObject(STAGED_REMOVE, stagedRemove);
        Utils.writeObject(LOG, log);
        Utils.writeObject(BRANCHES, branches);
    }

    private static void mergeHelper(Commit given,
                                    Commit current,
                                    Commit splitPoint) {
        HashSet<String> allFileNames = new HashSet<>();
        for (String fileName: given.getFileNames()) {
            allFileNames.add(fileName);
        }
        for (String fileName: headCommit.getFileNames()) {
            allFileNames.add(fileName);
        }
        for (String fileName: splitPoint.getFileNames()) {
            allFileNames.add(fileName);
        }
        for (String fileName: allFileNames) {
            File cwdFile = new File(CWD, fileName);
            if (splitPoint.tracksFile(fileName)) {
                if (given.tracksFile(fileName)) {
                    if (current.tracksFile(fileName)) {
                        if (!same(given, current, fileName)) {
                            if (!same(splitPoint, given, fileName)) {
                                if (same(splitPoint, current, fileName)) {
                                    Blob blob = given.getBlob(fileName);
                                    Utils.writeContents(cwdFile,
                                            blob.getFileAsString());
                                    stagedAdd.put(fileName,
                                            given.getBlobID(fileName));
                                } else {
                                    mergeConflict(current, given, fileName);
                                }
                            }
                        }
                    } else {
                        if (same(splitPoint, given, fileName)) {
                            miniAdd(fileName);
                        } else {
                            mergeConflict(current, given, fileName);
                        }
                    }
                } else {
                    if (current.tracksFile(fileName)) {
                        if (same(splitPoint, current, fileName)) {
                            miniAdd(fileName);
                        } else {
                            mergeConflict(current, given, fileName);
                        }
                    }
                }
            } else {
                if (current.tracksFile(fileName)) {
                    if (given.tracksFile(fileName)
                            && !same(given, current, fileName)) {
                        mergeConflict(current, given, fileName);
                    }
                } else {
                    Blob blob = given.getBlob(fileName);
                    Utils.writeContents(cwdFile, blob.getFileAsString());
                    stagedAdd.put(fileName, given.getBlobID(fileName));
                }
            }
        }
    }

    private static void miniAdd(String fileName) {
        File cwdFile = new File(fileName);
        stagedRemove.add(fileName);
        if (cwdFile.exists()) {
            cwdFile.delete();
        }
    }

    private static boolean same(Commit one, Commit two, String fileName) {
        return one.sameContents(fileName, two.getFileAsString(fileName));
    }

    private static void mergeConflict(Commit head, Commit given,
                                      String fileName) {
        String currentFile = head.getFileAsString(fileName);
        String givenFile = given.getFileAsString(fileName);
        String result = "<<<<<<< HEAD\n" + currentFile
                + "=======\n" + givenFile + ">>>>>>>\n";
        File cwdFile = new File(fileName);
        Utils.writeContents(cwdFile, result);
        Blob blob = new Blob(cwdFile);
        stagedAdd.put(fileName, blob.blobID());
        System.out.println("Encountered a merge conflict.");
    }

    private static Commit closestCommonAncestor(Commit given) {
        HashSet<String> givenAncestors = given.allAncestors();
        Queue<Commit> headAncestors = new LinkedList<>();
        headAncestors.add(headCommit);
        Commit found = null;
        while (headAncestors.size() > 0) {
            Commit curr = headAncestors.remove();
            if (givenAncestors.contains(curr.getUID())) {
                found = curr;
                break;
            }
            if (curr.getParent() != null) {
                headAncestors.add(curr.getParent());
            }
            if (curr.getMergedParent() != null) {
                headAncestors.add(curr.getMergedParent());
            }
        }
        return found;
    }

    private static File abbrCommitFile(String abbreviation) {
        for (String fileName: Utils.plainFilenamesIn(COMMITS)) {
            if (abbreviation.equals(fileName.substring(0,
                    abbreviation.length()))) {
                return new File(COMMITS, fileName);
            }
        }
        return null;
    }

    private static boolean initialized() {
        return CURRENT_BRANCH.exists();
    }
}
