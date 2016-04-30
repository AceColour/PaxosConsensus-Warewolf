package Client.Paxos;

import java.util.List;

/**
 * Created by erickchandra on 4/25/16.
 *
 * ProposalId is a data-structure-like class.
 */
public class ProposalId {
    // Attributes
    protected int id;
    private int playerId;

    public ProposalId(List<Integer> playerIDListFromSpekYangAnehBanget ){
        this.id = playerIDListFromSpekYangAnehBanget.get(0);
        this.playerId = playerIDListFromSpekYangAnehBanget.get(1);
    }

    // Constructor
    public ProposalId(int _id, int _playerId) {
        this.id = _id;
        this.setPlayerId(_playerId);
    }

    // Getter
    public int getId() {
        return this.id;
    }

    public int getPlayerId() {
        return playerId;
    }

    // Setter
    public void setId(int _id) {
        this.id = _id;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    // Methods
    public void incrementId() {
        this.id += 1;
    }

    public int compare(ProposalId rhs) {
        if (this.id == rhs.id) {
            if (rhs.playerId == this.playerId){
                return 0;
            }
            else if (this.playerId < rhs.playerId) {
                return -1;
            }
            else {
                return 1;
            }
        }
        else if (this.id < rhs.id) {
            return -1;
        }
        else {
            return 1;
        }
    }
}
