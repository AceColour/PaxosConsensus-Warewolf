package Client;

import Client.Misc.ClientInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by nim_13512501 on 4/23/16.
 */
public interface UI {
    InetSocketAddress askServerAddress();
    String askUsername();
    int askPortUDP();
    InetAddress askAddressUDP(Enumeration<NetworkInterface> choices) throws SocketException;
    int askKPUId();
    int killWerewolfId();
    int killCivilianId();

    void displaySuccessfulResponse(String header);
    void displayFailedResponse(String header, String cause);
    void displayErrorResponse(String header, String cause);

    void displayGameOver(String winner);

    void displayListClient(List<ClientInfo> clientInfoList);

    void displayErrorConnecting(InetSocketAddress inetSocketAddress);
    void displayGameStart(Object time, Object role, Object friend);

    void displayPlayerKilled(ClientInfo clientInfo);

    /**
     * mengembalikan 0 bila leave, 1 bila ready
     * */
    int askReadyOrLeave();

    int askPlayerKilled(String phase);

    Boolean askLeaveWhileWaiting();
}

