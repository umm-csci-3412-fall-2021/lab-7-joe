package segmentedfilesystem.model;

import java.nio.charset.StandardCharsets;

public class OutOfMoneyHeaderPacket extends OutOfMoneyPacket {
    public String getFilename() {
        // Each header packet contains a the name of a file, encoded in UTF-8.
        return new String(getContents(), StandardCharsets.UTF_8);
    }

    public OutOfMoneyHeaderPacket(byte fileId, byte[] contents) {
        super(true, fileId, contents);
    }
}
