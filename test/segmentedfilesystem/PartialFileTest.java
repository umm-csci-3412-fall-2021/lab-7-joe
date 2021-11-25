package segmentedfilesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import segmentedfilesystem.model.File;

public class PartialFileTest {
    private static final PartialFile NEW_PARTIAL_FILE = new PartialFile();

    private static final PartialFile WITHOUT_FILENAME = new PartialFile()
        .withNumberOfChunks(1)
        .addChunk(0, new byte[] { 1, 2, 3 });

    private static final PartialFile WITHOUT_NUMBER_OF_CHUNKS = new PartialFile()
        .withFilename("how-long-am-I.txt")
        .addChunk(0, new byte[] { 1, 2, 3 })
        .addChunk(1, new byte[] { 4, 5 })
        .addChunk(2, new byte[] { 6, 7, 8, 9 });

    private static final PartialFile WITHOUT_ANY_DATA = new PartialFile()
        .withFilename("missing-some-data.txt")
        .withNumberOfChunks(3);

    private static final PartialFile MISSING_SOME_DATA = new PartialFile()
        .withFilename("missing-some-data.txt")
        .withNumberOfChunks(3)
        .addChunk(0, new byte[] { 1, 2, 3 })
        .addChunk(2, new byte[] { 6, 7, 8, 9 });

    private static final PartialFile MISSING_FIRST_CHUNK = new PartialFile()
        .withFilename("missing-some-data.txt")
        .withNumberOfChunks(3)
        .addChunk(1, new byte[] { 4, 5 })
        .addChunk(2, new byte[] { 6, 7, 8, 9 });

    private static final PartialFile COMPLETE_PARTIAL_FILE = new PartialFile()
        .withFilename("the-coolest-data.txt")
        .withNumberOfChunks(3)
        .addChunk(0, new byte[] { 1, 2, 3 })
        .addChunk(1, new byte[] { 4, 5 })
        .addChunk(2, new byte[] { 6, 7, 8, 9 });

    private static final PartialFile ZERO_BYTES_LONG = new PartialFile()
        .withFilename("no-content.txt")
        .withNumberOfChunks(0);

    private static final List<PartialFile> INCOMPLETE_PARTIAL_FILES = List.of(
        NEW_PARTIAL_FILE,
        WITHOUT_FILENAME,
        WITHOUT_NUMBER_OF_CHUNKS,
        WITHOUT_ANY_DATA,
        MISSING_SOME_DATA,
        MISSING_FIRST_CHUNK
    );

    @Test
    public void testNewPartialFileIsIncomplete() {
        assertFalse(NEW_PARTIAL_FILE.isComplete());
    }

    @Test
    public void testPartialFileIsIncompleteWithoutFilename() {
        assertFalse(WITHOUT_FILENAME.isComplete());
    }

    @Test
    public void testPartialFileWithUnknownNumberOfChunksIsIncomplete() {
        assertFalse(WITHOUT_NUMBER_OF_CHUNKS.isComplete());
    }

    @Test
    public void testPartialFileMissingDataIsIncomplete() {
        assertFalse(WITHOUT_ANY_DATA.isComplete());
        assertFalse(MISSING_SOME_DATA.isComplete());
        assertFalse(MISSING_FIRST_CHUNK.isComplete());
    }

    @Test
    public void testPartialFileWithAllDataIsComplete() {
        assertTrue(COMPLETE_PARTIAL_FILE.isComplete());
    }

    @Test
    public void testZeroByteLongPartialFileIsComplete() {
        assertTrue(ZERO_BYTES_LONG.isComplete());
    }

    @Test
    public void testToFileWorks() {
        File file = COMPLETE_PARTIAL_FILE.toFile();
        assertThat(file.getName()).isEqualTo("the-coolest-data.txt");
        assertThat(file.getData()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    public void testToFileWorksOnZeroByteLongPartialFile() {
        File file = ZERO_BYTES_LONG.toFile();
        assertThat(file.getName()).isEqualTo("no-content.txt");
        assertThat(file.getData()).isEmpty();
    }

    @Test
    public void testConvertingIncompletePartialFileToFileThrows() {
        for (PartialFile pf : INCOMPLETE_PARTIAL_FILES) {
            assertThrows(IllegalStateException.class, () -> pf.toFile());
        }
    }
}
