package GamePlay;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by erickchandra on 4/25/16.
 */
public class Learner {

    private final int quorumSize;
    private HashMap<Integer, Integer> acceptcounts = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> acceptors = new HashMap<Integer, Integer>();
    private int finalValue = -999;

    private AtomicBoolean quorumReached = new AtomicBoolean();

    public Learner(int quorumSize) {
        this.quorumSize = quorumSize;
        quorumReached.set(false);
    }

    public synchronized int waitFinalValue() throws InterruptedException {
        while (!quorumReached.get()) synchronized(quorumReached) {
            quorumReached.wait();
        }
        return getFinalValue();
    }

    public int getFinalValue() {
        return finalValue;
    }

    public boolean isComplete() {

        return finalValue != -999;
    }

    public void receiveAccepted(int fromUId,
                                int acceptedValue) {

        if (isComplete())
            return;

        acceptors.put(fromUId, acceptedValue);

        if (!acceptcounts.containsKey(acceptedValue))
            acceptcounts.put(acceptedValue, 0);

        acceptcounts.replace(acceptedValue,acceptcounts.get(acceptedValue)+1);

        Integer thisCount = acceptcounts.get(acceptedValue);

        System.out.println(acceptedValue + " got " + thisCount + "accepteds");

        if (thisCount == quorumSize){
            finalValue = acceptedValue;
            acceptcounts.clear();
            acceptors.clear();
            synchronized(quorumReached){
                quorumReached.set(true);
                quorumReached.notify();
            }
        }
    }
}