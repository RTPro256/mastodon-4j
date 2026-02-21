package org.joinmastodon.torrent.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * UDP tracker client implementing BEP 15 (UDP Tracker Protocol).
 * 
 * The UDP tracker protocol is more efficient than HTTP because:
 * - Lower overhead (no HTTP headers)
 * - Better for high-frequency announces
 * - Supports connection reuse
 * 
 * Protocol flow:
 * 1. Connect: Client sends connect request, receives connection_id
 * 2. Announce: Client sends announce with connection_id, receives peers
 * 3. Scrape: Client can request statistics for multiple torrents
 */
public class UdpTrackerClient extends TrackerClient {

    private static final Logger log = LoggerFactory.getLogger(UdpTrackerClient.class);

    // UDP tracker protocol magic number
    private static final long PROTOCOL_ID = 0x41727101980L;

    // Action codes
    private static final int ACTION_CONNECT = 0;
    private static final int ACTION_ANNOUNCE = 1;
    private static final int ACTION_SCRAPE = 2;
    private static final int ACTION_ERROR = 3;

    private final Random random;

    public UdpTrackerClient() {
        super();
        this.random = new SecureRandom();
    }

    public UdpTrackerClient(int timeoutMs, int retryCount) {
        super(timeoutMs, retryCount);
        this.random = new SecureRandom();
    }

