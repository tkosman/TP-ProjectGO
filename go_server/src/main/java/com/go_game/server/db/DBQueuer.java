package com.go_game.server.db;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBQueuer {
    private final DBManager dbManager;
    private final ConcurrentLinkedQueue<Runnable> queue;
    private final ExecutorService executorService;

    public DBQueuer(DBManager dbManager) {
        this.dbManager = dbManager;
        this.queue = new ConcurrentLinkedQueue<>();
        this.executorService = Executors.newSingleThreadExecutor(); // Single thread to process the queue
        processQueue();
    }

    private void processQueue() {
        executorService.submit(() -> {
            while (true) {
                Runnable task = queue.poll();
                if (task != null) {
                    task.run();
                }
            }
        });
    }

    public void queueOperation(Runnable dbOperation) {
        queue.add(dbOperation);
    }

    // Proxy method for DBManager's getAllReplays
    public void getAllReplays() throws SQLException {
        queueOperation(() -> {
            try {
                dbManager.getAllReplays();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // Proxy method for DBManager's getReplayString
    public void getReplayString(int id) {
        queueOperation(() -> {
            try {
                dbManager.getReplayString(id);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // Additional methods for other database operations can be added similarly
}
