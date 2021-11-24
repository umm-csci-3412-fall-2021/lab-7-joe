package segmentedfilesystem;

import java.util.SortedMap;
import java.util.TreeMap;

import segmentedfilesystem.model.File;

/**
 * This class represents a file in the process of being downloaded.
 */
public class PartialFile {
    /**
     * A map storing every chunk of the file we've received so far.
     *
     * The keys are packet numbers; the values are packet contents.
     */
    private SortedMap<Integer, byte[]> chunks = new TreeMap<Integer, byte[]>();

    /**
     * The name of the file.
     */
    private String filename = null;
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public PartialFile withFilename(String filename) {
        setFilename(filename);
        return this;
    }


    /**
     * The total number of chunks in the file.
     *
     * We don't know what this value is until after we've received the final data packet.
     */
    private Integer numberOfChunks = null;
    public void setNumberOfChunks(int numberOfChunks) {
        this.numberOfChunks = numberOfChunks;
    }
    public PartialFile withNumberOfChunks(int numberOfChunks) {
        setNumberOfChunks(numberOfChunks);
        return this;
    }

    public PartialFile addChunk(int packetNumber, byte[] data) {
        chunks.put(packetNumber, data);
        return this;
    }

    /**
     * Return whether we've received the whole file from the server.
     *
     * If we've received the whole file, we can build it into a File object.
     *
     * If we haven't received the whole file yet, we should wait until we get more packets.
     */
    public boolean isComplete() {
        return filename != null
            && numberOfChunks != null
            && chunks.keySet().containsAll(Utils.range(0, numberOfChunks));
    }

    /**
     *
     * If this PartialFile is not complete (as defined by the `isComplete()` method), `toFile()` will throw an
     * `IllegalStateException`.
     */
    public File toFile() {
        if (!isComplete()) {
            throw new IllegalStateException("Trying to build a partial file before it's complete.");
        }

        return new File(filename, Utils.flatten(chunks.values()));
    }
}
