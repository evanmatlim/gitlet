package gitlet;
import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    /** bruh. */
    private String fileName;
    /** Contents of tracked file stored as a String. */
    private String fileContents;

    public Blob(File f) {
        fileName = f.getName();
        fileContents = Utils.readContentsAsString(f);
    }

    public String blobID() {
        return Utils.sha1(Utils.serialize(this));
    }

    public String getName() {
        return fileName;
    }

    public String getFileAsString() {
        return fileContents;
    }

    public boolean sameFileContents(String contents) {
        return fileContents.equals(contents);
    }

    public void writeContents(String contents) {
        fileContents = contents;
    }

}
