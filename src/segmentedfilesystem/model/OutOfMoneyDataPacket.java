package segmentedfilesystem.model;

public class OutOfMoneyDataPacket extends OutOfMoneyPacket {
    private int packetNumber;
    public int getPacketNumber() {
        return packetNumber;
    }

    private boolean isFinalPacket;
    public boolean isFinalPacket() {
        return isFinalPacket;
    }

    public OutOfMoneyDataPacket(byte fileId, byte[] contents, int packetNumber, boolean isFinalPacket) {
        super(false, fileId, contents);
        this.packetNumber = packetNumber;
        this.isFinalPacket = isFinalPacket;
    }
}
