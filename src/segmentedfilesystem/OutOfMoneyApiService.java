package segmentedfilesystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.BitSet;

import segmentedfilesystem.model.OutOfMoneyDataPacket;
import segmentedfilesystem.model.OutOfMoneyHeaderPacket;
import segmentedfilesystem.model.OutOfMoneyPacket;

/**
 * This class knows how to speak the OutOfMoney.com protocol. It reads raw UDP packets and converts them into
 * OutOfMoney.com packets.
 */
public class OutOfMoneyApiService {
    // The largest possible size a UDP datagram can have in the OutOfMoney.com protocol, in bytes.
    private static final int MAX_PACKET_SIZE = 1028;

    private DatagramSocket socket;

    /**
     * Open a UDP socket, and tell the server that we'd like some files.
     */
    public void startInteraction(String serverName, int port) throws IOException {
        socket = new DatagramSocket();
        socket.send(new DatagramPacket(new byte[0], 0, InetAddress.getByName(serverName), port));
    }

    /**
     * Read a packet from the server and return it.
     *
     * Throws an `IllegalStateException` if there isn't a currently open UDP socket.
     */
    public OutOfMoneyPacket getPacket() throws IOException {
        if (socket == null) {
            throw new IllegalStateException("Trying to read a packet when there's no open UDP socket");
        }

        var datagram = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
        socket.receive(datagram);
        byte[] receivedData = Arrays.copyOfRange(datagram.getData(), 0, datagram.getLength());

        return deserialize(receivedData);
    }

    /**
     * Close the UDP socket.
     *
     * Throws an `IllegalStateException` if there isn't a currently open UDP socket.
     */
    public void endInteraction() {
        if (socket == null) {
            throw new IllegalStateException("Trying to close a UDP socket when there isn't one open");
        }

        socket.close();
    }

    /**
     * Deserialize an array of bytes--the wire representation of an OutOfMoney.com packet--into a packet object.
     *
     * Visible for testing.
     */
    OutOfMoneyPacket deserialize(byte[] bytes) {
        BitSet statusBits = BitSet.valueOf(Arrays.copyOfRange(bytes, 0, 1));
        if (statusBits.get(0)) {
            // It's a data packet.
            boolean isFinalPacket = statusBits.get(1);
            byte fileId = bytes[1];
            int packetNumber = 256 * Byte.toUnsignedInt(bytes[2]) + Byte.toUnsignedInt(bytes[3]);
            byte[] contents = Arrays.copyOfRange(bytes, 4, bytes.length);
            return new OutOfMoneyDataPacket(fileId, contents, packetNumber, isFinalPacket);
        } else {
            // It's a header packet.
            byte fileId = bytes[1];
            byte[] contents = Arrays.copyOfRange(bytes, 2, bytes.length);
            return new OutOfMoneyHeaderPacket(fileId, contents);
        }
    }
}
