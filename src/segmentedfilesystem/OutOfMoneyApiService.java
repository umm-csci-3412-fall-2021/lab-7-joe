package segmentedfilesystem;

import segmentedfilesystem.model.OutOfMoneyPacket;

/**
 * This class knows how to speak the OutOfMoney.com protocol. It reads raw UDP packets and converts them into
 * OutOfMoney.com packets.
 */
public class OutOfMoneyApiService {
    /**
     * Tell the server that we'd like some files.
     */
    public void sendRequest(String serverName, int port) {
        // TODO
    }

    /**
     * Read a packet from the server and return it.
     *
     * This method blocks until we receive a packet from the server, so make sure you call `sendRequest()` first, or
     * else the program will hang.
     */
    public OutOfMoneyPacket getPacket(String serverName, int port) {
        // TODO
        return null;
    }
}
