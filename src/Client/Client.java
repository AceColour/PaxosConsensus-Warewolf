package Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by nim_13512501 on 4/23/16.
 */
public class Client {
    UI ui;

    InetSocketAddress serverAddress;
    String username;

    Integer player_id;
    public Client(){
        ui = new CommandLineUI();
    }

    public void join(){
        boolean retry_joining;
        do{
            serverAddress = ui.askServerAddress();
            username = ui.askUsername();

            JSONObject server_join_response;
            SynchronousTCPJSONClient synchronousTCPJSONClient = new SynchronousTCPJSONClient(serverAddress);
            try {
                synchronousTCPJSONClient.connect();

                JSONObject join_request = new JSONObject();
                join_request.put("method","join");
                join_request.put("username",username);
                server_join_response = synchronousTCPJSONClient.call(join_request);

                String status = (String) server_join_response.get("status");

                if (status==null){
                    ui.displayFailedServerJoin("connection failure: error response from server");
                    retry_joining = true;
                }else if (status.equals("ok")){
                    player_id = (Integer) server_join_response.get("player_id");
                    if (player_id==null){
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
    }

    public static void main(String [] args){
        Client client = new Client();

        client.run();
    }
}
