package Paxos;

import java.util.HashMap;

/**
 * Created by erickchandra on 4/25/16.
 */
public class Learner {
    class Proposal {
        int acceptCount;
        int retentionCount;
        int value;

        Proposal(int acceptCount, int retentionCount, int value) {
            this.acceptCount = acceptCount;
            this.retentionCount = retentionCount;
            this.value = value;
        }
    }

    private final Messenger messenger;
    private final int quorumSize;
    private HashMap<ProposalId, Proposal> proposals = new HashMap<ProposalId, Proposal>();
    private HashMap<String, ProposalId> acceptors = new HashMap<String, ProposalId>();
    private int finalValue = -999;
    private ProposalId finalProposalId = null;

    public Learner(Messenger messenger, int quorumSize) {
        this.messenger = messenger;
        this.quorumSize = quorumSize;
    }

    public int getFinalValue() {
        return finalValue;
    }

    public ProposalId getFinalProposalID() {
        return finalProposalId;
    }

    public boolean isComplete() {

        return finalValue != -999;
    }

    public void receiveAccepted(String fromUId, ProposalId proposalId,
                                int acceptedValue) {

        if (isComplete())
            return;

        ProposalId oldPId = acceptors.get(fromUId);

        if (oldPId != null && !(proposalId.getId() > oldPId.getId()))
            return;

        acceptors.put(fromUId, proposalId);

        if (oldPId != null) {
            Proposal oldProposal = proposals.get(oldPId);
            oldProposal.retentionCount -= 1;
            if (oldProposal.retentionCount == 0)
                proposals.remove(oldPId);
        }

        if (!proposals.containsKey(proposalId))
            proposals.put(proposalId, new Proposal(0, 0, acceptedValue));

        Proposal thisProposal = proposals.get(proposalId);

        thisProposal.acceptCount += 1;
        thisProposal.retentionCount += 1;

        if (thisProposal.acceptCount == quorumSize) {
            finalProposalId = proposalId;
            finalValue = acceptedValue;
            proposals.clear();
            acceptors.clear();

            messenger.onResolution(proposalId, acceptedValue);
        }
    }
}