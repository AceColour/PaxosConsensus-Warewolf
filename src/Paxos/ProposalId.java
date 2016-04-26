package Paxos;

/**
 * Created by erickchandra on 4/25/16.
 *
 * ProposalId is a data-structure-like class.
 */
public class ProposalId {
    // Attributes
    protected int id;

    // Constructor
    public ProposalId(int _id) {
        this.id = _id;
    }

    // Getter
    public int getId() {
        return this.id;
    }

    // Setter
    public void setId(int _id) {
        this.id = _id;
    }

    // Methods
    public void incrementId() {
        this.id += 1;
    }

    public int compare(ProposalId rhs) {
        if (this.id == rhs.id) {
            return 0;
        }
        else if (this.id < rhs.id) {
            return -1;
        }
        else {
            return 1;
        }
    }
}
