// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.*;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private Map<byte[], BlockInfo> chain;
    private Block maxHeightBlock;
    private TransactionPool txPool;
    private UTXOPool maxHeightUtxoPool;
    private int maxHeight;


    private class BlockInfo {
        private int height;
        private UTXOPool utxoPool;

        public BlockInfo (int height, UTXOPool utxoPool) {
            this.height = height;
            this.utxoPool = utxoPool;
        }
    }

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        chain = new HashMap<>();
        txPool = new TransactionPool();
        maxHeightUtxoPool = new UTXOPool();

        maxHeight = 0;
        maxHeightBlock = genesisBlock;
        maxHeightUtxoPool = calcUtxoPool(maxHeightUtxoPool, genesisBlock);

        chain.put(genesisBlock.getHash(), new BlockInfo(maxHeight, maxHeightUtxoPool));
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return maxHeightBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return maxHeightUtxoPool;
    }

    private UTXOPool calcUtxoPool(UTXOPool pool, Block block) {
        UTXOPool result = new UTXOPool(pool);

        for (Transaction tx : block.getTransactions()) {
            for (Transaction.Input input : tx.getInputs()) {
                UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                result.removeUTXO(utxo);
            }
            for (int index = 0; index < tx.getOutputs().size(); index++) {
                UTXO utxo = new UTXO(tx.getHash(), index);
                result.addUTXO(utxo, tx.getOutput(index));
            }
        }

        //add coinbase
        UTXO utxo = new UTXO(block.getCoinbase().getHash(), 0);
        result.addUTXO(utxo, block.getCoinbase().getOutput(0));

        return result;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 0) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        if (block.getPrevBlockHash() == null) return  false;

        // don't found parent for block => block invalid
        if (!chain.containsKey(block.getPrevBlockHash())) return false;
        BlockInfo prevBlockInfo = chain.get(block.getPrevBlockHash());

        TxHandler handler = new TxHandler(prevBlockInfo.utxoPool);
        for (Transaction tx : block.getTransactions()) {
            if (!handler.isValidTx(tx)) return false;
        }
        // 100 points can be archived by using bottom check for valid transactions
//        int totalTransactions = block.getTransactions().size();
//        int totalValidTransactions = handler.handleTxs(block.getTransactions().toArray(new Transaction[totalTransactions])).length;
//        if (totalValidTransactions != totalTransactions) return false;

        BlockInfo newBlockInfo = new BlockInfo(prevBlockInfo.height + 1, calcUtxoPool(prevBlockInfo.utxoPool, block));
        chain.put(block.getHash(), newBlockInfo);

        if (newBlockInfo.height > maxHeight) {
            maxHeight = newBlockInfo.height;
            maxHeightBlock = block;
            maxHeightUtxoPool = newBlockInfo.utxoPool;
        }

        cleanUpChain();
        return true;
    }

    private void cleanUpChain() {
        Set<byte[]> outdatedBlocks = new HashSet<>();
        for (Map.Entry<byte[], BlockInfo> entry : chain.entrySet()) {
            if (entry.getValue().height < maxHeight - CUT_OFF_AGE) outdatedBlocks.add(entry.getKey());
        }
        chain.keySet().removeAll(outdatedBlocks);
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        txPool.addTransaction(tx);
    }
}