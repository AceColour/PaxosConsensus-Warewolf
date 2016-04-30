package GamePlay;

/**
 * Created by erickchandra on 4/30/16.
 */
public class Player {
    // Attributes
    private String ipAddress;
    private int portNumber;
    private int playerId;
    private String playerRole;

    // Constructor
    public Player(String _ipAddress, int _portNumber, int _playerId, String _playerRole) {
        this.ipAddress = _ipAddress;
        this.portNumber = _portNumber;
        this.playerId = _playerId;
        this.playerRole = _playerRole;
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

    public String getPlayerRole() {
        return playerRole;
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

    public void setPlayerRole(String _playerRole) {
        this.playerRole = _playerRole;
    }
}
