import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class MaliciousNode implements Node {

    private int numRounds = 0;
    private Set<Transaction> pendingTransactions;
    private Transaction[] tx;

    public MaliciousNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.pendingTransactions = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        return;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions.addAll(pendingTransactions);
        tx = pendingTransactions.toArray(new Transaction[pendingTransactions.size()]);
    }

    public Set<Transaction> sendToFollowers() {
//        numRounds++;

//        Set<Transaction> txForFollowers = new HashSet<>();
//        txForFollowers.add(tx[numRounds]);

        return pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        return;
    }
}
