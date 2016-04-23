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
}
