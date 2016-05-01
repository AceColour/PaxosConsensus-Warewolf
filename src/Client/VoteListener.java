package Client;

import Client.Communications.TCPRequestResponseChannel;
import Client.Misc.ClientInfo;
import Client.Paxos.Acceptor;
import Client.Paxos.Messenger;
import Client.Paxos.ProposalId;
import Client.Paxos.Proposer;
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
    CommandLineUI ui;
    int thisPlayerId;
    DatagramSocket datagramSocket;
    TCPRequestResponseChannel tcpRequestResponseChannel;
    Boolean isWerewolf = false;
    Map<Integer,Integer> voteResult = new HashMap();
    /**
     *
     * @param clientList a clientlist received from the server
     * @param thisPlayerId this client's player ID
     * @param datagramSocket a bound datagramsocket for listening and sending messages
     * @throws SocketException
     */
    public VoteListener(List<ClientInfo> clientList, int thisPlayerId, DatagramSocket datagramSocket, Boolean isWerewolf) throws SocketException {
        super();
        this.clientList = new ArrayList<ClientInfo>(clientList);
        continueListening = true;
        this.isWerewolf = isWerewolf;
    }

    @Override
    public void run(){
        if(voteResult.size() >= clientList.size() && !isWerewolf){
            continueListening = false;
        }else if(voteResult.size() >= 2 && isWerewolf) {
            continueListening = false;
        }
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

            String receivedMethod = "";
            if (jsonObject.containsKey("method")){
                if(jsonObject.get("method").equals("vote_werewolf") && isWerewolf){
                    int playerId =  Integer.parseInt(jsonObject.get("player_id").toString());

                    Object value = voteResult.get(playerId);
                    if (value != null) {
                        // Get value
                        for (Map.Entry<Integer, Integer> entry : voteResult.entrySet()) {
                            if(entry.getKey() == playerId){
                                countVote += entry.getValue();
                            }
                        }
                    } else {
                        // No such key
                        voteResult.put(playerId,1);
                    }
                }

//            }else if (jsonObject.containsKey("status") && jsonObject.containsKey("description")){
//                if (jsonObject.get("status").equals("ok") && jsonObject.get("description").equals("accepted")) {
//                    if (jsonObject.containsKey("previous_accepted")) {
//                        receivedMethod = "promise";
//                    } else {
//                        receivedMethod = "acceptAccepted";
//                    }
//                } else if(jsonObject.get("status").equals("fail") && jsonObject.get("description").equals("rejected")){
//                    receivedMethod = "rejected";
//                }
            }else {

            }

//            //next method
//            //TODO kasih kapan harus berhenti
//            if (receivedMethod.equals("prepare")){
//                acceptor.receivePrepare(UID, new ProposalId((List<Integer>) jsonObject.get("proposal_id")));
//            } else if (receivedMethod.equals("accept")) {
//                acceptor.receiveAccept(UID, new ProposalId((List<Integer>) jsonObject.get("proposal_id")), (Integer) jsonObject.get("kpu_id"));
//            } else if (receivedMethod.equals("promise")){
//                proposer.receivePromise(UID, new ProposalId((List<Integer>) jsonObject.get("proposal_id")),
//                        new ProposalId(-1,-1) /*TODO apakah ganti yang bener atau gimana, soalnya di protokol yang dari spek ngga ada ini*/,
//                        (Integer) jsonObject.get("previous_accepted"));
//            }else if (receivedMethod.equals("acceptAccepted")){
//                proposer.receiveAccepted();
//                continueListening = false;
//            }else if(receivedMethod.equals("rejected")) {
//                //TODO Rejected promise
//            }

        } catch (ParseException e) {

        }
    }

    public Map<Integer, Integer> getVoteResult() {
        return voteResult;
    }

    public int getUIDFromPortAndInetAddress(InetAddress address, int port){
        for (ClientInfo clientInfo: clientList){
            if (clientInfo.getAddress().equals(address) && clientInfo.getPort() == port)
                return clientInfo.getPlayerId();
        }
        return -1;
    }
}
