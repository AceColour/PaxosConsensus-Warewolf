package GamePlay;

import Server.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by erickchandra on 4/30/16.
 */
public class Game {
    // Attributes
    private HashSet playerCitizenActiveList = new HashSet();
    private HashSet playerWerewolfActiveList = new HashSet();
    private HashSet playerCitizenDeadList = new HashSet();
    private HashSet playerWerewolfDeadList = new HashSet();
    private HashSet playerConnected = new HashSet();
    private HashSet playerReady = new HashSet();
    private HashSet playerLeft = new HashSet();
    boolean startedStatus = false;
    boolean dayStatus = false; // True: Day; False: Night.
    int dayCount = 0;

    private List<Server> serverList;

    // Methods
    // Getter
    public HashSet getPlayerCitizenActiveList() {
        return playerCitizenActiveList;
    }

    public HashSet getPlayerWerewolfActiveList() {
        return playerWerewolfActiveList;
    }

    public HashSet getPlayerConnected() {
        return playerConnected;
    }

    public HashSet getPlayerReady() {
        return playerReady;
    }

    public boolean getStartedStatus() {
        return startedStatus;
    }

    public boolean getDayStatus() {
        return dayStatus;
    }

    public int getDayCount() {
        return dayCount;
    }

    public List<Server> getServerList() {
        return serverList;
    }

    public void addServerList(Server server) {
        serverList.add(server);
    }

    // Other methods
    public void addCitizen(Player _player) {
        this.playerCitizenActiveList.add(_player);
    }

    public void addWerewolf(Player _player) {
        this.playerWerewolfActiveList.add(_player);
    }

    public void killPlayer(int _playerId) {
        boolean found = false;
        Iterator<Player> iterator = playerCitizenActiveList.iterator();
        Player currentPlayerIterator;
        while (!found && iterator.hasNext()) {
            currentPlayerIterator = iterator.next();
            if (currentPlayerIterator.getPlayerId() == _playerId) {
                this.playerCitizenDeadList.add(new Player(currentPlayerIterator.getIpAddress(), currentPlayerIterator.getPortNumber(), currentPlayerIterator.getPlayerId(), currentPlayerIterator.getUsername(), false));
                found = true;
                iterator.remove();

                // Also set aliveStatus in playerConnected.
                Iterator<Player> iteratorPlayerConnected = playerConnected.iterator();
                Player currentPlayerConnectedIterator;
                boolean foundPlayerConnected = false;
                while (foundPlayerConnected && iteratorPlayerConnected.hasNext()) {
                    currentPlayerConnectedIterator = iteratorPlayerConnected.next();
                    if (currentPlayerConnectedIterator.getPlayerId() == _playerId) {
                        currentPlayerConnectedIterator.setAliveStatus(false);
                    }
                }
            }
        }

        iterator = playerWerewolfActiveList.iterator();
        while (!found && iterator.hasNext()) {
            currentPlayerIterator = iterator.next();
            if (currentPlayerIterator.getPlayerId() == _playerId) {
                this.playerWerewolfDeadList.add(new Player(currentPlayerIterator.getIpAddress(), currentPlayerIterator.getPortNumber(), currentPlayerIterator.getPlayerId(), currentPlayerIterator.getUsername(), false));
                found = true;
                iterator.remove();

                // Also set aliveStatus in playerConnected.
                Iterator<Player> iteratorPlayerConnected = playerConnected.iterator();
                Player currentPlayerConnectedIterator;
                boolean foundPlayerConnected = false;
                while (foundPlayerConnected && iteratorPlayerConnected.hasNext()) {
                    currentPlayerConnectedIterator = iteratorPlayerConnected.next();
                    if (currentPlayerConnectedIterator.getPlayerId() == _playerId) {
                        currentPlayerConnectedIterator.setAliveStatus(false);
                    }
                }
            }
        }
    }

    public void startGame() {
        this.startedStatus = true;
    }

    public void clientConnect(Player _player) {
        this.playerConnected.add(_player);
    }

    public void clientReady(String _ipAddr, int _portNo) {
        Iterator<Player> iteratorPlayerConnected = playerConnected.iterator();
        Player currentPlayerConnectedIterator;
        boolean foundPlayerConnected = false;
        while (foundPlayerConnected && iteratorPlayerConnected.hasNext()) {
            currentPlayerConnectedIterator = iteratorPlayerConnected.next();
            if (currentPlayerConnectedIterator.getIpAddress().equals(_ipAddr) && currentPlayerConnectedIterator.getPortNumber() == _portNo) {
                this.playerReady.add(new Player(currentPlayerConnectedIterator.getIpAddress(), currentPlayerConnectedIterator.getPortNumber(), currentPlayerConnectedIterator.getPlayerId(), currentPlayerConnectedIterator.getUsername(), true));
            }
        }
    }

    public int clientLeave(String _ipAddr, int _portNo) {
        // Returns 0: no error
        // Returns 1: game is playing

        Iterator<Player> iterator = playerConnected.iterator();
        Player currentPlayerIterator;
        boolean found = false;

        if (getStartedStatus()) {
            return 1;
        }
        else {
            while (!found && iterator.hasNext()) {
                currentPlayerIterator = iterator.next();
                if (currentPlayerIterator.getIpAddress().equals(_ipAddr) && currentPlayerIterator.getPortNumber() == _portNo) {
                    this.playerLeft.add(new Player(currentPlayerIterator.getIpAddress(), currentPlayerIterator.getPortNumber(), currentPlayerIterator.getPlayerId(), currentPlayerIterator.getUsername(), false));

                    iterator.remove();
                    found = true;
                }
            }

            return 0;
        }
    }

    public boolean isPlayerReadyEqualsPlayerConnected() {
        return (this.playerReady.size() == this.playerConnected.size());
    }

    public boolean isUsernameExists(String _username) {
        boolean found = false;
        Iterator<Player> iterator = playerConnected.iterator();
        Player currentPlayerIterator;

        while (!found && iterator.hasNext()) {
            currentPlayerIterator = iterator.next();
            if (currentPlayerIterator.getUsername().equals(_username)) {
                found = true;
            }
        }

        return found;
    }

    public boolean changeDay() {
        if (this.dayStatus == false) {
            dayCount++;
        }
        this.dayStatus = !this.dayStatus;
        return dayStatus;
    }

    public void sendBroadcast(String strSend) {
        for (Server server : serverList) {
            server.send(server.getClientSocket(), strSend);
        }
    }

    public void sendStartGameBroadcast() {

        for (Server server : serverList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("method", "start");
            jsonObject.put("time", "night");
            jsonObject.put("role", server.isWerewolf ? "werewolf" : "civilian");
            if (server.isWerewolf) {
                JSONArray jsonArray = new JSONArray();
                Iterator<Player> iterator = playerWerewolfActiveList.iterator();
                Player playerIterator;
                while (iterator.hasNext()) {
                    playerIterator = iterator.next();
                    if (!playerIterator.getUsername().equals(server.username)) {
                        jsonArray.add(playerIterator.getUsername());
                    }
                }
                jsonObject.put("friends", jsonArray);
            }
            else {
                jsonObject.put("friends", "");
            }
            jsonObject.put("description", "game is started");
        }
    }
}