    @Override
    public CompletableFuture<TrackerAnnounceResult> announce(
            String trackerUrl,
            byte[] infoHash,
            byte[] peerId,
            int port,
            long uploaded,
            long downloaded,
            long left,
            AnnounceEvent event) {

        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                // Parse tracker URL
                PublicTrackerList.TrackerUrl url = PublicTrackerList.parseTrackerUrl(trackerUrl);
                InetAddress address = InetAddress.getByName(url.host());
                InetSocketAddress socketAddress = new InetSocketAddress(address, url.port());

                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(timeoutMs);

                    // Step 1: Connect and get connection_id
                    long connectionId = connect(socket, socketAddress);
                    log.debug("Connected to UDP tracker {}: connection_id={}", trackerUrl, connectionId);

                    // Step 2: Announce
                    TrackerAnnounceResult result = doAnnounce(
                            socket, socketAddress, connectionId,
                            infoHash, peerId, port, uploaded, downloaded, left, event,
                            System.currentTimeMillis() - startTime
                    );

                    log.debug("Announced to {}: {} peers, {} seeders, {} leechers",
                            trackerUrl, result.getPeers().size(), result.getSeeders(), result.getLeechers());

                    return result;
                }
            } catch (Exception e) {
                long elapsed = System.currentTimeMillis() - startTime;
                log.warn("Failed to announce to UDP tracker {}: {}", trackerUrl, e.getMessage());
                return TrackerAnnounceResult.failure(trackerUrl, e.getMessage(), elapsed);
            }
        });
    }

    @Override
    public CompletableFuture<ScrapeResult> scrape(String trackerUrl, byte[]... infoHashes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PublicTrackerList.TrackerUrl url = PublicTrackerList.parseTrackerUrl(trackerUrl);
                InetAddress address = InetAddress.getByName(url.host());
                InetSocketAddress socketAddress = new InetSocketAddress(address, url.port());

                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(timeoutMs);

                    // Connect
                    long connectionId = connect(socket, socketAddress);

                    // Scrape
                    return doScrape(socket, socketAddress, connectionId, infoHashes);
                }
            } catch (Exception e) {
                log.warn("Failed to scrape UDP tracker {}: {}", trackerUrl, e.getMessage());
                return new ScrapeResult(trackerUrl, ScrapeResult.Status.FAILED, e.getMessage(), List.of());
            }
        });
    }

    @Override
    public boolean supports(String trackerUrl) {
        return PublicTrackerList.isUdpTracker(trackerUrl);
    }

    /**
     * Send a connect request and receive connection_id.
     */
    private long connect(DatagramSocket socket, InetSocketAddress address) throws Exception {
        int transactionId = random.nextInt();

        // Build connect request
        ByteBuffer request = ByteBuffer.allocate(16);
        request.order(ByteOrder.BIG_ENDIAN);
        request.putLong(PROTOCOL_ID);
        request.putInt(ACTION_CONNECT);
        request.putInt(transactionId);

        // Send request
        byte[] requestData = request.array();
        DatagramPacket packet = new DatagramPacket(requestData, requestData.length, address);
        socket.send(packet);

        // Receive response
        byte[] responseData = new byte[16];
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);
        socket.receive(responsePacket);

        // Parse response
        ByteBuffer response = ByteBuffer.wrap(responsePacket.getData(), 0, responsePacket.getLength());
        response.order(ByteOrder.BIG_ENDIAN);

        int action = response.getInt();
        int receivedTransactionId = response.getInt();

        if (action == ACTION_ERROR) {
            byte[] errorMsg = new byte[response.remaining()];
            response.get(errorMsg);
            throw new RuntimeException("Tracker error: " + new String(errorMsg));
        }

        if (action != ACTION_CONNECT) {
            throw new RuntimeException("Unexpected action in connect response: " + action);
        }

        if (receivedTransactionId != transactionId) {
            throw new RuntimeException("Transaction ID mismatch");
        }

        return response.getLong();
    }

    /**
     * Send an announce request.
     */
    private TrackerAnnounceResult doAnnounce(
            DatagramSocket socket,
            InetSocketAddress address,
            long connectionId,
            byte[] infoHash,
            byte[] peerId,
            int port,
            long uploaded,
            long downloaded,
            long left,
            AnnounceEvent event,
            long elapsedMs) throws Exception {

        int transactionId = random.nextInt();

        // Build announce request
        ByteBuffer request = ByteBuffer.allocate(98);
        request.order(ByteOrder.BIG_ENDIAN);

        // Connection ID (8 bytes)
        request.putLong(connectionId);
        // Action (4 bytes)
        request.putInt(ACTION_ANNOUNCE);
        // Transaction ID (4 bytes)
        request.putInt(transactionId);
        // Info Hash (20 bytes)
        request.put(infoHash);
        // Peer ID (20 bytes)
        request.put(peerId);
        // Downloaded (8 bytes)
        request.putLong(downloaded);
        // Left (8 bytes)
        request.putLong(left);
        // Uploaded (8 bytes)
        request.putLong(uploaded);
        // Event (4 bytes)
        request.putInt(event.getCode());
        // IP Address (4 bytes) - 0 for automatic
        request.putInt(0);
        // Key (4 bytes)
        request.putInt(random.nextInt());
        // Num Want (4 bytes) - -1 for default
        request.putInt(-1);
        // Port (2 bytes)
        request.putShort((short) port);

        // Send request
        byte[] requestData = new byte[request.position()];
        request.position(0);
        request.get(requestData);
        DatagramPacket packet = new DatagramPacket(requestData, requestData.length, address);
        socket.send(packet);

        // Receive response
        byte[] responseData = new byte[2048];
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);
        socket.receive(responsePacket);

        // Parse response
        ByteBuffer response = ByteBuffer.wrap(responsePacket.getData(), 0, responsePacket.getLength());
        response.order(ByteOrder.BIG_ENDIAN);

        int action = response.getInt();
        int receivedTransactionId = response.getInt();

        if (action == ACTION_ERROR) {
            byte[] errorMsg = new byte[response.remaining()];
            response.get(errorMsg);
            return TrackerAnnounceResult.failure(address.toString(),
                    "Tracker error: " + new String(errorMsg), elapsedMs);
        }

        if (action != ACTION_ANNOUNCE) {
            return TrackerAnnounceResult.failure(address.toString(),
                    "Unexpected action: " + action, elapsedMs);
        }

        if (receivedTransactionId != transactionId) {
            return TrackerAnnounceResult.failure(address.toString(),
                    "Transaction ID mismatch", elapsedMs);
        }

        // Parse announce response
        int interval = response.getInt();
        int leechers = response.getInt();
        int seeders = response.getInt();

        // Parse peers (6 bytes each: 4 IP + 2 port)
        List<TrackerAnnounceResult.PeerInfo> peers = new ArrayList<>();
        while (response.remaining() >= 6) {
            byte[] ip = new byte[4];
            response.get(ip);
            String ipStr = String.format("%d.%d.%d.%d",
                    ip[0] & 0xFF, ip[1] & 0xFF, ip[2] & 0xFF, ip[3] & 0xFF);
            int peerPort = response.getShort() & 0xFFFF;
            peers.add(new TrackerAnnounceResult.PeerInfo(ipStr, peerPort));
        }

        return TrackerAnnounceResult.success(
                address.toString(), seeders, leechers, interval, peers, elapsedMs);
    }

    /**
     * Send a scrape request.
     */
    private ScrapeResult doScrape(
            DatagramSocket socket,
            InetSocketAddress address,
            long connectionId,
            byte[][] infoHashes) throws Exception {

        int transactionId = random.nextInt();

        // Build scrape request
        ByteBuffer request = ByteBuffer.allocate(16 + infoHashes.length * 20);
        request.order(ByteOrder.BIG_ENDIAN);

        // Connection ID
        request.putLong(connectionId);
        // Action
        request.putInt(ACTION_SCRAPE);
        // Transaction ID
        request.putInt(transactionId);
        // Info hashes
        for (byte[] infoHash : infoHashes) {
            request.put(infoHash);
        }

        // Send request
        byte[] requestData = new byte[request.position()];
        request.position(0);
        request.get(requestData);
        DatagramPacket packet = new DatagramPacket(requestData, requestData.length, address);
        socket.send(packet);

        // Receive response
        byte[] responseData = new byte[2048];
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);
        socket.receive(responsePacket);

        // Parse response
        ByteBuffer response = ByteBuffer.wrap(responsePacket.getData(), 0, responsePacket.getLength());
        response.order(ByteOrder.BIG_ENDIAN);

        int action = response.getInt();
        int receivedTransactionId = response.getInt();

        if (action == ACTION_ERROR) {
            byte[] errorMsg = new byte[response.remaining()];
            response.get(errorMsg);
            return new ScrapeResult(address.toString(), ScrapeResult.Status.FAILED,
                    new String(errorMsg), List.of());
        }

        if (action != ACTION_SCRAPE) {
            return new ScrapeResult(address.toString(), ScrapeResult.Status.FAILED,
                    "Unexpected action: " + action, List.of());
        }

        if (receivedTransactionId != transactionId) {
            return new ScrapeResult(address.toString(), ScrapeResult.Status.FAILED,
                    "Transaction ID mismatch", List.of());
        }

        // Parse scrape entries (12 bytes each)
        List<ScrapeEntry> entries = new ArrayList<>();
        for (byte[] infoHash : infoHashes) {
            if (response.remaining() >= 12) {
                int seeders = response.getInt();
                int completed = response.getInt();
                int leechers = response.getInt();
                entries.add(new ScrapeEntry(infoHash, seeders, completed, leechers));
            }
        }

        return new ScrapeResult(address.toString(), ScrapeResult.Status.SUCCESS, null, entries);
    }
}
