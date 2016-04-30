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
    boolean startedStatus = false;

    // Methods
    // Getter
    public HashSet getPlayerCitizenActiveList() {
        return playerCitizenActiveList;
    }

    public HashSet getPlayerWerewolfActiveList() {
        return playerWerewolfActiveList;
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
                this.playerCitizenDeadList.add(new Player(currentPlayerIterator.getIpAddress(), currentPlayerIterator.getPortNumber(), currentPlayerIterator.getPlayerId(), currentPlayerIterator.getPlayerRole()));
                found = true;
                iterator.remove();
            }
        }

        iterator = playerWerewolfActiveList.iterator();
        while (!found && iterator.hasNext()) {
            currentPlayerIterator = iterator.next();
            if (currentPlayerIterator.getPlayerId() == _playerId) {
                this.playerWerewolfDeadList.add(new Player(currentPlayerIterator.getIpAddress(), currentPlayerIterator.getPortNumber(), currentPlayerIterator.getPlayerId(), currentPlayerIterator.getPlayerRole()));
                found = true;
                iterator.remove();
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
        if (this.playerReady.size() == this.playerConnected.size()) {
            this.startGame();
        }
    }
}
