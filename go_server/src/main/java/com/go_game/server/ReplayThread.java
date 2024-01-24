package com.go_game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.go_game.server.db.DBManager;

import shared.messages.IndexSetMsg;
import shared.messages.ReplayFetchMsg;
import shared.messages.StringMsg;

// TODO: database
public class ReplayThread implements Runnable {
    private DBManager dbm;
    private ObjectOutputStream toPlayer;
    private ObjectInputStream fromPlayer;

    public ReplayThread(ObjectOutputStream toPlayer, ObjectInputStream fromPlayer) {
        this.dbm = new DBManager();
        this.toPlayer = toPlayer;
        this.fromPlayer = fromPlayer;
        
        Thread fred = new Thread(this);
        fred.start();
    }

    @Override
    public void run() {
        try {
            ResultSet rs = dbm.getAllReplays();

            List<List<String>> replayList = new ArrayList<>();

            while (rs.next()) {
                List<String> rowValues = new ArrayList<>();

                int columnCount = rs.getMetaData().getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    rowValues.add(rs.getString(i));
                }
                replayList.add(rowValues);
            }

            //? debug
            // for (List<String> row : replayList) {
            //     System.out.println(row);
            // }

            toPlayer.reset();
            toPlayer.writeObject(new ReplayFetchMsg(replayList));

            // wait for response
            Object selectMessage = fromPlayer.readObject();
        
            if (selectMessage.toString().contains("INDEX_SET")) {
                int replayID = ((IndexSetMsg) selectMessage).getIndex();
                String replay = dbm.getReplayString(replayID).getString("replay_string");

                toPlayer.reset();
                toPlayer.writeObject(new StringMsg(replay));

                rs.close();
                exit();
            }
            else if (selectMessage.toString().contains("CLOSE")) {
                rs.close();
                exit();
            } 
            else {
                rs.close();
                throw new IOException("invalid message recived");
            }

        } catch (SQLException | IOException | ClassNotFoundException e) {
            exit();
            e.printStackTrace();
        }
    }

    private void exit() {
        try {
            this.fromPlayer.close();
            this.toPlayer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
