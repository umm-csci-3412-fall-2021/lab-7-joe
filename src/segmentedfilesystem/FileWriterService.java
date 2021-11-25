package segmentedfilesystem;

import java.io.FileOutputStream;
import java.io.IOException;

import segmentedfilesystem.model.File;

/**
 * A small class that knows how to write files to disk.
 */
public class FileWriterService {
    public void createFileInWorkingDirectory(File file) {
        try (var outputStream = new FileOutputStream(file.getName())) {
            outputStream.write(file.getData());
        } catch (IOException e) {
            System.err.printf("Unable to write to file %s\n", file.getName());
            e.printStackTrace();
        }
    }
}
