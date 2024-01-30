package shared.db;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import shared.messages.BoardStateMsg;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;


public class DBQueuer {
    private final DBManager dbManager;
    private final ConcurrentLinkedQueue<Runnable> queue;
    private final ExecutorService executorService;

    public DBQueuer(DBManager dbManager) {
        this.dbManager = dbManager;
        this.queue = new ConcurrentLinkedQueue<>();
        this.executorService = Executors.newSingleThreadExecutor();
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

    public void saveBoardStateMsg(BoardStateMsg msg) {
        queueOperation(() -> {
            try {
                byte[] serializedMsg = serializeBoardStateMsg(msg);
                dbManager.saveGameState(msg.getGameID(), msg.getMoveNumber(), serializedMsg);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private byte[] serializeBoardStateMsg(BoardStateMsg msg) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(msg);
            return baos.toByteArray();
        }
    }

    public List<BoardStateMsg> retrieveGameStates(int gameID) {
        try {
            List<byte[]> serializedStates = dbManager.getGameStates(gameID);
            return deserializeList(serializedStates);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<BoardStateMsg> deserializeList(List<byte[]> serializedList) {
        List<BoardStateMsg> deserializedList = new ArrayList<>();
        for (byte[] serialized : serializedList) {
            try {
                BoardStateMsg message = deserialize(serialized);
                deserializedList.add(message);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return deserializedList;
    }

    private BoardStateMsg deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (BoardStateMsg) ois.readObject();
        }
    }

    public int getHighestGameNumber() {

        try {
            return dbManager.getHighestGameNumber();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<Integer, Date> getAllGameIDs() {
        try {
            return dbManager.getAllGameIDsAndDates();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new HashMap<>() {
            
        };
    }

    public void dbClose() {
        dbManager.close();
    }

    
}
