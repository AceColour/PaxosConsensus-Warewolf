package Client;

import java.net.InetSocketAddress;

/**
 * Created by nim_13512501 on 4/23/16.
 */
public interface UI {
    InetSocketAddress askServerAddress();
    void displaySuccessfulServerJoin();
    void displayFailedServerJoin(String cause);
    String askUsername();

    // Ready Up
    void displaySuccessfulReadyUp();
    void displayFailedReadyUp(String cause);

    // Leave
    void displaySuccessfulLeave();
    void displayFailedLeave(String cause);

    // List Client
    void displaySuccessfulRetrieveList();
    void displayFailedRetrieveList(String cause);
}

