package Client;

import java.net.InetSocketAddress;

/**
 * Created by nim_13512501 on 4/23/16.
 */
public interface UI {
    InetSocketAddress askServerAddress();
    String askUsername();
    int askPortUDP();
    int askKPUId();

    void displaySuccessfulResponse(String header);
    void displayFailedResponse(String header, String cause);
    void displayErrorResponse(String header, String cause);

    void displayGameOver(String winner);
}

