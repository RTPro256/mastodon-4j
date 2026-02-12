package org.joinmastodon.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.stereotype.Service;

@Service
public class PostgresNotificationListener {
    private final DataSource dataSource;
    private final StreamingHub streamingHub;
    private final ObjectMapper objectMapper;
    private final Set<String> channels = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "pg-notify-listener");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean running = true;
    private Connection connection;
    private PGConnection pgConnection;

    public PostgresNotificationListener(DataSource dataSource, StreamingHub streamingHub, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.streamingHub = streamingHub;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        executor.submit(this::listenLoop);
    }

    public void ensureListening(String channel) {
        String safeChannel = sanitizeChannel(channel);
        if (safeChannel.isBlank()) {
            return;
        }
        if (!channels.add(safeChannel)) {
            return;
        }
        executeListen(safeChannel);
    }

    private void listenLoop() {
        while (running) {
            try {
                ensureConnection();
                PGNotification[] notifications = pgConnection.getNotifications();
                if (notifications != null) {
                    for (PGNotification notification : notifications) {
                        handleNotification(notification);
                    }
                }
                Thread.sleep(250);
            } catch (Exception ex) {
                closeConnection();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        closeConnection();
    }

    private void handleNotification(PGNotification notification) {
        if (notification == null) {
            return;
        }
        String channel = notification.getName();
        String payload = notification.getParameter();
        try {
            StreamEvent event = objectMapper.readValue(payload, StreamEvent.class);
            streamingHub.publish(channel, event);
        } catch (Exception ignored) {
        }
    }

    private void ensureConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        pgConnection = connection.unwrap(PGConnection.class);
        for (String channel : channels) {
            executeListen(channel);
        }
    }

    private void executeListen(String channel) {
        try {
            ensureConnection();
            try (Statement statement = connection.createStatement()) {
                statement.execute("LISTEN " + channel);
            }
        } catch (SQLException ignored) {
        }
    }

    private String sanitizeChannel(String channel) {
        if (channel == null) {
            return "";
        }
        return channel.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        executor.shutdownNow();
        closeConnection();
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            connection = null;
            pgConnection = null;
        }
    }
}
