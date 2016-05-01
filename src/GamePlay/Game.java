package GamePlay;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by erickchandra on 4/30/16.
 */
public class Game extends Thread{
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

    // Other methods
    public void addCitizen(Player _player) {
        this.playerCitizenActiveList.add(_player);
    }

    public void addWerewolf(Player _player) {
        this.playerWerewolfActiveList.add(_player);
    }

    private Object killPlayerRequestReceivedLock = new Object();
    private boolean adaPlayerDibunuh;

    public void killPlayer(int _playerId) {
        boolean found = false;
        Iterator<Player> iterator = playerCitizenActiveList.iterator();
        Player currentPlayerIterator;
        while (!found && iterator.hasNext()) {
            currentPlayerIterator = iterator.next();
            if (currentPlayerIterator.getPlayerId() == _playerId) {
                this.playerCitizenDeadList.add(currentPlayerIterator);
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
                        adaPlayerDibunuh = true;
                    }
                }
            }
        }

        iterator = playerWerewolfActiveList.iterator();
        while (!found && iterator.hasNext()) {
            currentPlayerIterator = iterator.next();
            if (currentPlayerIterator.getPlayerId() == _playerId) {
                this.playerWerewolfDeadList.add(currentPlayerIterator);
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
                        adaPlayerDibunuh = true;
                    }
                }
            }
        }

        killPlayerRequestReceivedLock.notify();
    }

    public void waitkillPlayerRequestReceived(){
        try {
            killPlayerRequestReceivedLock.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int quorumSize(){
        return (playerConnected.size()-2)/2+1;
    }

    public void startGame() {
        this.startedStatus = true;
        assignRoles();
        learner = new Learner(quorumSize());
        sendStartGameBroadcast();
    }

    public synchronized void clientConnect(Player _player) {
        this.playerConnected.add(_player);
    }

    public synchronized void clientReady(String _ipAddr, int _portNo) {
        Iterator<Player> iteratorPlayerConnected = playerConnected.iterator();
        Player currentPlayerConnectedIterator;
        boolean foundPlayerConnected = false;
        while (!foundPlayerConnected && iteratorPlayerConnected.hasNext()) {
            currentPlayerConnectedIterator = iteratorPlayerConnected.next();
            if (currentPlayerConnectedIterator.getIpAddress().equals(_ipAddr) && currentPlayerConnectedIterator.getPortNumber() == _portNo) {
                this.playerReady.add(currentPlayerConnectedIterator);
                foundPlayerConnected = true;
            }
        }
    }

    public synchronized int clientLeave(String _ipAddr, int _portNo) {
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
                    this.playerLeft.add(iterator);

                    iterator.remove();

                    //also search from playerReady
                    if (playerReady.contains(currentPlayerIterator))
                        playerReady.remove(currentPlayerIterator);

                    found = true;
                }
            }


            return 0;
        }
    }

    public boolean isPlayerReadyEqualsPlayerConnected() {
        return (this.playerReady.size() == this.playerConnected.size());
    }

    public boolean startCondition(){
        return playerConnected.size() >=6 && isPlayerReadyEqualsPlayerConnected();
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

    public void sendStartGameBroadcast() {

        for (Object playerObject : playerConnected) {
            Player player = (Player) playerObject;
            JSONObject request = new JSONObject();
            request.put("method", "start");
            request.put("time", "night");
            request.put("role", player.getRole());
            if (player.getRole().equals("werewolf")) {
                JSONArray jsonArray = new JSONArray();
                Iterator<Player> iterator = playerWerewolfActiveList.iterator();
                Player playerIterator;
                while (iterator.hasNext()) {
                    playerIterator = iterator.next();
                    if (!playerIterator.getUsername().equals(player.getUsername())) {
                        jsonArray.add(playerIterator.getUsername());
                    }
                }
                request.put("friends", jsonArray);
            }
            request.put("description", "game is started");

            try {
                JSONObject response = player.getCommunicator().sendRequestAndGetResponse(request);
                System.out.println("send start game request to player " + player.getPlayerId() + ". response: ");
                System.out.println(response.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void assignRoles(){
        int jatahWerewolf = 2;
        int jatahCivilian = getPlayerConnected().size() - jatahWerewolf;

        Random random = new Random();

        for (Object playerObject : playerConnected){
            String role = null;

            Player player = (Player) playerObject;

            if (jatahWerewolf > 0 && jatahCivilian > 0){
                int randomNumber = random.nextInt(2);
                if (randomNumber==0){
                    role = "werewolf";
                    addWerewolf(player);
                    jatahWerewolf--;
                }else{
                    role = "civilian";
                    addCitizen(player);
                    jatahCivilian--;
                }
            }else if (jatahWerewolf > 0){
                role = "werewolf";
                addWerewolf(player);
                jatahWerewolf--;
            }else if (jatahCivilian > 0){
                role = "civilian";
                addCitizen(player);
                jatahCivilian--;
            }

            player.setRole(role);
        }
    }

    public void accepted(int fromUid, int acceptedValue){
        learner.receiveAccepted(fromUid, acceptedValue);
    }

    Learner learner;

    public void waitKPUId(){
        try {
            int kpu_id = learner.waitFinalValue();
            sendKPUIdBroadcast(kpu_id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendKPUIdBroadcast(int kpu_id){
        JSONObject request = new JSONObject();
        request.put("method","kpu_selected");
        request.put("kpu_id",kpu_id);

        for (Object playerObject : playerConnected){
            Player player = (Player) playerObject;

            try {
                JSONObject response = player.getCommunicator().sendRequestAndGetResponse(request);
                System.out.println("send kpu_id " + kpu_id + " to player " + player.getPlayerId() + " . response: ");
                System.out.println(response);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitNumPlayersReady(){

        System.out.println("waiting players");

        while (!startCondition()){
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run(){
        waitNumPlayersReady();
        startGame();
        waitKPUId();
        voteNow();
        waitkillPlayerRequestReceived();
        if (!adaPlayerDibunuh){
            voteNow();
            waitkillPlayerRequestReceived();
        }

    }

    private void voteNow() {
        JSONObject request = new JSONObject();
        request.put("method","vote_now");
        request.put("phase",dayStatus?"day":"night");

        for (Object playerObject : playerConnected){
            Player player = (Player) playerObject;

            try {
                JSONObject response = player.getCommunicator().sendRequestAndGetResponse(request);
                System.out.println("send vote_now to player " + player.getPlayerId() + " . response: ");
                System.out.println(response);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
