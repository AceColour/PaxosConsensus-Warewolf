package GamePlay;

/**
 * Created by erickchandra on 4/30/16.
 */
public class Player {
    // Attributes
    private String ipAddress;
    private int portNumber;
    private int playerId;
    private String username;
    private boolean aliveStatus;

    // Constructor
    public Player(String _ipAddress, int _portNumber, int _playerId, String _username, boolean _aliveStatus) {
        this.ipAddress = _ipAddress;
        this.portNumber = _portNumber;
        this.playerId = _playerId;
        this.username = _username;
        this.aliveStatus = _aliveStatus;
    }

    // Methods
    // Getter
    public String getIpAddress() {
        return ipAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public boolean getAliveStatus() {
        return aliveStatus;
    }

    // Setter
    public void setIpAddress(String _ipAddress) {
        this.ipAddress = _ipAddress;
    }

    public void setPortNumber(int _portNumber) {
        this.portNumber = _portNumber;
    }

    public void setPlayerId(int _playerId) {
        this.playerId = _playerId;
    }

    public void setUsername(String _username) {
        this.username = _username;
    }

    public void setAliveStatus(boolean _aliveStatus) {
        this.aliveStatus = _aliveStatus;
    }
}
