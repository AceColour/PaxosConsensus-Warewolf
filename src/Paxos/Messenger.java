package Paxos;

import Communications.UnreliableSender;
import Misc.ClientInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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

    public Messenger (List<ClientInfo> listClient, int clientIdTerbesar, int clientIdKeduaTerbesar) throws SocketException {
        this.listClient = listClient;
        this.clientIdTerbesar = clientIdTerbesar;
        this.clientIdKeduaTerbesar = clientIdKeduaTerbesar;

        datagramSocket = new DatagramSocket();
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

    public void sendAccepted(ProposalId proposalId, Object acceptedValue) throws IOException {
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

    public void onResolution(ProposalId proposalID, Object value){
        //TODO diisi nanti
    }

    //helper
    private void sendJSONString (JSONObject jsonObject, ClientInfo clientInfo) throws IOException {
        String jsonString = jsonObject.toJSONString();
        byte[] data = jsonString.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(data,data.length,clientInfo.getAddress(),clientInfo.getPort());

        unreliableSender.send(datagramPacket);
    }
}
