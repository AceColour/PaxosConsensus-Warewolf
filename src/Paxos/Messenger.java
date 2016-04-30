package Paxos;

import Communications.UnreliableSender;
import Misc.ClientInfo;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by erickchandra on 4/25/16.
 */
//TODO tambah learner
public class Messenger {

    List<ClientInfo> listClient;
    int clientIdTerbesar;
    int clientIdKeduaTerbesar;

    DatagramSocket datagramSocket;
    UnreliableSender unreliableSender;

    public Messenger (List<ClientInfo> listClient, int clientIdTerbesar, int clientIdKeduaTerbesar, DatagramSocket datagramSocket) throws SocketException {
        this.listClient = listClient;
        this.clientIdTerbesar = clientIdTerbesar;
        this.clientIdKeduaTerbesar = clientIdKeduaTerbesar;

        this.datagramSocket = datagramSocket;
        unreliableSender = new UnreliableSender(datagramSocket);
    }

    public void sendPrepare(ProposalId proposalId) throws IOException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(proposalId.getId());
        jsonArray.add(proposalId.getPlayerId());
        jsonObject.put("method","prepare_proposal");
        jsonObject.put("proposal_id", jsonArray);

        for (ClientInfo clientInfo : listClient){

            if (clientInfo.getPlayer_id() != clientIdKeduaTerbesar && clientInfo.getPlayer_id() != clientIdTerbesar){

                sendJSONString(jsonObject,clientInfo);
            }
        }
    }

    public void sendPromise(int proposerUID, int prevAcceptedValue, int acceptedValue) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "ok");
        jsonObject.put("description", "accepted");
        jsonObject.put("previous_accepted", prevAcceptedValue);

        for (ClientInfo clientInfo : listClient){
            if (clientInfo.getPlayer_id() == proposerUID){
                sendJSONString(jsonObject,clientInfo);
            }
        }
    }

    public void sendAccept(ProposalId proposalId, int proposalValue) throws IOException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(proposalId.getId());
        jsonArray.add(proposalId.getPlayerId());
        jsonObject.put("method", "accept_proposal");
        jsonObject.put("proposal_id", jsonArray);
        jsonObject.put("kpu_id", proposalValue);
        for (ClientInfo clientInfo : listClient){
            if (clientInfo.getPlayer_id() == proposalId.getPlayerId()){
                sendJSONString(jsonObject,clientInfo);
            }
        }
    }

    public void sendAccepted(ProposalId proposalId, int acceptedValue) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "ok");
        jsonObject.put("description", "accepted");
        for (ClientInfo clientInfo : listClient){
            if (clientInfo.getPlayer_id() == proposalId.getPlayerId()){
                sendJSONString(jsonObject,clientInfo);
            }
        }
        //TODO tambah learner
    }

    public void onResolution(ProposalId proposalID,  int value){
        //TODO diisi nanti
    }

    public void sendFail(int UID, String description) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "fail");
        jsonObject.put("description", description);
        for (ClientInfo clientInfo : listClient){
            if (clientInfo.getPlayer_id() == UID){
                sendJSONString(jsonObject,clientInfo);
            }
        }
    }

    public void sendError(int UID, String description) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "error");
        jsonObject.put("description", description);
        for (ClientInfo clientInfo : listClient){
            if (clientInfo.getPlayer_id() == UID){
                sendJSONString(jsonObject,clientInfo);
            }
        }
    }

    //helper
    private void sendJSONString (JSONObject jsonObject, ClientInfo clientInfo) throws IOException {
        String jsonString = jsonObject.toJSONString();
        byte[] data = jsonString.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(data,data.length,clientInfo.getAddress(),clientInfo.getPort());

        unreliableSender.send(datagramPacket);
    }

    //untuk testing
    //prosedur: jalankan netcat -ul untuk port 4000-4005
    //periksa di semua
    //mestinya ada beberapa yang ngga nyampe
    public static void main (String [] args) throws IOException {
        List<ClientInfo> listClient = new ArrayList<ClientInfo>();
        for (int i=0;i<6;i++){
            ClientInfo clientInfo = new ClientInfo(i, 1, InetAddress.getByName("localhost"), 4000+i, "tai" + i);
            listClient.add(clientInfo);
        }

        Messenger messenger = new Messenger(listClient,5,4,new DatagramSocket());

        ProposalId proposalId = new ProposalId(1,2);

        messenger.sendPrepare(proposalId);
        messenger.sendPromise(0,1,2);
        messenger.sendAccept(proposalId,1);
        messenger.sendAccepted(proposalId, 2);
    }
}
