package Client.Paxos;

import java.io.IOException;
import java.util.HashSet;

/**
 * Created by erickchandra on 4/25/16.
 *
 * Proposer is a class for implementing Client.Paxos Algorithm.
 */
public class Proposer {
    // Attributes
    protected Messenger messenger;
    protected ProposalId proposalId;
    protected int proposedValue = 0;
    protected ProposalId lastAcceptedId = null;
    protected final int quorumSize;
    protected HashSet<Integer> promisesReceived = new HashSet<Integer>();

    // Constructor
    public Proposer(Messenger _messenger, int _quorumSize) {
        this.messenger = _messenger;
        this.quorumSize = _quorumSize;
        this.proposalId = new ProposalId(0, 0);
    }

    // Getter
    public ProposalId getProposalId() {
        return this.proposalId;
    }

    public int getProposedValue() {
        return this.proposedValue;
    }

    public ProposalId getLastAcceptedId() {
        return this.lastAcceptedId;
    }

    public int getQuorumSize() {
        return this.quorumSize;
    }

    public int numPromises() {
        return promisesReceived.size();
    }
    // Setter
    public void setProposedValue(int _proposalValue) {
        this.proposedValue = _proposalValue;
    }

    // Methods: RECEIVE(s)
    public void receivePromise(int _fromUid, ProposalId _proposalId, ProposalId _prevAcceptedId, int _prevAcceptedValue) throws IOException {
        if (!proposalId.equals(this.proposalId) || promisesReceived.contains(_fromUid)) {
            return;
        }

        promisesReceived.add(_fromUid);

        if (lastAcceptedId == null || _prevAcceptedId.getId() > lastAcceptedId.getId()) {
            lastAcceptedId = _prevAcceptedId;

            proposedValue = _prevAcceptedValue;
        }

        if (promisesReceived.size() == quorumSize) {
            if (proposedValue != 0) {
                messenger.sendAccept(this.proposalId, this.proposedValue);
            }
        }
    }

    public void receiveAccepted() {

    }

    // Other methods
    public void prepare() {
        promisesReceived.clear();
        proposalId.incrementId();
        try {
            messenger.sendPrepare(proposalId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
