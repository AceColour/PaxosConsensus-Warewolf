package GamePlay;

import java.util.HashSet;
import java.util.Iterator;

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

    public boolean getStartedStatus() {
        return startedStatus;
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

    public void clientReady(Player _player) {
        this.playerReady.add(_player);
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
}
