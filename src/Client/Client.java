package Client;


import Client.Communications.TCPRequestResponseChannel;
import Client.Misc.ClientInfo;
import Client.Paxos.PaxosController;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nim_13512501 on 4/23/16.
 */
public class Client {
    private UI ui;
    private ClientInfo playerInfo;
    private Boolean isReady;
    private Boolean isStart;
    private Boolean isKPU;
    private Boolean isDay;

    //
    private ArrayList<ClientInfo> listPlayer = new ArrayList<ClientInfo>();

    //
    private ArrayList<String> friends = new ArrayList<String>();

    //
    private int daysCount;

    private int numPlayer;

    // Paxos Controller
    PaxosController paxosController;

    // TCP
    private InetSocketAddress serverAddress;

    // UDP
    private InetSocketAddress UDPAddress;

    private TCPRequestResponseChannel communicator;


    public Client(){
        // Initialization
        ui = new CommandLineUI();
        // playerInfo = new ClientInfo()
        isKPU = false;
        isDay = false;
        numPlayer = 0;
        daysCount = 0;
    }

    public void join() throws IOException {

        // Set username
        playerInfo.setUsername(ui.askUsername());

        // Variable to determine request whether ok or not
        Boolean retryRequest = false;

        // Create JSON Object for join request
        JSONObject joinRequest = new JSONObject();
        joinRequest.put("method", "join");
        joinRequest.put("username", playerInfo.getUsername());
        joinRequest.put("udp_address", UDPAddress.getAddress().toString());
        joinRequest.put("udp_port", UDPAddress.getPort());

        do {
            // Get JSON Object as join response from server
            JSONObject joinResponse = null;
            try {
                joinResponse = new JSONObject(communicator.sendRequestAndGetResponse(joinRequest));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Get status from response
            String status = joinResponse.get("status").toString();

            // Check status response from server
            if (status == null) {
                ui.displayFailedResponse("Join", "connection failure: error response from server");
                retryRequest = true;
            } else if(status.equals("ok")){
                ui.displaySuccessfulResponse("Join");
                playerInfo.setPlayerId(Integer.parseInt(joinResponse.get("player_id").toString()));
            } else if(status.equals("fail")) {
                ui.displayFailedResponse("Join", "connection failure: error response from server");
                retryRequest = true;
            } else if(status.equals("error")){
                ui.displayErrorResponse("Join", "error: " + joinResponse.get("description"));
                retryRequest = true;
            } else {
                ui.displayErrorResponse("Join", "error: error is undetermined");
                retryRequest = true;
            }
        }while (retryRequest); // while there is error or failed response, try send request again
    }

    public void start() {
        // listening to the port
        JSONObject recv = null;
        try {
            recv = communicator.getLastRequestDariSeberangSana();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(recv.get("method").equals("start")) {
            isDay = recv.get("time").equals("day");
            playerInfo.setRole(recv.get("role").toString());
            if(playerInfo.getRole().equals("werewolf")){
                friends = (ArrayList)recv.get("friends");
            }

            // Send back response to server
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            try {
                communicator.sendResponseKeSeberangSana(response);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        daysCount++;
        isDay = true;
    }

    public void run() throws IOException {
        // Get UDP Port
        try {
            UDPAddress = new InetSocketAddress(
                    InetAddress.getLocalHost().getHostAddress(),
                    ui.askPortUDP()
            );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Initializing TCP Channel for request and response
        try {
            communicator = new TCPRequestResponseChannel(
                    ui.askServerAddress().getAddress(),
                    ui.askServerAddress().getPort()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        join();
        if(isReady && isStart) {
            paxosController.run();
        }
    }

    public void leave() {
        // Variable to determine request whether ok or not
        Boolean retryRequest = false;

        // Create JSON Object for leave request
        JSONObject leaveRequest = new JSONObject();
        leaveRequest.put("method", "leave");

        do {
            // Get JSON Object as join response from server
            JSONObject leaveResponse = null;
            try {
                leaveResponse = new JSONObject(communicator.sendRequestAndGetResponse(leaveRequest));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Get status from response
            String status = leaveResponse.get("status").toString();

            // Check status response from server
            if (status == null) {
                ui.displayFailedResponse("Leave", "connection failure: error response from server");
                retryRequest = true;
            } else if(status.equals("ok")){
                ui.displaySuccessfulResponse("Leave");
                playerInfo.setIsAlive(0);
            } else if(status.equals("fail")) {
                ui.displayFailedResponse("Leave", "connection failure: error response from server");
                retryRequest = true;
            } else if(status.equals("error")){
                ui.displayErrorResponse("Leave", "error: " + leaveResponse.get("description"));
                retryRequest = true;
            } else {
                ui.displayErrorResponse("Leave", "error: error is undetermined");
                retryRequest = true;
            }
        }while (retryRequest); // while there is error or failed response, try send request again
    }

    public void readyUp() {
        // Variable to determine request whether ok or not
        Boolean retryRequest = false;

        // Create JSON Object for readyUp request
        JSONObject readyUpRequest = new JSONObject();
        readyUpRequest.put("method", "ready");

        do {
            // Get JSON Object as join response from server
            JSONObject readyUpResponse = null;
            try {
                readyUpResponse = new JSONObject(communicator.sendRequestAndGetResponse(readyUpRequest));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Get status from response
            String status = readyUpResponse.get("status").toString();

            // Check status response from server
            if (status == null) {
                ui.displayFailedResponse("Retrieve List Client", "connection failure: error response from server");
                retryRequest = true;
            } else if(status.equals("ok")){
                ui.displaySuccessfulResponse("Retrieve List Client");
                isReady = true;
            } else if(status.equals("fail")) {
                ui.displayFailedResponse("Retrieve List Client", "connection failure: error response from server");
                retryRequest = true;
            } else if(status.equals("error")){
                ui.displayErrorResponse("Retrieve List Client", "error: " + readyUpResponse.get("description"));
                retryRequest = true;
            } else {
                ui.displayErrorResponse("Retrieve List Client", "error: error is undetermined");
                retryRequest = true;
            }
        }while (retryRequest); // while there is error or failed response, try send request again
    }

    public void retrieveListClient() throws UnknownHostException {

        // Variable to determine request whether ok or not
        Boolean retryRequest = false;

        // Create JSON Object for listClient request
        JSONObject listClientRequest = new JSONObject();
        listClientRequest.put("method", "ready");

        do {
            // Get JSON Object as join response from server
            JSONObject listClientResponse = null;
            try {
                listClientResponse = new JSONObject(communicator.sendRequestAndGetResponse(listClientRequest));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Get status from response
            String status = listClientResponse.get("status").toString();

            // Check status response from server
            if (status == null) {
                ui.displayFailedResponse("Retrieve List Client", "connection failure: error response from server");
                retryRequest = true;
            } else if(status.equals("ok")){
                ui.displaySuccessfulResponse("Retrieve List Client");

                // Clear list player
                listPlayer.clear();

                // Iterating client's array
                JSONArray slideContent = (JSONArray) listClientResponse.get("clients");
                Iterator i = slideContent.iterator();

                while (i.hasNext()) {
                    JSONObject clientJSON = (JSONObject) i.next();

                    // Add client to list Player
                    listPlayer.add(new ClientInfo(
                            (Integer) clientJSON.get("player_id"),
                            (Integer) clientJSON.get("is_alive"),
                            InetAddress.getByName((String)clientJSON.get("address")),
                            (Integer) clientJSON.get("port"),
                            (String)clientJSON.get("username")
                    ));
                }

            } else if(status.equals("fail")) {
                ui.displayFailedResponse("Retrieve List Client", "connection failure: error response from server");
                retryRequest = true;
            } else if(status.equals("error")){
                ui.displayErrorResponse("Retrieve List Client", "error: " + listClientResponse.get("description"));
                retryRequest = true;
            } else {
                ui.displayErrorResponse("Retrieve List Client", "error: error is undetermined");
                retryRequest = true;
            }
        }while (retryRequest); // while there is error or failed response, try send request again
    }

    //TODO refactor ini
    public void play() throws IOException, InterruptedException {
        DatagramSocket datagramSocket = new DatagramSocket(UDPAddress.getPort());
        boolean gameOver = false;

        int kpu_id;

        do{
            //change phase
            boolean gotChangePhase = false;
            do {
                JSONObject message = communicator.getLastRequestDariSeberangSana();
                if (isMethodGameOver(message)){
                    gameOver = true;
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    communicator.sendResponseKeSeberangSana(response);
                }else if (message.get("method").equals("change_phase")){
                    isDay=message.get("phase").equals("day");
                    daysCount= Integer.parseInt(message.get("days").toString());
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    communicator.sendResponseKeSeberangSana(response);
                }else{
                    JSONObject response = new JSONObject();
                    response.put("status", "fail");
                    response.put("description", "client cannot conform");
                    communicator.sendResponseKeSeberangSana(response);
                }
            }while (!gotChangePhase);

            if (isDay) {
                //TODO show civilian killed if day and not gameover
                //TODO show werewolf/civilian killed if night and not gameover

                //run paxos if day and not gameover
                paxosController = new PaxosController(listPlayer, playerInfo.getPlayerId(), datagramSocket);
                paxosController.start();

                //tunggu perintah vote dari server
                //periksa lagi apakah perintah vote itu sebelum atau sesudah paxos
                boolean gotVoteCommand = false;
                do {
                    JSONObject message = communicator.getLastRequestDariSeberangSana();
                    if (isMethodGameOver(message)) {
                        gameOver = true;
                        JSONObject response = new JSONObject();
                        response.put("status", "ok");
                        communicator.sendResponseKeSeberangSana(response);
                    } else if (message.get("method").equals("vote_now")) {
                        isDay = message.get("phase").equals("day");
                        JSONObject response = new JSONObject();
                        response.put("status", "ok");
                        communicator.sendResponseKeSeberangSana(response);
                    } else {
                        JSONObject response = new JSONObject();
                        response.put("status", "fail");
                        response.put("description", "client cannot conform");
                        communicator.sendResponseKeSeberangSana(response);
                    }
                } while (!gotVoteCommand);

                paxosController.stopPaxos();


                if (isDay) {
                    //TODO show civilian killed if day and not gameover
                    //TODO show werewolf/civilian killed if night and not gameover

                    //run paxos if day and not gameover
                    paxosController = new PaxosController(listPlayer, playerInfo.getPlayerId(), datagramSocket);
                    paxosController.start();

                    //tunggu perintah vote dari server
                    //periksa lagi apakah perintah vote itu sebelum atau sesudah paxos
                    gotVoteCommand = false;
                    do {
                        JSONObject message = communicator.getLastRequestDariSeberangSana();
                        JSONObject response = new JSONObject();
                        if (isMethodGameOver(message)) {
                            gameOver = true;
                            response.put("status", "ok");
                            communicator.sendResponseKeSeberangSana(response);
                        } else if (message.get("method").equals("kpu_selected")) {
                            kpu_id = Integer.parseInt(message.get("kpu_id").toString());
                            response.put("status", "ok");
                            communicator.sendResponseKeSeberangSana(response);
                        } else {
                            response.put("status", "fail");
                            response.put("description", "client cannot conform");
                            communicator.sendResponseKeSeberangSana(response);
                        }
                    } while (!gotVoteCommand);
                }
            }
            //TODO run voting process
            if (!isDay && playerInfo.getRole().equals("werewolf")){
                //TODO vote as werewolf
            }else{
                //TODO vote sisanya
            }
        }while (!gameOver);

    }

    private boolean isMethodGameOver(JSONObject message){
        return message.get("method").equals("game_over");
    }

    public static void main(String [] args) throws IOException {
        Client client = new Client();

        try {
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
