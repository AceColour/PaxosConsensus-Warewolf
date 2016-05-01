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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by erickchandra on 4/25/16.
 */
public class Server extends Thread {
    public static int currentLastPlayerId = -1;
    private static Game game = new Game();
    private static ServerSocket serverSocket;
    private final Socket clientSocket;
    private String udpIpAddress;
    private int udpPortNumber;
    private int playerId;

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

            if (jsonObjectReceived.get("method").equals("join")) {
                this.udpIpAddress = jsonObjectReceived.get("udp_address").toString();
                this.udpPortNumber = Integer.parseInt(jsonObjectReceived.get("udp_port").toString());
                this.playerId = this.receiveJoin(jsonObjectReceived.get("udp_address").toString(), Integer.parseInt(jsonObjectReceived.get("udp_port").toString()), jsonObjectReceived.get("username").toString());
                JSONObject jsonObjectSend = new JSONObject();
                jsonObjectSend.put("status", "ok");
                jsonObjectSend.put("player_id", this.playerId);
                send(clientSocket, jsonObjectSend.toString());
            }
            else if (jsonObjectReceived.get("method").equals("ready")) {
                receiveReady(this.udpIpAddress, this.udpPortNumber);
            }
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
            else {
                // Command not found. Wrong JSON request.
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
                new Server(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
