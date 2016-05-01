package Client.Communications;

import java.io.*;
import java.net.*;
import java.util.*;
import static java.lang.System.out;

public class ListNets {

    public static List<InetAddress> getInetAddresses() throws SocketException {
        List<InetAddress> retval = new LinkedList<InetAddress>();

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)){
            List<InetAddress> inetAddressList = getInetAddresses(netint);
            retval.addAll(inetAddressList);
        }
        return retval;
    }

    public static List<InetAddress> getInetAddresses(NetworkInterface netint) throws SocketException {
        List<InetAddress> retval = new LinkedList<InetAddress>();

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            retval.add(inetAddress);
        }
        return retval;

    }
}