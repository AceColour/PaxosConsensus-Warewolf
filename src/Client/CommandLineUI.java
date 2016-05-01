package Client;

import Client.Communications.ListNets;
import Client.Misc.ClientInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.util.*;

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
    public InetAddress askAddressUDP(Enumeration<NetworkInterface> networkInterfaceEnumeration) throws SocketException {
        Map<Integer, InetAddress> choices = new HashMap<Integer, InetAddress>();
        int choicesindex = 1;
        System.out.println("=====Choose UDP Address=====");
        for (NetworkInterface networkInterface : Collections.list(networkInterfaceEnumeration)){
            List<InetAddress> inetAddresses = ListNets.getInetAddresses(networkInterface);
            for (InetAddress inetAddress : inetAddresses){
                choices.put(choicesindex,inetAddress);
                System.out.println("" + choicesindex + ". " + inetAddress.getHostAddress() + "\t" +networkInterface.getDisplayName());
                choicesindex++;
            }
        }

        Scanner sc = new Scanner(System.in);

        while(true){

            System.out.print("pilihan (angka di depannya):");

            String s = sc.nextLine();

            try{
                int choice = Integer.parseInt(s);
                if (choices.containsKey(choice))
                    return choices.get(choice);
            }catch(NumberFormatException e){
            }
        }

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
        while (!scanIn.hasNextInt()){
            if (scanIn.hasNext()) scanIn.next(); else try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return scanIn.nextInt();
    }

    @Override
    public int killCivilianId(){
        System.out.println("=======         CIVILIAN       ======");
        System.out.println("=====Vote Player Id suspected as Werewolf to be killed====");
        System.out.print("Player Id=: ");

        Scanner scanIn = new Scanner(System.in);
        while (!scanIn.hasNextInt()){
            if (scanIn.hasNext()) scanIn.next(); else try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
            else
                System.out.println("");
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
    public void displayPlayerKilled(ClientInfo clientInfo) {
        System.out.println("");
        System.out.println("Player Killed: " +clientInfo.getPlayerId() + "\t" + clientInfo.getUsername());
        if (clientInfo.getRole()!=null)
            System.out.println("\t" + clientInfo.getRole() );
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

    @Override
    public int askPlayerKilled(String phase) {
        System.out.println("Vote kill: ");
        Scanner sc = new Scanner(System.in);
        while (!sc.hasNextInt()){
            if (sc.hasNext()) sc.next(); else try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        }

        return sc.nextInt();
    }

    boolean waiting;

    @Override
    public Boolean askLeaveWhileWaiting() {
        System.out.println("waiting... type LEAVE followed by enter newline to leave the game");
        waiting = true;
        String result = "";
        while (waiting){
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            try {
                // wait until we have data to complete a readLine()
                while (!in.ready()  /*  ADD SHUTDOWN CHECK HERE */) {
                    Thread.sleep(200);
                }
                result= in.readLine();
            } catch (InterruptedException e) {
                waiting=false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result.equals("leave");
    }
}