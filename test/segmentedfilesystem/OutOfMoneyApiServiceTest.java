package segmentedfilesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import segmentedfilesystem.model.OutOfMoneyDataPacket;
import segmentedfilesystem.model.OutOfMoneyHeaderPacket;
import segmentedfilesystem.model.OutOfMoneyPacket;

public class OutOfMoneyApiServiceTest {
    public OutOfMoneyApiService outOfMoneyApiService;

    @Before
    public void beforeEach() {
        outOfMoneyApiService = new OutOfMoneyApiService();
    }

    private static final byte[] HEADER_DATAGRAM = new byte[] { 0b00, 53, 'f', 'o', 'o', '.', 't', 'x', 't' };
    private static final byte[] DATA_DATAGRAM_NUMBER_0 = new byte[] { 0b01, 77, 0, 0, 'D', 'A', 'T', 'A' };
    private static final byte[] DATA_DATAGRAM_NUMBER_1 = new byte[] { 0b01, 77, 0, 1, -1, -2, -3, -4 };
    private static final byte[] DATA_DATAGRAM_NUMBER_2 = new byte[] { 0b01, 77, 0, 2 };
    private static final byte[] DATA_DATAGRAM_NUMBER_255 = new byte[] { 0b01, 77, 0, -1, 'a' };
    private static final byte[] DATA_DATAGRAM_NUMBER_1023 = new byte[] { 0b11, 77, 3, -1, 'a' };

    @Test
    public void testCanDeserializeHeaderPackets() {
        OutOfMoneyPacket packet = outOfMoneyApiService.deserialize(HEADER_DATAGRAM);
        assertTrue(packet.isHeaderPacket());

        var headerPacket = (OutOfMoneyHeaderPacket) packet;
        assertThat(headerPacket.getFileId()).isEqualTo((byte) 53);
        assertThat(headerPacket.getFilename()).isEqualTo("foo.txt");
    }

    @Test
    public void testCanDeserializeDataPackets() {
        OutOfMoneyPacket packet = outOfMoneyApiService.deserialize(DATA_DATAGRAM_NUMBER_0);
        assertFalse(packet.isHeaderPacket());

        var dataPacket = (OutOfMoneyDataPacket) packet;
        assertFalse(dataPacket.isFinalPacket());
        assertThat(dataPacket.getFileId()).isEqualTo((byte) 77);
        assertThat(dataPacket.getPacketNumber()).isEqualTo(0);
        assertThat(dataPacket.getContents()).containsExactly("DATA".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testCanDeserializeDataPacketsWithInvalidUtf8() {
        OutOfMoneyPacket packet = outOfMoneyApiService.deserialize(DATA_DATAGRAM_NUMBER_1);
        assertFalse(packet.isHeaderPacket());

        var dataPacket = (OutOfMoneyDataPacket) packet;
        assertFalse(dataPacket.isFinalPacket());
        assertThat(dataPacket.getFileId()).isEqualTo((byte) 77);
        assertThat(dataPacket.getPacketNumber()).isEqualTo(1);
        assertThat(dataPacket.getContents()).containsExactly(-1, -2, -3, -4);
    }

    @Test
    public void testCanDeserializeEmptyDataPackets() {
        OutOfMoneyPacket packet = outOfMoneyApiService.deserialize(DATA_DATAGRAM_NUMBER_2);
        assertFalse(packet.isHeaderPacket());

        var dataPacket = (OutOfMoneyDataPacket) packet;
        assertFalse(dataPacket.isFinalPacket());
        assertThat(dataPacket.getFileId()).isEqualTo((byte) 77);
        assertThat(dataPacket.getPacketNumber()).isEqualTo(2);
        assertThat(dataPacket.getContents()).isEmpty();
    }

    // This test case makes sure we don't have sign-bit-related errors.
    @Test
    public void testCanDeserializeDataPacketsWithPacketNumberGreaterThan128() {
        OutOfMoneyPacket packet = outOfMoneyApiService.deserialize(DATA_DATAGRAM_NUMBER_255);
        assertFalse(packet.isHeaderPacket());

        var dataPacket = (OutOfMoneyDataPacket) packet;
        assertFalse(dataPacket.isFinalPacket());
        assertThat(dataPacket.getFileId()).isEqualTo((byte) 77);
        assertThat(dataPacket.getPacketNumber()).isEqualTo(255);
        assertThat(dataPacket.getContents()).containsExactly('a');
    }

    // This test case makes sure that we're grouping two bytes together into a short properly.
    @Test
    public void testCanDeserializeDataPacketsWithPacketNumberGreaterThan255() {
        OutOfMoneyPacket packet = outOfMoneyApiService.deserialize(DATA_DATAGRAM_NUMBER_1023);
        assertFalse(packet.isHeaderPacket());

        var dataPacket = (OutOfMoneyDataPacket) packet;
        assertTrue(dataPacket.isFinalPacket());
        assertThat(dataPacket.getFileId()).isEqualTo((byte) 77);
        assertThat(dataPacket.getPacketNumber()).isEqualTo(1023);
        assertThat(dataPacket.getContents()).containsExactly('a');
    }
}
