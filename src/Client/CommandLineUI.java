package Client;

import Client.Misc.ClientInfo;

import java.net.InetSocketAddress;
import java.util.List;
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

    // Voting handler
    @Override
    public int killWerewolfId(){
        System.out.println("=======         WEREWOLF       ======");
        System.out.println("=====Vote Player Id  to be killed====");
        System.out.print("Player Id=: ");

        Scanner scanIn = new Scanner(System.in);
        return scanIn.nextInt();
    }

    @Override
    public int killCivilianId(){
        System.out.println("=======         CIVILIAN       ======");
        System.out.println("=====Vote Player Id suspected as Werewolf to be killed====");
        System.out.print("Player Id=: ");

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

    @Override
    public void displayGameOver(String winner) {
        System.out.println("==============================");
        System.out.println("---------- GAME OVER ---------");
        System.out.println("winner: " + winner);
    }

    @Override
    public void displayListClient(List<ClientInfo> clientInfoList) {
        System.out.println("==============================");
        System.out.println("---------- Players -----------");
        System.out.println("id\talive\tname");
        for (ClientInfo clientInfo : clientInfoList){
            System.out.print(""+clientInfo.getPlayerId() + "\t" + clientInfo.getIsAlive() + "\t" + clientInfo.getUsername());
            if (clientInfo.getRole()!=null)
                System.out.println("\t" + clientInfo.getRole() );
        }
    }

    @Override
    public void displayErrorConnecting(InetSocketAddress inetSocketAddress) {
        System.out.println("Error connecting to " + inetSocketAddress);
    }

    @Override
    public void displayGameStart(Object time, Object role, Object friend) {
        System.out.println("======================");
        System.out.println("---- GAME START! -----");
        if (time!=null)
            System.out.println("time: " + time);
        if (role!=null)
            System.out.println("role: " + role);
        if (friend != null)
            System.out.println("Friends: " + friend);
    }

    @Override
    public int askReadyOrLeave() {
        System.out.println("1: ready");
        System.out.println("0: leave");
        System.out.print("command: ");

        Scanner sc = new Scanner(System.in);

        while(true){
            String s = sc.nextLine();
            if (s.equals("0")){
                return 0;
            }else if (s.equals("1")){
                return 1;
            }
        }
    }

    boolean waiting;

    @Override
    public Boolean askLeaveWhileWaiting() {
        System.out.println("waiting... type LEAVE followed by enter newline to leave the game");
        waiting = true;
        String result = "";
        while (waiting){
            Scanner scanIn = new Scanner(System.in);
            if (scanIn.hasNext()){
                result = scanIn.nextLine();
                if (result.equals("leave"))
                    waiting = false;
            }else
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    waiting = false;
                }
        }
        return result.equals("leave");
    }
}