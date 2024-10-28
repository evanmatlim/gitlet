package gitlet;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.io.Serializable;

public class Commit implements Serializable {
    /** bruh. */
    private static File cOMMITS = new File(".gitlet/commits");
    /** bruh. */
    private static File bLOBS = new File(".gitlet/blobs");
    /** bruh. */
    private String parentID;
    /** bruh. */
    private String mergedParentID;
    /** bruh. */
    private Date date;
    /** bruh. */
    private String message;
    /** Key: file name, Value: blobID. */
    private HashMap<String, String> blobs;

    public Commit(String msg, Commit parent) {
        message = msg;
        if (parent != null) {
            parentID = parent.getUID();
            this.blobs = parent.blobs();
            date = new Date();
        } else {
            blobs = new HashMap<String, String>();
            date = new Date(0);
        }
    }

    public String getParentID() {
        return parentID;
    }

    public String getMergedParentID() {
        return mergedParentID;
    }

    public void setMergedParent(String mpID) {
        mergedParentID = mpID;
    }

    public Commit getParent() {
        if (parentID == null) {
            return null;
        }
        File parentFile = new File(cOMMITS, parentID);
        return Utils.readObject(parentFile, Commit.class);
    }

    public Commit getMergedParent() {
        if (mergedParentID == null) {
            return null;
        }
        File mergedParentFile = new File(cOMMITS, mergedParentID);
        return Utils.readObject(mergedParentFile, Commit.class);
    }

    public HashMap<String, String> blobs() {
        return blobs;
    }

    public String getUID() {
        return Utils.sha1(Utils.serialize(this));
    }

    public String getDate() {
        String pattern = "E MMM dd HH:mm:ss yyyy Z";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    public String getMessage() {
        return message;
    }

    public boolean sameFileContents(File file) {
        File blobFile = new File(bLOBS, blobs.get(file.getName()));
        if (!blobFile.exists()) {
            return false;
        }
        Blob blob = Utils.readObject(blobFile, Blob.class);
        String fileContents = Utils.readContentsAsString(file);
        return blob.getFileAsString().equals(fileContents);
    }

    public boolean sameContents(String fileName, String fileContents) {
        if (!blobs.containsKey(fileName)) {
            return false;
        }
        File blobFile = new File(bLOBS, blobs.get(fileName));
        Blob blob = Utils.readObject(blobFile, Blob.class);
        return blob.sameFileContents(fileContents);
    }

    public void updateFile(String fileName, String blobID) {
        blobs.put(fileName, blobID);
    }

    public void removeFile(String fileName) {
        blobs.remove(fileName);
    }

    public boolean tracksFile(String fileName) {
        return blobs.containsKey(fileName);
    }

    public String getFileAsString(String fileName) {
        if (!blobs.containsKey(fileName)) {
            return "";
        }
        File blobFile = new File(bLOBS,  blobs.get(fileName));
        Blob b = Utils.readObject(blobFile, Blob.class);
        return b.getFileAsString();
    }

    public String[] getFileNames() {
        return blobs.keySet().toArray(new String[blobs.size()]);
    }

    public Blob getBlob(String fileName) {
        return Utils.readObject(new File(bLOBS, blobs.get(fileName)),
                Blob.class);
    }

    public String getBlobID(String fileName) {
        return blobs.get(fileName);
    }

    public HashSet<String> allAncestors() {
        HashSet<String> ancestors = new HashSet<>();
        ancestors.add(getUID());
        if (getParent() != null) {
            for (String ancestor: getParent().allAncestors()) {
                ancestors.add(ancestor);
            }
        }
        if (getMergedParent() != null) {
            for (String ancestor: getMergedParent().allAncestors()) {
                ancestors.add(ancestor);
            }
        }
        return ancestors;
    }

    public void mergeMessage() {
        if (mergedParentID != null) {
            String first = parentID.substring(0, 7);
            String second = mergedParentID.substring(0, 7);
            System.out.println("Merge: " + first + " " + second);
        }
    }

}
