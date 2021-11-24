package segmentedfilesystem.model;

public class File {
    private String filename;
    public String getFilename() {
        return filename;
    }

    private byte[] data;
    public byte[] getData() {
        return data;
    }

    public File(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
    }
}
