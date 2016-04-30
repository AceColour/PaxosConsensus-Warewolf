package Server;

import GamePlay.*;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by erickchandra on 4/25/16.
 */
public class Server {
    private int currentLastPlayerId = -1;
    private Game game = new Game();

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

    public void receiveReady() {

    }

    public int receiveLeaveGame(String _ipAddr, int _portNo) {
        // Returns 0: no error.
        // Returns 1: game is running.
        return game.clientLeave(_ipAddr, _portNo);
    }

    public HashSet getClientList() {
        return game.getPlayerConnected();
    }
}
