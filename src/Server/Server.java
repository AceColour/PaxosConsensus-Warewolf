package Server;

import Client.Communications.TCPRequestResponseChannel;
import GamePlay.*;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by erickchandra on 4/25/16.
 */
public class Server {
    private int currentLastPlayerId = -1;
    private Game game = new Game();

    private TCPRequestResponseChannel communicator;

    public int receiveJoin(String _ipAddr, int _portNo, String _username) {
        // receiveJoin will return integer >= 0 if the client successfully inserted become a player.
        // Returns -1 if username exists.
        // Returns -2 if game is running.

        if (game.isUsernameExists(_username)) {
            return -1;
        }
        else if (game.getStartedStatus()) {
            return -2;
        }
        else {
            game.clientConnect(new Player(_ipAddr, _portNo, ++currentLastPlayerId, _username, true));
            return currentLastPlayerId;
        }
    }

    public void receiveReady(String _ipAddr, int _portNo) {
        game.clientReady(_ipAddr, _portNo);
    }

    public int receiveLeaveGame(String _ipAddr, int _portNo) {
        // Returns 0: no error.
        // Returns 1: game is running.
        return game.clientLeave(_ipAddr, _portNo);
    }

    public HashSet getClientList() {
        return game.getPlayerConnected();
    }

    public void run() {
        try {
            communicator = new TCPRequestResponseChannel(InetAddress.getLocalHost(), 8088);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject temp = communicator.getLastRequestDariSeberangSana();
            System.out.println(temp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();

        server.run();

    }
}
