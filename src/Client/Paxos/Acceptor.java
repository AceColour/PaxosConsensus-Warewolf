package Client.Paxos;

import java.io.IOException;

/**
 * Created by erickchandra on 4/25/16.
 *
 * Acceptor is a class for implementing Client.Paxos Algorithm.
 */
public class Acceptor {
    // Attributes
    protected Messenger messenger;
    protected ProposalId promisedId;
    protected ProposalId acceptedId;
    protected int acceptedValue;
    protected int prevAcceptedValue;

    // Constructor


    // Getter
    public ProposalId getPromisedId() {
        return promisedId;
    }

    public ProposalId getAcceptedId() {
        return acceptedId;
    }

    public int getAcceptedValue() {
        return acceptedValue;
    }

    public int getPrevAcceptedValue() {return prevAcceptedValue;}

    // Setter: N/A

    // Methods: SEND(s)
    public void sendPromise() {

    }

    public void sendAccepted() {

    }

    // Methods: RECEIVE(s)
    public void receivePrepare(int fromUId, ProposalId proposalId) throws IOException {
        if (this.promisedId != null && proposalId.equals(promisedId)) { // duplicate message
            messenger.sendPromise(fromUId, prevAcceptedValue, acceptedValue);
        }
        else if (this.promisedId == null || proposalId.getId() > promisedId.getId()) {
            promisedId = proposalId;
            messenger.sendPromise(fromUId, prevAcceptedValue, acceptedValue);
        }
    }

    public void receiveAccept(int fromUId, ProposalId proposalId,
                              int value) throws IOException {
        if (promisedId == null || proposalId.getId() > promisedId.getId() || proposalId.equals(promisedId)) {
            promisedId    = proposalId;
            acceptedId    = proposalId;
            prevAcceptedValue = acceptedValue;
            acceptedValue = value;

            messenger.sendAccepted(acceptedId, acceptedValue);
        }
    }
}
