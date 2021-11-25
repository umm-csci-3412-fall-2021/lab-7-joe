package segmentedfilesystem.model;

import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        // `instanceof` will also handle the case where `o` is null.
        if (!(o instanceof File)) {
            return false;
        }

        File other = (File) o;
        return Objects.equals(filename, other.filename) && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        Object[] allRelevantValues = new Object[1 + data.length];
        allRelevantValues[0] = filename;
        for (int i = 0; i < data.length; i++) {
            allRelevantValues[i + 1] = data[i];
        }
        return Arrays.hashCode(allRelevantValues);
    }

    @Override
    public String toString() {
        return String.format("File[%s, %s]", filename, Arrays.toString(data));
    }
}
