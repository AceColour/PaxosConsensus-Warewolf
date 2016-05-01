package Client;

import Client.Communications.TCPRequestResponseChannel;
import Client.Misc.ClientInfo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mfikria on 01/05/2016.
 */
public class VoteListener extends Thread{
    List<ClientInfo> clientList;
    List<Integer> hasVotedId;
    CommandLineUI ui;
    int thisPlayerId;
    DatagramSocket datagramSocket;
    TCPRequestResponseChannel tcpRequestResponseChannel;
    Map<Integer,Integer> voteResult = new HashMap();
    Boolean isDay = false;
    /**
     *
     * @param clientList a clientlist received from the server
     * @param thisPlayerId this client's player ID
     * @param datagramSocket a bound datagramsocket for listening and sending messages
     * @throws SocketException
     */
    public VoteListener(List<ClientInfo> clientList, int thisPlayerId, DatagramSocket datagramSocket, Boolean isDay) throws SocketException {
        super();
        this.thisPlayerId = thisPlayerId;
        this.datagramSocket = datagramSocket;
        this.isDay = isDay;
        this.clientList = new ArrayList<ClientInfo>(clientList);
        this.hasVotedId = new ArrayList<Integer>();
        continueListening = true;
    }

    @Override
    public void run(){
        try {
            runSisanya();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private boolean continueListening;

    public void runSisanya() throws SocketException {

        byte [] buf = new byte[65507];
        DatagramPacket message = new DatagramPacket(buf, 65507);

        int originalTimeOut = datagramSocket.getSoTimeout();
        datagramSocket.setSoTimeout(10);

        while (continueListening){
            try {

                // Check whether it is day or night
                if(voteResult.size() >= clientList.size() && isDay){
                    continueListening = false;
                    break;
                }else if(voteResult.size() >= getRemainingWerewolf() && !isDay) {
                    continueListening = false;
                    break;
                }

                datagramSocket.receive(message);
                handleMessage(message);
            } catch (SocketTimeoutException e){
                //wait whether continueListening
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        datagramSocket.setSoTimeout(originalTimeOut);
    }

    public void stopListener(){
        continueListening = false;

    }

    public void handleMessage(DatagramPacket message){
        byte [] data = message.getData();
        int datalength = message.getLength();
        int dataoffset = message.getOffset();
        InetAddress address = message.getAddress();
        int port = message.getPort();

        //dapatkan UID
        int UID = getUIDFromPortAndInetAddress(address,port);
        if (UID==-1) // bukan dari client terdaftar. ignore
            return;

        String messageString = new String(data,dataoffset,datalength);

        JSONObject jsonObject;
        try{
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(messageString);
            int countVote = 0;

            if (jsonObject.containsKey("method")){
                if((jsonObject.get("method").equals("vote_werewolf") && !isDay)
                        || (jsonObject.get("method").equals("vote_civilian") && isDay)){

                    int playerId =  Integer.parseInt(jsonObject.get("player_id").toString());

                    Boolean isFound = false;
                    for (Map.Entry<Integer, Integer> entry : voteResult.entrySet()) {
                        if(entry.getKey() == playerId && !hasVotedId.contains(playerId)){
                            countVote = entry.getValue();
                            countVote++;
                            isFound = true;
                            hasVotedId.add(playerId);
                        }
                    }
                    if(!isFound)
                        voteResult.put(playerId,1);
                    }
            }
        } catch (ParseException e) {

        }
    }

    // Get JSONObject from vote result
    public JSONObject getVoteResult() {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = (JSONObject) parser.parse(voteResult.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public int getUIDFromPortAndInetAddress(InetAddress address, int port){
        for (ClientInfo clientInfo: clientList){
            if (clientInfo.getAddress().equals(address) && clientInfo.getPort() == port)
                return clientInfo.getPlayerId();
        }
        return -1;
    }

    public int getRemainingWerewolf() {
        int count = 2;
        for(ClientInfo ci : clientList){
            if(ci.getRole().equals("werewolf")){
                count--;
            }
        }
        return count;
    }
}
