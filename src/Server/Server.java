package Server;

import Client.Communications.TCPRequestResponseChannel;
import GamePlay.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by erickchandra on 4/25/16.
 */
public class Server extends Thread {
    private static int currentLastPlayerId = -1;
    private static Game game = new Game();
    private static ServerSocket serverSocket;
    private final Socket clientSocket;
    private String udpIpAddress;
    private int udpPortNumber;
    private int playerId;
    private static JSONArray kpuProposalJSON = null;
    private static int voteDayNotDecidedCount;
    private boolean isWerewolf = false;
    private String username;
    private boolean aliveStatus = true;

    private static List<Server> serverList;

    private boolean leaveStatus = false;

    private TCPRequestResponseChannel communicator;

    public Server(Socket _clientSocket) {
        this.clientSocket = _clientSocket;
        start();
    }

    public int receiveJoin(String _ipAddr, int _portNo, String _username) {
        // receiveJoin will return integer >= 0 if the client successfully inserted become a player.
        // Returns -1 if username exists.
        // Returns -2 if game is running.

        if (game.isUsernameExists(_username)) {
            return -1;
        }
        else if (game.getStartedStatus()) {
            return -2;
        }
        else {
            game.clientConnect(new Player(_ipAddr, _portNo, ++currentLastPlayerId, _username, true));
            return currentLastPlayerId;
        }
    }

    public void receiveReady(String _ipAddr, int _portNo) {
        game.clientReady(_ipAddr, _portNo);
    }

    public int receiveLeaveGame(String _ipAddr, int _portNo) {
        // Returns 0: no error.
        // Returns 1: game is running.
        return game.clientLeave(_ipAddr, _portNo);
    }

    public HashSet getClientList() {
        return game.getPlayerConnected();
    }

