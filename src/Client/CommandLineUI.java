package Client;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * Created by nim_13512501 on 4/23/16.
 */
public class CommandLineUI implements UI{

    @Override
    public InetSocketAddress askServerAddress(){
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
    public String askUsername(){
        System.out.println("=====Insert Username=====");
        System.out.print("user name: ");

        Scanner scanIn = new Scanner(System.in);
        return scanIn.nextLine();
    }

    @Override
    public int askPortUDP() {
        System.out.println("=====Insert UDP Port=====");
        System.out.print("port UDP: ");

        Scanner scanIn = new Scanner(System.in);
        return scanIn.nextInt();
    }

    @Override
    public int askKPUId() {
        System.out.println("=====Insert Proposed KPU Id====");
        System.out.print("KPU Id=: ");

        Scanner scanIn = new Scanner(System.in);
        return scanIn.nextInt();
    }

    @Override
    public void displaySuccessfulResponse(String header){
        System.out.println("=====" + header + " Result=====");
        System.out.println("status: ok");
    }

    @Override
    public void displayFailedResponse(String header, String cause){
        System.out.println("=====" + header + " Result=====");
        System.out.println("status: failed");
        System.out.println("cause: " + cause);
    }

    @Override
    public void displayErrorResponse(String header, String cause) {
        System.out.println("=====" + header + " Result=====");
        System.out.println("status: failed");
        System.out.println("cause: " + cause);
    }
}