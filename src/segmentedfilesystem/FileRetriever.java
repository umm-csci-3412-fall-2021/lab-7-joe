package segmentedfilesystem;

import java.util.HashMap;

import segmentedfilesystem.model.OutOfMoneyDataPacket;
import segmentedfilesystem.model.OutOfMoneyHeaderPacket;
import segmentedfilesystem.model.OutOfMoneyPacket;

/**
 * This class connects to an OutOfMoney.com server, receives a number of files from it, and writes those files to disk.
 */
public class FileRetriever {
    public static final int NUMBER_OF_FILES_EXPECTED = 3;

    OutOfMoneyApiService outOfMoneyApiService;
    FileWriterService fileWriterService;

    // We're using a dependency-injection style, to make unit tests easier.
    public FileRetriever(OutOfMoneyApiService outOfMoneyApiService, FileWriterService fileWriterService) {
        this.outOfMoneyApiService = outOfMoneyApiService;
        this.fileWriterService = fileWriterService;
    }

    public void downloadFiles(String serverName, int port) {
        // A map from file IDs to partial files (which we'll append to until they're complete).
        var downloadingFiles = new HashMap<Byte, PartialFile>();

        outOfMoneyApiService.sendRequest(serverName, port);

        // Keep reading packets until we have all the files, and they're complete.
        while (
            downloadingFiles.size() < NUMBER_OF_FILES_EXPECTED
            || downloadingFiles.values().stream().anyMatch(pf -> !pf.isComplete())
        ) {
            OutOfMoneyPacket packet = outOfMoneyApiService.getPacket(serverName, port);

            byte fileId = packet.getFileId();
            if (!downloadingFiles.containsKey(fileId)) {
                downloadingFiles.put(fileId, new PartialFile());
            }

            PartialFile partialFile = downloadingFiles.get(fileId);
            if (packet.isHeaderPacket()) {
                addHeaderToFile(partialFile, (OutOfMoneyHeaderPacket) packet);
            } else {
                addDataToFile(partialFile, (OutOfMoneyDataPacket) packet);
            }
        }

        for (PartialFile pf : downloadingFiles.values()) {
            fileWriterService.createFileInWorkingDirectory(pf.toFile());
        }
    }

    private void addHeaderToFile(PartialFile partialFile, OutOfMoneyHeaderPacket headerPacket) {
        partialFile.setFilename(headerPacket.getFilename());
    }

    private void addDataToFile(PartialFile partialFile, OutOfMoneyDataPacket dataPacket) {
        partialFile.addChunk(dataPacket.getPacketNumber(), dataPacket.getContents());
        if (dataPacket.isFinalPacket()) {
            partialFile.setNumberOfChunks(dataPacket.getPacketNumber() + 1);
        }
    }
}
