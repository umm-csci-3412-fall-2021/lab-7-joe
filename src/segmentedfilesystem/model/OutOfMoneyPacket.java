package segmentedfilesystem.model;

/**
 * A packet from the OutOfMoney.com protocol, representing a piece of a file.
 */
public abstract class OutOfMoneyPacket {
    private boolean isHeaderPacket;
    public boolean isHeaderPacket() {
        return isHeaderPacket;
    }

    private byte fileId;
    public byte getFileId() {
        return fileId;
    }

    private byte[] contents;
    /**
     * For a header packet, `contents` contains the filename. For a data packet, `contents` contains the a chunk of the
     * file.
     */
    public byte[] getContents() {
        return contents;
    }

    protected OutOfMoneyPacket(boolean isHeaderPacket, byte fileId, byte[] contents) {
        this.isHeaderPacket = isHeaderPacket;
        this.fileId = fileId;
        this.contents = contents;
    }
}