    public void run() {
        System.out.println("New Thread Started.");
        do {
            JSONObject jsonObjectReceived = listen(clientSocket);
            System.out.println(jsonObjectReceived.toString());

            // [OK] PROTOCOL NO. 1
            if (jsonObjectReceived.get("method").equals("join")) {
                this.udpIpAddress = jsonObjectReceived.get("udp_address").toString();
                this.udpPortNumber = Integer.parseInt(jsonObjectReceived.get("udp_port").toString());
                this.username = jsonObjectReceived.get("username").toString();
                this.aliveStatus = true;
                this.playerId = this.receiveJoin(jsonObjectReceived.get("udp_address").toString(), Integer.parseInt(jsonObjectReceived.get("udp_port").toString()), jsonObjectReceived.get("username").toString());
                JSONObject jsonObjectSend = new JSONObject();
                jsonObjectSend.put("status", "ok");
                jsonObjectSend.put("player_id", this.playerId);
                send(clientSocket, jsonObjectSend.toString());

                // TEMP: THE ROLE
                if (this.playerId == 2 || this.playerId == 3) {
                    this.isWerewolf = true;
                }
                else {
                    this.isWerewolf = false;
                }

            }
            // PROTOCOL NO. 2 (+ PROTOCOL NO. 12 INCLUSIVE: START GAME) TODO: Random Werewolf player and START GAME
            else if (jsonObjectReceived.get("method").equals("ready")) {
                receiveReady(this.udpIpAddress, this.udpPortNumber);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", "ok");
                jsonObject.put("description", "waiting for other player to start");
                send(clientSocket, jsonObject.toString());
                if (game.getPlayerReady().size() == game.getPlayerConnected().size() && game.getPlayerReady().size() >= 6) {
                    game.startGame();

                    // Random player role

//                    boolean werewolfRole;
//                    int randomNum = (int) Math.random();
//                    if (randomNum == 0) {
//                        werewolfRole = false;
//                    }
//                    else {
//                        werewolfRole = true;
//                    }

                    // Assign role to Game List
                    for (Server server : serverList) {
                        if (this.isWerewolf) {
                            game.addWerewolf(new Player(this.udpIpAddress, this.udpPortNumber, this.playerId, this.username, this.aliveStatus));
                        }
                        else {
                            game.addCitizen(new Player(this.udpIpAddress, this.udpPortNumber, this.playerId, this.username, this.aliveStatus));
                        }
                    }

                    jsonObject = new JSONObject();
                    jsonObject.put("status", "ok");
                    jsonObject.put("description", "waiting for other player to start");
                    sendBroadcast(jsonObject.toString());
                }
            }
            // PROTOCOL NO. 3
            else if (jsonObjectReceived.get("method").equals("leave")) {
                if (receiveLeaveGame(this.udpIpAddress, this.udpPortNumber) == 0) {
                    JSONObject jsonObjectSend = new JSONObject();
                    jsonObjectSend.put("status", "ok");
                }
                else if (receiveLeaveGame(this.udpIpAddress, this.udpPortNumber) == 1) {
                    JSONObject jsonObjectSend = new JSONObject();
                    jsonObjectSend.put("status", "fail");
                    jsonObjectSend.put("description", "The game has started. You are not allowed to leave.");
                }
            }
            // PROTOCOL NO. 4 TODO: Insert Werewolf information ONLY FOR DEAD PLAYERS
            else if (jsonObjectReceived.get("method").equals("client_address")) {
                JSONObject playerConnectedJSONObject = new JSONObject();
                playerConnectedJSONObject.put("status", "ok");

                Player currentPlayerConnectedIterator;

                JSONArray playerConnectedJSONArray = new JSONArray();
                HashSet playerConnected = game.getPlayerConnected();
                Iterator<Player> iterator = playerConnected.iterator();
                while (iterator.hasNext()) {
                    JSONObject playerEachConnectedJSONObject = new JSONObject();
                    currentPlayerConnectedIterator = iterator.next();
                    playerEachConnectedJSONObject.put("player_id", currentPlayerConnectedIterator.getPlayerId());
                    playerEachConnectedJSONObject.put("is_alive", currentPlayerConnectedIterator.getAliveStatus());
                    playerEachConnectedJSONObject.put("address", currentPlayerConnectedIterator.getIpAddress());
                    playerEachConnectedJSONObject.put("port", currentPlayerConnectedIterator.getPortNumber());
                    playerEachConnectedJSONObject.put("username", currentPlayerConnectedIterator.getUsername());
                    playerConnectedJSONArray.add(playerEachConnectedJSONObject);
                }

                playerConnectedJSONObject.put("clients", playerConnectedJSONArray);
                playerConnectedJSONObject.put("description", "list of clients retrieved");

                System.out.println(playerConnectedJSONObject.toString());
                send(clientSocket, playerConnectedJSONObject.toString());
            }
            // [OK] PROTOCOL NO. 7: CLIENT ACCEPTED PROPOSAL (FOR KPU_ID) (+PROTOCOL NO. 12: KPU SELECTED)
            else if (jsonObjectReceived.get("method").equals("accepted_proposal")) {
                if (kpuProposalJSON == null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("kpu_id", jsonObjectReceived.get("kpu_id"));
                    jsonObject.put("count", 1);
                    kpuProposalJSON.add(jsonObject);
                }
                else {
                    // Iterate through JSON Array
                    Iterator iterator = kpuProposalJSON.iterator();
                    boolean found = false;
                    while (!found && iterator.hasNext()) {
                        JSONObject jsonObjectIterate = (JSONObject) iterator.next();
                        if (jsonObjectIterate.get(jsonObjectReceived.get("kpu_id")) != null) {
                            jsonObjectIterate.put("count", (Integer) jsonObjectIterate.get("count") + 1);
                            found = true;
                        }
                    }

                    // KPU ID not found
                    if (!found) {
                        JSONObject jsonObjectIterate = new JSONObject();
                        jsonObjectIterate.put("kpu_id", jsonObjectReceived.get("kpu_id"));
                        jsonObjectIterate.put("count", 1);
                        kpuProposalJSON.add(jsonObjectIterate);
                    }
                }

                // Iterate through kpuProposal JSON and Check condition if the KPU ID has reached quorum majority
                Iterator iterator = kpuProposalJSON.iterator();
                boolean found = false;
                while (!found && iterator.hasNext()) {
                    JSONObject jsonObjectIterate = (JSONObject) iterator.next();
                    if ((Integer) jsonObjectIterate.get("count") > (Integer) (game.getPlayerConnected().size() / 2) + 1) {
                        // Quorum reached, send KPU SELECTED
                        JSONObject jsonObjectSend = new JSONObject();
                        jsonObjectSend.put("method", "kpu_selected");
                        jsonObjectSend.put("kpu_id", jsonObjectIterate.get("kpu_id"));
                        sendBroadcast(jsonObjectSend.toString());
                        found = true;
                    }
                }


            }
            // PROTOCOL NO. 9: INFO WEREWOLF KILLED (PROTOCOL NO. 13 INCLUSIVE: Change phase)
            else if (jsonObjectReceived.get("method").equals("vote_result_werewolf")) {
                game.killPlayer((Integer) jsonObjectReceived.get("player_killed"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", "ok");
                jsonObject.put("description", "player killed");

                // Change phase
                game.changeDay();
                jsonObject = new JSONObject();
                jsonObject.put("method", "change_phase");
                jsonObject.put("time", game.getDayStatus() ? "day" : "night");
                jsonObject.put("description", "");
            }
            // PROTOCOL NO. 11: INFO CIVILIAN KILLED (PROTOCOL NO. 13 INCLUSIVE: Change phase)
            else if (jsonObjectReceived.get("method").equals("vote_result_civilian")) {
                game.killPlayer((Integer) jsonObjectReceived.get("player_killed"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", "ok");
                jsonObject.put("description", "player killed");

                // Change phase
                game.changeDay();
                jsonObject = new JSONObject();
                jsonObject.put("method", "change_phase");
                jsonObject.put("time", game.getDayStatus() ? "day" : "night");
                jsonObject.put("description", "");
            }
            else if (jsonObjectReceived.get("method").equals("vote_result")) { // Cannot decide
                if (game.getDayStatus() == false) { // Werewolf must vote again and again
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("method", "vote_now");
                    jsonObject.put("phase", "night");
//                    sendBroadcast()

                }
                else { // In the day time, civilian is only restricted to vote only max 2 times. Otherwise, change day

                }
            }
            // EXCEPTIONAL UNKNOWN PROTOCOL
            else {
                // Command not found. Wrong JSON request.
                JSONObject jsonObjectSend = new JSONObject();
                jsonObjectSend.put("status", "error");
                jsonObjectSend.put("description", "wrong request");
            }
        } while (!leaveStatus);
    }

    public static JSONObject listen(Socket socket) {
        String lineRead = "";
//        StringBuilder stringBuilder = new StringBuilder();
        try {
//            System.out.println("INSIDE");
//            InputStream inputStream = socket.getInputStream();
//            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
//            object = (Object) objectInputStream.readObject();
//            System.out.println("Server received: " + object.toString() + " from " + socket);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            while ((line = bufferedReader.readLine()) != null) {
//                stringBuilder.append(line);
//                if (line.equals("BYE")) {
//                    break;
//                }
//                System.out.println("Strbuilder: " + stringBuilder);
//            }
            if ((lineRead = bufferedReader.readLine()) != null) {
                System.out.println("Server received: " + lineRead + " from " + socket);
            }
//            socket.getInputStream().close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONParser parser = new JSONParser();
        JSONObject objectReceived = null;
        try {
            objectReceived = (JSONObject) parser.parse(lineRead);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return objectReceived;
    }

    public static boolean send(Socket socket, String sendStr) {
        try {
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(sendStr);
            bw.flush();
            System.out.println("Server sent: " + sendStr + " to " + socket);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Client " + socket + " has disconnected.");
            return false;
        }
        return true;
    }

    public static boolean sendBroadcast(String sendStr) {
        for (Server server : serverList) {
            try {
                OutputStream os = server.clientSocket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(sendStr);
                bw.flush();
                System.out.println("Server sent: " + sendStr + " to " + server.clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Client " + server.clientSocket + " has disconnected.");
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(8088);
            System.out.println(InetAddress.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            System.out.println("LISTENING NEW CONNECTION...");
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                System.out.println("ACCEPTED NEW CONNECTION from " + socket);
                serverList.add(new Server(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
