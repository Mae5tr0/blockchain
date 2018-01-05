import java.util.ArrayList;
import java.util.List;

public class TxHandler {
    private UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        pool = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        //1
        for (Transaction.Input input : tx.getInputs()) {
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            if (!pool.contains(utxo)) {
                return false;
            }
        }
        //2
        for (int index = 0; index < tx.getInputs().size(); index++) {
            Transaction.Input input = tx.getInput(index);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output output = pool.getTxOutput(utxo);

            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(index), input.signature)) {
                return false;
            }
        }
        //3
        UTXOPool buf_pool = new UTXOPool(pool);
        for (Transaction.Input input : tx.getInputs()) {
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            if (!buf_pool.contains(utxo)) {
                return false;
            }
            buf_pool.removeUTXO(utxo);
        }
        //4
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) return false;
        }
        //5
        double sum_input = 0;
        double sum_output = 0;
        for (Transaction.Input input : tx.getInputs()) {
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output outputForInput = pool.getTxOutput(utxo);
            sum_input += outputForInput.value;
        }
        for (Transaction.Output output : tx.getOutputs()) {
            sum_output += output.value;
        }
        return sum_input >= sum_output ;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        //simple filter valid transactions
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                result.add(tx);
            }
        }
        //check for mutually valid array of accepted transactions

        //update UTXO pool

        return result.toArray(new Transaction[result.size()]);
    }

}
