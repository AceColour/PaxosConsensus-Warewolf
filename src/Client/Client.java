package Client;

import Misc.ClientInfo;
import Paxos.Acceptor;
import Paxos.Proposer;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    private InetSocketAddress serverAddress;
    private int numPlayer;
    private List<ClientInfo> listPlayer;
    private String role;

    public Client(){
        ui = new CommandLineUI();
        isKPU = false;
        numPlayer = 0;
    }

    public void join(){
        boolean retry_joining;
        do{
            serverAddress = ui.askServerAddress();
            playerInfo.setUsername(ui.askUsername());

            JSONObject server_join_response;
            SynchronousTCPJSONClient synchronousTCPJSONClient = new SynchronousTCPJSONClient(serverAddress);
            try {
                synchronousTCPJSONClient.connect();

                JSONObject join_request = new JSONObject();
                join_request.put("method","join");
                join_request.put("username",playerInfo.getUsername());
                server_join_response = synchronousTCPJSONClient.call(join_request);

                String status = (String) server_join_response.get("status");

                if (status==null){
                    ui.displayFailedServerJoin("connection failure: error response from server");
                    retry_joining = true;
                }else if (status.equals("ok")){
                    playerInfo.setPlayer_id( (Integer) server_join_response.get("player_id"));
                    if (playerInfo.getPlayer_id() < 0){
                        ui.displayFailedServerJoin("connection failure: error response from server");
                        retry_joining = true;
                    }else{
                        ui.displaySuccessfulServerJoin();
                        retry_joining = false;
                    }
                }else if (status.equals("fail")){
                    ui.displayFailedServerJoin("server refused: " + server_join_response.get("description"));
                    retry_joining=true;
                }else if (status.equals("error")){
                    ui.displayFailedServerJoin("error: " + server_join_response.get("description"));
                    retry_joining=true;
                }else{
                    ui.displayFailedServerJoin("connection failure: error response from server");
                    retry_joining = true;
                }
            } catch (IOException e) {
                ui.displayFailedServerJoin("connection failure" + e);
                retry_joining = true;
            } catch (ParseException e) {
                ui.displayFailedServerJoin("connection failure" + e);
                retry_joining = true;
            }
        }while (retry_joining);
    }

    public void run(){
        join();
        if(isReady && isStart) {

        }
    }

    public void leave() {
        boolean retry_leave = false;
        do{
            JSONObject server_leave_response;
            SynchronousTCPJSONClient synchronousTCPJSONClient = new SynchronousTCPJSONClient(serverAddress);
            try {
                synchronousTCPJSONClient.connect();

                JSONObject leave_request = new JSONObject();
                leave_request.put("method","leave");
                server_leave_response = synchronousTCPJSONClient.call(leave_request);

                String status = (String) server_leave_response.get("status");

                if (status==null){
                    ui.displayFailedLeave("connection failure: error response from server");
                    retry_leave = true;
                }else if (status.equals("ok")){
                    ui.displaySuccessfulLeave();
                    isReady = false;
                    isStart = false;
                }else if (status.equals("fail")){

                }else if (status.equals("error")){
                    ui.displayFailedLeave("error: " + server_leave_response.get("description"));
                    retry_leave=true;
                }else{
                    ui.displayFailedLeave("connection failure: error response from server");
                    retry_leave = true;
                }
            } catch (IOException e) {
                ui.displayFailedReadyUp("connection failure" + e);
                retry_leave = true;
            } catch (ParseException e) {
                ui.displayFailedReadyUp("connection failure" + e);
                retry_leave = true;
            }
        }while (retry_leave);
    }

    public void readyup() {
        boolean retry_readyup = false;
        do{
            JSONObject server_readyup_response;
            SynchronousTCPJSONClient synchronousTCPJSONClient = new SynchronousTCPJSONClient(serverAddress);
            try {
                synchronousTCPJSONClient.connect();

                JSONObject readyup_request = new JSONObject();
                readyup_request.put("method","ready");
                server_readyup_response = synchronousTCPJSONClient.call(readyup_request);

                String status = (String) server_readyup_response.get("status");

                if (status==null){
                    ui.displayFailedReadyUp("connection failure: error response from server");
                    retry_readyup = true;
                }else if (status.equals("ok")){
                    ui.displaySuccessfulReadyUp();
                    isReady = true;

                    // Wait until server send start response
                    String statusStart;

                }else if (status.equals("fail")){

                }else if (status.equals("error")){
                    ui.displayFailedReadyUp("error: " + server_readyup_response.get("description"));
                    retry_readyup=true;
                }else{
                    ui.displayFailedReadyUp("connection failure: error response from server");
                    retry_readyup = true;
                }
            } catch (IOException e) {
                ui.displayFailedReadyUp("connection failure" + e);
                retry_readyup = true;
            } catch (ParseException e) {
                ui.displayFailedReadyUp("connection failure" + e);
                retry_readyup = true;
            }
        }while (retry_readyup);
    }

    public void retrieveListClient() {
        boolean retry_retrievelist = false;
        do{
            JSONObject server_retrievelist_response;
            SynchronousTCPJSONClient synchronousTCPJSONClient = new SynchronousTCPJSONClient(serverAddress);
            try {
                synchronousTCPJSONClient.connect();

                JSONObject retrievelist_request = new JSONObject();
                retrievelist_request.put("method","ready");
                server_retrievelist_response = synchronousTCPJSONClient.call(retrievelist_request);

                String status = (String) server_retrievelist_response.get("status");

                if (status==null){
                    ui.displayFailedRetrieveList("connection failure: error response from server");
                    retry_retrievelist = true;
                }else if (status.equals("ok")){
                    ui.displaySuccessfulRetrieveList();
                    isReady = true;

                    listPlayer.clear();
                    //listPlayer.add()

                    JSONArray slideContent = (JSONArray) server_retrievelist_response.get("clients");
                    Iterator i = slideContent.iterator();

                    while (i.hasNext()) {
                        JSONObject clientJSON = (JSONObject) i.next();
                        listPlayer.add(new ClientInfo(
                                (Integer) clientJSON.get("player_id"),
                                (Integer) clientJSON.get("is_alive"),
                                InetAddress.getByName((String)clientJSON.get("address")),
                                (Integer) clientJSON.get("port"),
                                (String)clientJSON.get("username")
                        ));
                    }

                }else if (status.equals("fail")){

                }else if (status.equals("error")){
                    ui.displayFailedRetrieveList("error: " + server_retrievelist_response.get("description"));
                    retry_retrievelist=true;
                }else{
                    ui.displayFailedRetrieveList("connection failure: error response from server");
                    retry_retrievelist = true;
                }
            } catch (IOException e) {
                ui.displayFailedRetrieveList("connection failure" + e);
                retry_retrievelist = true;
            } catch (ParseException e) {
                ui.displayFailedRetrieveList("connection failure" + e);
                retry_retrievelist = true;
            }
        }while (retry_retrievelist);
    }

    public static void main(String [] args){
        Client client = new Client();

        client.run();
    }
}
