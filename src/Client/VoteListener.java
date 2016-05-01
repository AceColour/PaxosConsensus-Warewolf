package Client;

import Client.Communications.TCPRequestResponseChannel;
import Client.Misc.ClientInfo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;
import java.util.*;

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
                if(hasVotedId.size() >= getNumRemainingPlayer() && isDay){
                    continueListening = false;
                    break;
                }else if(hasVotedId.size() >= getRemainingWerewolf() && !isDay) {
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
                        if(entry.getKey() == playerId && !hasVotedId.contains(UID)){
                            countVote = entry.getValue();
                            countVote++;
                            entry.setValue(countVote);
                            isFound = true;
                            hasVotedId.add(UID);
                        }
                    }
                    if(!isFound && !hasVotedId.contains(UID)){
                        voteResult.put(playerId,1);
                        hasVotedId.add(UID);
                    }
            }
        }
        }catch (ParseException e) {

        }
    }

    // Get JSONObject from vote result
    public JSONObject getInfoKilled() {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = new JSONObject();

        List<List<Integer>> voteResultList = new LinkedList<List<Integer>>();
        //search majority
        int maxVal = 0;
        boolean majorityExists = false;
        int maxKey = -1;
        for (Map.Entry<Integer, Integer> entry : voteResult.entrySet()){
            if (entry.getValue()>maxVal){
                maxKey=entry.getKey();
                maxVal=entry.getValue();
                majorityExists=true;
            }else if (entry.getValue()==maxVal){ //lebih dari satu key memiliki maximum value yang sama
                majorityExists=false;
            }
            List<Integer> thisEntry = new ArrayList<Integer>();
            thisEntry.add(entry.getKey());thisEntry.add(entry.getValue());
            voteResultList.add(thisEntry);
        }


        //konstruksi jsonObject
        if (isDay)
                jsonObject.put("method","vote_result_civilian");
        else    jsonObject.put("method","vote_result_werewolf");
        if (majorityExists){
            jsonObject.put("vote_status",1);
            jsonObject.put("player_killed", maxKey);
        }else    jsonObject.put("vote_status",-1);
        jsonObject.put("vote_result",voteResultList);


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
            if (ci.getRole()!=null)
            if(ci.getRole().equals("werewolf") && ci.getIsAlive()==0){
                count--;
            }
        }
        return count;
    }

    public int getNumRemainingPlayer(){
        int count = 0;
        for(ClientInfo ci : clientList){
            if (ci.getIsAlive()==1)
                count++;
        }
        return count;

    }
}
