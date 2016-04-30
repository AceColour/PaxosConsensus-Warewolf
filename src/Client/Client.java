package Client;


import Client.Communications.TCPRequestResponseChannel;
import Client.Misc.ClientInfo;
import Client.Paxos.PaxosController;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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
            JSONObject joinResponse = new JSONObject(communicator.sendRequestAndGetResponse(joinRequest));

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
        JSONObject recv = communicator.getLastRequestDariSeberangSana();
        if(recv.get("method").equals("start")) {
            isDay = recv.get("time").equals("day");
            playerInfo.setRole(recv.get("role").toString());
            if(playerInfo.getRole().equals("werewolf")){
                friends = (ArrayList)recv.get("friends");
            }

            // Send back response to server
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            communicator.sendResponseKeSeberangSana(response);

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
            JSONObject leaveResponse = new JSONObject(communicator.sendRequestAndGetResponse(leaveRequest));

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
            JSONObject readyUpResponse = new JSONObject(communicator.sendRequestAndGetResponse(readyUpRequest));

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
            JSONObject listClientResponse = new JSONObject(communicator.sendRequestAndGetResponse(listClientRequest));

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

    public static void main(String [] args) throws IOException {
        Client client = new Client();

        try {
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
