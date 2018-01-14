import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private static final int MEDIAN_ROUND = 5;
    private static final double INTERSEPTION_PERCENT = 50;

    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;
    private boolean[] followees;

    private int currentRound;
    private Set<Transaction> pendingTransactions;
    private Set<Integer> allowedSenders;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;

        currentRound = 0;
        pendingTransactions = new HashSet<>();
        allowedSenders = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions.addAll(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        return pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        currentRound++;

        if (currentRound == MEDIAN_ROUND) {
            Map<Integer, Set<Transaction>> senderTransactionMap = new HashMap<>();
            for (Candidate candidate : candidates) {
                if (senderTransactionMap.containsKey(candidate.sender)) {
                    senderTransactionMap.get(candidate.sender).add(candidate.tx);
                } else {
                    Set<Transaction> txs = new HashSet<>();
                    txs.add(candidate.tx);
                    senderTransactionMap.put(candidate.sender, txs);
                }
            }

            for (Map.Entry<Integer, Set<Transaction>> entry : senderTransactionMap.entrySet()) {
                Set<Transaction> txSet = entry.getValue();
                int baseSize = pendingTransactions.size();
                txSet.retainAll(pendingTransactions);
                if ( (txSet.size()/(double) baseSize)*100 > INTERSEPTION_PERCENT) allowedSenders.add(entry.getKey());
            }
        }

        for (Candidate candidate : candidates) {
            if (allowedSenders.size() > 0 && !allowedSenders.contains(candidate.sender)) {
                continue;
            }
            this.pendingTransactions.add(candidate.tx);
        }
    }
}
