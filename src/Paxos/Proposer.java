package Paxos;

/**
 * Created by erickchandra on 4/25/16.
 *
 * Proposer is a class for implementing Paxos Algorithm.
 */
public class Proposer {
    // Attributes
    protected ProposalId proposalId;
    protected Object proposedValue = null;
    protected ProposalId lastAcceptedId = null;
    protected final int quorumSize;

    // Constructor
    public Proposer(int _quorumSize) {
        this.quorumSize = _quorumSize;
        this.proposalId = new ProposalId(0);
    }

    // Getter
    public ProposalId getProposalId() {
        return this.proposalId;
    }

    public Object getProposedValue() {
        return this.proposedValue;
    }

    public ProposalId getLastAcceptedId() {
        return this.lastAcceptedId;
    }

    public int getQuorumSize() {
        return this.quorumSize;
    }

    // Setter
    public void setProposedValue(Object _proposalValue) {
        this.proposedValue = _proposalValue;
    }

    // Methods: SEND(s)
    public void sendPrepare() {

    }

    public void sendAccept() {

    }

    // Methods: RECEIVE(s)
    public void receivePromise() {

    }

    public void receiveAccepted() {

    }
}
