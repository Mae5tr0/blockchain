// Example of a Simulation. This test runs the nodes on a random graph.
// At the end, it will print out the Transaction ids which each node
// believes consensus has been reached upon. You can use this simulation to
// test your nodes. You will want to try creating some deviant nodes and
// mixing them in the network to fully test.

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;

//https://www.coursera.org/learn/cryptocurrency/discussions/weeks/4/threads/09H3eJZcEeeNexIKzDOQDA
//https://www.coursera.org/learn/cryptocurrency/discussions/weeks/4/threads/jaqr13AmEeei2A4K_5_JHA
public class Simulation {

   public static void main(String[] args) {

      //Autograder test cases
//      runSimulation(.1, .3, .01, 7);
//      runSimulation(.1, .3, .05, 7);
//      runSimulation(.1, .45, .01, 10);
//      runSimulation(.1, .45, .05, 10);
//      runSimulation(.2, .3, .01, 10);
//      runSimulation(.2, .3, .05, 10);
//      runSimulation(.2, .45, .01, 10);
      runSimulation(.2, .45, .05, 10);
   }

   private static void runSimulation(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
      int numNodes = 100;

      // pick which nodes are malicious and which are compliant
      Node[] nodes = new Node[numNodes];
      for (int i = 0; i < numNodes; i++) {
         if(Math.random() < p_malicious)
            // When you are ready to try testing with malicious nodes, replace the
            // instantiation below with an instantiation of a MaliciousNode
            nodes[i] = new MaliciousNode(p_graph, p_malicious, p_txDistribution, numRounds);
         else
            nodes[i] = new CompliantNode(p_graph, p_malicious, p_txDistribution, numRounds);
      }


      // initialize random follow graph
      boolean[][] followees = new boolean[numNodes][numNodes]; // followees[i][j] is true iff i follows j
      for (int i = 0; i < numNodes; i++) {
         for (int j = 0; j < numNodes; j++) {
            if (i == j) continue;
            if(Math.random() < p_graph) { // p_graph is .1, .2, or .3
               followees[i][j] = true;
            }
         }
      }

      // notify all nodes of their followees
      for (int i = 0; i < numNodes; i++)
         nodes[i].setFollowees(followees[i]);

      // initialize a set of 500 valid Transactions with random ids
      int numTx = 500;
      HashSet<Integer> validTxIds = new HashSet<Integer>();
      Random random = new Random();
      for (int i = 0; i < numTx; i++) {
         int r = random.nextInt();
         validTxIds.add(r);
      }


      // distribute the 500 Transactions throughout the nodes, to initialize
      // the starting state of Transactions each node has heard. The distribution
      // is random with probability p_txDistribution for each Transaction-Node pair.
      for (int i = 0; i < numNodes; i++) {
         HashSet<Transaction> pendingTransactions = new HashSet<Transaction>();
         for(Integer txID : validTxIds) {
            if (Math.random() < p_txDistribution) // p_txDistribution is .01, .05, or .10.
               pendingTransactions.add(new Transaction(txID));
         }
         nodes[i].setPendingTransaction(pendingTransactions);
      }


      // Simulate for numRounds times
      for (int round = 0; round < numRounds; round++) { // numRounds is either 10 or 20

         // gather all the proposals into a map. The key is the index of the node receiving
         // proposals. The value is an ArrayList containing 1x2 Integer arrays. The first
         // element of each array is the id of the transaction being proposed and the second
         // element is the index # of the node proposing the transaction.
         HashMap<Integer, Set<Candidate>> allProposals = new HashMap<>();

         for (int i = 0; i < numNodes; i++) {
            Set<Transaction> proposals = nodes[i].sendToFollowers();
            for (Transaction tx : proposals) {
               if (!validTxIds.contains(tx.id))
                  continue; // ensure that each tx is actually valid

               for (int j = 0; j < numNodes; j++) {
                  if(!followees[j][i]) continue; // tx only matters if j follows i

                  if (!allProposals.containsKey(j)) {
                     Set<Candidate> candidates = new HashSet<>();
                     allProposals.put(j, candidates);
                  }

                  Candidate candidate = new Candidate(tx, i);
                  allProposals.get(j).add(candidate);
               }

            }
         }

         // Distribute the Proposals to their intended recipients as Candidates
         for (int i = 0; i < numNodes; i++) {
            if (allProposals.containsKey(i))
               nodes[i].receiveFromFollowees(allProposals.get(i));
         }
      }


      int compliantNodeCount = 0;
      int consensusCount = 0;
      Set<Transaction> consensus = null;

      int i = 0;
      int j = nodes.length - 1;

      while (true) {
         if (i >= j) break;

         if (nodes[i].sendToFollowers().equals(nodes[j].sendToFollowers())) {
            consensus = nodes[i].sendToFollowers();
         }

         i++;
         j--;
      }

      if (consensus == null) {
         System.out.println("Consensus not found");
         return;
      }

      for (Node node : nodes) {
         if (node.getClass() == CompliantNode.class) {
            compliantNodeCount++;

            if (node.sendToFollowers().equals(consensus)) {
               consensusCount++;
            }
         }
      }

      System.out.println("Valid nodes:" + compliantNodeCount);


      System.out.println("On average " + consensusCount + " out of " + compliantNodeCount + " of nodes reach consensus");
      System.out.println("Consensus length: " + consensus.size());
   }
}

