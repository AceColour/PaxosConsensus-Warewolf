package Paxos;

/**
 * Created by erickchandra on 4/25/16.
 *
 * Acceptor is a class for implementing Paxos Algorithm.
 */
public class Acceptor {
    // Attributes
    protected ProposalId promisedId;
    protected ProposalId acceptedId;
    protected Object acceptedValue;

    // Constructor


    // Getter
    public ProposalId getPromisedId() {
        return promisedId;
    }

    public ProposalId getAcceptedId() {
        return acceptedId;
    }

    public Object getAcceptedValue() {
        return acceptedValue;
    }

    // Setter: N/A

    // Methods: SEND(s)
    public void sendPromise() {

    }

    public void sendAccepted() {

    }

    // Methods: RECEIVE(s)
    public void receivePrepare() {

    }

    public void receiveAccept() {

    }
}
