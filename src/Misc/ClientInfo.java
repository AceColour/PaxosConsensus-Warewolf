package Misc;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by nim_13512501 on 4/29/16.
 */
public class ClientInfo {
    private int player_id;
    private int is_alive;
    private Boolean isReady;
    private Boolean isStart;
    private Boolean isKPU;
    private InetAddress address;
    private int port;
    private String username;
    private String role;


    // Constructor

    public ClientInfo(){

    }


    // Getter

    public int getPlayer_id() {
        return player_id;
    }

    public int getIs_alive() {
        return is_alive;
    }

    public Boolean getIsReady(){
        return isReady;
    }

    public Boolean getIsStart(){
        return isStart;
    }

    public Boolean getIsKPU() {
        return isKPU;
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

    public void setPlayer_id(int player_id) {
        this.player_id = player_id;
    }

    public void setIs_alive(int is_alive) {
        this.is_alive = is_alive;
    }

    public void setIsReady(Boolean status) {
        isReady = status;
    }

    public void setIsStart(Boolean status) {
        isStart = status;
    }

    public void setIsKPU(Boolean status) {
        isKPU = status;
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

    public void setRole(String role) {
        this.role = role;
    }
}
