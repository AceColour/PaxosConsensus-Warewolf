package Client;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * Created by nim_13512501 on 4/23/16.
 */
public class CommandLineUI implements UI{

    @Override
    public InetSocketAddress askServerAddress() {
        Scanner scanIn = new Scanner(System.in);

        System.out.println("=====Server Info=====");
        System.out.print("Host: ");

        String IPString = scanIn.nextLine();

        System.out.print("Port: ");

        while (!scanIn.hasNextInt()){
            System.out.println("Port must be integer");
            System.out.print("Port: ");
        }

        int Port = scanIn.nextInt();

        return new InetSocketAddress(IPString, Port);
    }

    @Override
    public void displaySuccessfulServerJoin() {
        System.out.println("=====Join Result=====");
        System.out.println("status: ok");
    }

    @Override
    public void displayFailedServerJoin(String cause) {
        System.out.println("=====Join Result=====");
        System.out.println("status: failed");
        System.out.println("cause: " + cause);
    }

    @Override
    public String askUsername() {
        System.out.println("=====Join Result=====");
        System.out.print("user name: ");

        Scanner scanIn = new Scanner(System.in);
        return scanIn.nextLine();
    }

    // Ready Up
    @Override
    public void displaySuccessfulReadyUp() {
        System.out.println("=====ReadyUp Result=====");
        System.out.println("status: ok");
    }

    @Override
    public void displayFailedReadyUp(String cause) {
        System.out.println("=====ReadyUp Result=====");
        System.out.println("status: failed");
        System.out.println("cause: " + cause);
    }

    // Leave
    // Leave
    @Override
    public void displaySuccessfulLeave(){
        System.out.println("=====ReadyUp Result=====");
        System.out.println("status: ok");
    }

    @Override
    public void displayFailedLeave(String cause){
        System.out.println("=====ReadyUp Result=====");
        System.out.println("status: failed");
        System.out.println("cause: " + cause);
    }

}