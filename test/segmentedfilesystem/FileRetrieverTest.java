package segmentedfilesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import segmentedfilesystem.model.File;
import segmentedfilesystem.model.OutOfMoneyDataPacket;
import segmentedfilesystem.model.OutOfMoneyHeaderPacket;
import segmentedfilesystem.model.OutOfMoneyPacket;

public class FileRetrieverTest {
    private static final String SERVER_NAME = "localhost";
    private static final int PORT = 8080;

    private static final File FILE_1 = new File(
        "file1.txt",
        "Some text\nSome more text\nThe last bit of text".getBytes(StandardCharsets.UTF_8)
    );

    private static final List<OutOfMoneyPacket> FILE_1_PACKETS = List.of(
        new OutOfMoneyHeaderPacket((byte) 1, "file1.txt".getBytes(StandardCharsets.UTF_8)),
        new OutOfMoneyDataPacket((byte) 1, "Some text\n".getBytes(StandardCharsets.UTF_8), 0, false),
        new OutOfMoneyDataPacket((byte) 1, "Some more text\n".getBytes(StandardCharsets.UTF_8), 1, false),
        new OutOfMoneyDataPacket((byte) 1, "The last bit of text".getBytes(StandardCharsets.UTF_8), 2, true)
    );

    private static final File FILE_2 = new File(
        "file2.bin",
        new byte[] { 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, -100 }
    );

    private static final List<OutOfMoneyPacket> FILE_2_PACKETS = List.of(
        new OutOfMoneyHeaderPacket((byte) 2, "file2.bin".getBytes(StandardCharsets.UTF_8)),
        new OutOfMoneyDataPacket((byte) 2, new byte[] { 0, 1, 1 }, 0, false),
        new OutOfMoneyDataPacket((byte) 2, new byte[] { 2, 3, 5 }, 1, false),
        new OutOfMoneyDataPacket((byte) 2, new byte[] { 8, 13, 21 }, 2, false),
        new OutOfMoneyDataPacket((byte) 2, new byte[] { 34, 55, -100 }, 3, true)
    );

    private static final File FILE_3 = new File(
        "file3.café.résumé",
        new byte[0]
    );

    private static final List<OutOfMoneyPacket> FILE_3_PACKETS = List.of(
        new OutOfMoneyHeaderPacket((byte) -127, "file3.café.résumé".getBytes(StandardCharsets.UTF_8)),
        new OutOfMoneyDataPacket((byte) -127, new byte[0], 0, true)
    );

    private static final List<OutOfMoneyPacket> ALL_PACKETS_IN_ORDER =
        List.of(FILE_1_PACKETS, FILE_2_PACKETS, FILE_3_PACKETS)
            .stream()
            .flatMap(f -> f.stream())
            .collect(Collectors.toList());

    private static final List<OutOfMoneyPacket> ALL_PACKETS_OUT_OF_ORDER =
        List.of(
            FILE_1_PACKETS.subList(1, FILE_1_PACKETS.size()),
            FILE_2_PACKETS.subList(3, FILE_2_PACKETS.size()),
            FILE_3_PACKETS.subList(1, FILE_3_PACKETS.size()),
            FILE_1_PACKETS.subList(0, 1),
            FILE_2_PACKETS.subList(0, 3),
            FILE_3_PACKETS.subList(0, 1)
        ).stream()
            .flatMap(f -> f.stream())
            .collect(Collectors.toList());

    private static final List<OutOfMoneyPacket> INCOMPLETE_PACKETS =
        List.of(
            FILE_1_PACKETS.get(0),
            FILE_2_PACKETS.get(0),
            FILE_3_PACKETS.get(0),
            FILE_1_PACKETS.get(FILE_1_PACKETS.size() - 1),
            FILE_2_PACKETS.get(FILE_2_PACKETS.size() - 1),
            FILE_3_PACKETS.get(FILE_3_PACKETS.size() - 1)
        );


    @Spy
    private OutOfMoneyApiService outOfMoneyApiService;

    @Spy
    private FileWriterService fileWriterService;

    private FileRetriever fileRetriever;

    private AutoCloseable mockitoContext;

    @Before
    public void beforeEach() {
        mockitoContext = MockitoAnnotations.openMocks(this);

        fileRetriever = new FileRetriever(outOfMoneyApiService, fileWriterService);
        doNothing().when(fileWriterService).createFileInWorkingDirectory(any());
    }

    @After
    public void afterEach() throws Exception {
        mockitoContext.close();
    }

    @Test
    public void testCanCollectPackets() {
        setUpWithThesePackets(ALL_PACKETS_IN_ORDER);
        fileRetriever.downloadFiles(SERVER_NAME, PORT);
        verifyWroteAllThreeFiles();
    }

    @Test
    public void testCanCollectPacketsOutOfOrder() {
        setUpWithThesePackets(ALL_PACKETS_OUT_OF_ORDER);
        fileRetriever.downloadFiles(SERVER_NAME, PORT);
        verifyWroteAllThreeFiles();
    }

    @Test
    public void testKeepsTryingToReadPacketsUntilAllThreeFilesAreDownloaded() {
        setUpWithThesePackets(INCOMPLETE_PACKETS);
        assertThrows(NoMorePacketsException.class, () ->
            fileRetriever.downloadFiles(SERVER_NAME, PORT)
        );
    }

    /**
     * Mock `outOfMoneyApiService.getPacket()` so that the first call returns the first element of `packets`, the
     * second call returns the second element of `packets`, and so on.
     *
     * After the `packets` list is exhausted, any further calls to `outOfMoneyApiService.getPacket()` will throw a
     * NoMorePacketsException.
     */
    private void setUpWithThesePackets(List<OutOfMoneyPacket> packets) {
        doAnswer(new Answer<OutOfMoneyPacket>() {
            // Keep track of where we are in the list.
            int indexInList = 0;

            // For each invokation:
            @Override
            public OutOfMoneyPacket answer(InvocationOnMock invocation) {
                // Are there still more packets to send?
                if (indexInList < packets.size()) {
                    // If so, send along the next packet.
                    OutOfMoneyPacket nextPacket = packets.get(indexInList);
                    indexInList++;
                    return nextPacket;
                } else {
                    // Otherwise, throw an exception.
                    // (In production, getPacket() won't throw an exception, it'll just hang. But we're taking some
                    // liberties with getPacket() to make testing easier.)
                    throw new NoMorePacketsException();
                }
            }
        }).when(outOfMoneyApiService).getPacket(eq(SERVER_NAME), eq(PORT));
    }

    private void verifyWroteAllThreeFiles() {
        ArgumentCaptor<File> filesWritten = ArgumentCaptor.forClass(File.class);
        verify(fileWriterService, times(3)).createFileInWorkingDirectory(filesWritten.capture());

        assertThat(filesWritten.getAllValues()).containsExactlyInAnyOrder(FILE_1, FILE_2, FILE_3);
    }

    private static class NoMorePacketsException extends RuntimeException {}
}
