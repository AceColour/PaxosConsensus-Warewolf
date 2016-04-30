package Client.Misc;

import java.net.InetAddress;

/**
 * Created by nim_13512501 on 4/29/16.
 */
public class ClientInfo {
    private int playerId;
    private int isAlive;
    private InetAddress address;
    private int port;
    private String username;
    private String role;

    // Constructor
    public ClientInfo(int playerId, int isAlive, InetAddress address, int port, String username){
        this.playerId = playerId;
        this.isAlive = isAlive;
        this.address = address;
        this.port = port;
        this.username = username;
    }

    public ClientInfo(int playerId, int isAlive, InetAddress address, int port, String username, String role){
        this.playerId = playerId;
        this.isAlive = isAlive;
        this.address = address;
        this.port = port;
        this.username = username;
        this.role = role;
    }


    // Getter
    public int getPlayerId() {
        return playerId;
    }

    public int getIsAlive() {
        return isAlive;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }


    // Setter
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setIsAlive(int isAlive) {
        this.isAlive = isAlive;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role){
        this.role = role;
    }
}
