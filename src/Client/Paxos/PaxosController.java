package Client.Paxos;

import Client.CommandLineUI;
import Client.Communications.TCPRequestResponseChannel;
import Client.Misc.ClientInfo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nim_13512501 on 4/30/16.
 */

//TODO nanti harus direfactor antara proposer dan acceptor
public class PaxosController extends Thread{
    List<ClientInfo> clientList;
    Acceptor acceptor;
    Proposer proposer;
    Messenger messenger;
    int thisPlayerId;

    int idTerbesar = 0;
    int idKeduaTerbesar = 0;

    DatagramSocket datagramSocket;
    TCPRequestResponseChannel tcpRequestResponseChannel;
    /**
     *
     * @param clientList a clientlist received from the server
     * @param thisPlayerId this client's player ID
     * @param datagramSocket a bound datagramsocket for listening and sending messages
     * @throws SocketException
     */
    public PaxosController(List<ClientInfo> clientList, int thisPlayerId, DatagramSocket datagramSocket) throws SocketException {
        super();

        this.datagramSocket = datagramSocket;
        this.clientList = clientList;
        this.thisPlayerId = thisPlayerId;

        //hitung dua client id terbesar
        idTerbesar = 0;
        idKeduaTerbesar = 0;
        this.clientList = new ArrayList<ClientInfo>(clientList);
        for (ClientInfo clientInfo: clientList){
            if (clientInfo.getPlayerId()>idTerbesar)
                idTerbesar = clientInfo.getPlayerId();
            else if (clientInfo.getPlayerId()>idKeduaTerbesar)
                idKeduaTerbesar = clientInfo.getPlayerId();
        }

        messenger = new Messenger(clientList,idTerbesar,idKeduaTerbesar,datagramSocket, tcpRequestResponseChannel);
        acceptor = new Acceptor();
        acceptor.messenger = messenger;
        proposer = new Proposer(this.thisPlayerId, messenger, (clientList.size()-2)/2+1);

    }

    @Override
    public void run(){
        if (thisPlayerId == idTerbesar || thisPlayerId == idKeduaTerbesar)
            try {
                System.out.println("\nYou can propose");
                runAsProposer();

            } catch (SocketException e) {
                e.printStackTrace();
            }
        else
            try {
                runAsAcceptor();
            } catch (SocketException e) {
                e.printStackTrace();
            }
    }

    public void runAsProposer() throws SocketException {
        proposer.setProposedValue(thisPlayerId);//ui.askKPUId());
        proposer.prepare();
        runSisanya();
    }

    public void runAsAcceptor() throws SocketException {
        runSisanya();
    }

    private boolean continueListening;

    public void runSisanya() throws SocketException {
        continueListening = true;
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

    public void stopPaxos(){
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

            String receivedMethod = "";
            if (jsonObject.containsKey("method")){
                receivedMethod = (String) jsonObject.get("method");
            }else if (jsonObject.containsKey("status") && jsonObject.containsKey("description")){
                if (jsonObject.get("status").equals("ok") && jsonObject.get("description").equals("accepted")) {
                    if (jsonObject.containsKey("previous_accepted")) {
                        receivedMethod = "promise";
                    } else {
                        receivedMethod = "acceptAccepted";
                    }
                } else if(jsonObject.get("status").equals("fail") && jsonObject.get("description").equals("rejected")){
                    receivedMethod = "rejected";
                }
            }else {
                messenger.sendError(UID, "unknown method");
            }

            //next method
            //TODO kasih kapan harus berhenti
            if (receivedMethod.equals("prepare")){
                acceptor.receivePrepare(UID, new ProposalId((List<Integer>) jsonObject.get("proposal_id")));
            } else if (receivedMethod.equals("accept")) {
                acceptor.receiveAccept(UID, new ProposalId((List<Integer>) jsonObject.get("proposal_id")), (Integer) jsonObject.get("kpu_id"));
            } else if (receivedMethod.equals("promise")){
                proposer.receivePromise(UID, new ProposalId((List<Integer>) jsonObject.get("proposal_id")),
                        new ProposalId(-1,-1) /*TODO apakah ganti yang bener atau gimana, soalnya di protokol yang dari spek ngga ada ini*/,
                        (Integer) jsonObject.get("previous_accepted"));
            }else if (receivedMethod.equals("acceptAccepted")){
                proposer.receiveAccepted();
                continueListening = false;
            }else if(receivedMethod.equals("rejected")) {
                //TODO Rejected promise
            }

        } catch (ParseException e) {
            try {
                messenger.sendError(UID, "message error");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getUIDFromPortAndInetAddress(InetAddress address, int port){
        for (ClientInfo clientInfo: clientList){
            if (clientInfo.getAddress().equals(address) && clientInfo.getPort() == port)
                return clientInfo.getPlayerId();
        }
        return -1;
    }
}
