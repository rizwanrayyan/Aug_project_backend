package com.example.saveetha_ec.service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticEIP1559GasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import com.example.saveetha_ec.contracts.DigitalGoldToken; // Updated contract wrapper
import com.example.saveetha_ec.model.UserDetail;
import com.example.saveetha_ec.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;

@Service
public class BlockchainService {

    @Value("${blockchain.node.url}")
    private String nodeUrl;

    @Value("${backend.wallet.private-key}")
    private String privateKey;

    @Value("${contract.address}")
    private String contractAddress;

    private Web3j web3j;
    private Credentials credentials;

    private final UserRepository userRepository;

    public BlockchainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Add this class inside the BlockchainService class
    public static class MintingResult {
        private final String batchId;
        private final String transactionHash;

        public MintingResult(String batchId, String transactionHash) {
            this.batchId = batchId;
            this.transactionHash = transactionHash;
        }

        public String getBatchId() {
            return batchId;
        }

        public String getTransactionHash() {
            return transactionHash;
        }
    }

    @PostConstruct
    public void init() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        this.web3j = Web3j.build(new HttpService(nodeUrl, okHttpClient));
        this.credentials = Credentials.create(privateKey);
        System.out.println("✅ BlockchainService initialized. Backend Wallet Address: " + credentials.getAddress());
    }

    // UPDATED: Now returns the batchId from the event.
    public MintingResult mintTokens(Long userId, BigInteger amount, byte[] dataHash) throws Exception {
        UserDetail user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        String toAddress = user.getWalletAddress();
        if (toAddress == null || toAddress.isBlank()) {
            throw new IllegalStateException("User " + userId + " does not have a wallet address set.");
        }

        long chainId = 80002;
        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

        ContractGasProvider gasProvider = new StaticEIP1559GasProvider(chainId, Convert.toWei("150", Convert.Unit.GWEI).toBigInteger(), Convert.toWei("30", Convert.Unit.GWEI).toBigInteger(), BigInteger.valueOf(500_000L));
        // 4. Load the contract with dynamic gas settings
        DigitalGoldToken contract = DigitalGoldToken.load(contractAddress, web3j, txManager, gasProvider);
        // Use new contract wrapper

        var transactionReceipt = contract.purchaseGold(toAddress, amount, dataHash).send();

        if (!transactionReceipt.isStatusOK()) {
            throw new RuntimeException("Minting transaction failed on-chain. Status: " + transactionReceipt.getStatus() + ". Revert reason: " + transactionReceipt.getRevertReason());
        }

        var events = contract.getGoldPurchasedEvents(transactionReceipt);
        if (events.isEmpty()) {
            throw new RuntimeException("Could not find GoldPurchased event in transaction receipt.");
        }

        String batchId = Numeric.toHexString(events.get(0).batchId);
        System.out.println("✅ Transaction successful! Hash: " + transactionReceipt.getTransactionHash() + ", BatchId: " + batchId); // Return batchId to be stored

        return new MintingResult(batchId, transactionReceipt.getTransactionHash());
    }

    // UPDATED: Simplified to match the new contract's redeemGold function.
    public String redeemGold(Long userId, BigInteger amount) throws Exception {
        UserDetail user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        String userAddress = user.getWalletAddress();
        if (userAddress == null || userAddress.isBlank()) {
            throw new IllegalStateException("User " + userId + " does not have a wallet address set.");
        }

        long chainId = 80002;
        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);
        ContractGasProvider gasProvider = new StaticEIP1559GasProvider(chainId, Convert.toWei("150", Convert.Unit.GWEI).toBigInteger(), Convert.toWei("30", Convert.Unit.GWEI).toBigInteger(), BigInteger.valueOf(500_000L));

        DigitalGoldToken contract = DigitalGoldToken.load(contractAddress, web3j, txManager, gasProvider); // Use new contract wrapper

        var transactionReceipt = contract.redeemGold(userAddress, amount).send();

        if (!transactionReceipt.isStatusOK()) {
            throw new RuntimeException("Redemption transaction failed on-chain. Status: " + transactionReceipt.getStatus() + ". Revert reason: " + transactionReceipt.getRevertReason());
        }

        System.out.println("✅ Redemption successful! Hash: " + transactionReceipt.getTransactionHash());
        return transactionReceipt.getTransactionHash();
    }

    /**
     * Calls the smart contract to get the dataHash of a specific batch.
     *
     * @param batchId The on-chain ID of the batch.
     * @return The data hash stored on the blockchain for that batch.
     * @throws Exception if the blockchain call fails.
     */
 // Inside the getOnChainBatchHash method in BlockchainService.java

    @SuppressWarnings("deprecation")
	public byte[] getOnChainBatchHash(String batchId) throws Exception {
        DigitalGoldToken contract = DigitalGoldToken.load(contractAddress, web3j, credentials, (ContractGasProvider) null);

        // ------------------- FIX -------------------
        // Convert the hex string to a BigInteger first, then to a 32-byte array.
        // This correctly handles padding for the bytes32 type.
        BigInteger batchIdBigInt = Numeric.toBigInt(batchId);
        byte[] batchIdBytes32 = Numeric.toBytesPadded(batchIdBigInt, 32);
        // ----------------- END FIX -----------------

        // The getBatch function returns multiple values, we are interested in the second one (dataHash)
        var batchDetails = contract.getBatch(batchIdBytes32).send(); // Use the padded byte array

        if (batchDetails != null && batchDetails.getValue2() != null) {
             return (byte[]) batchDetails.getValue2();
        } else {
            throw new RuntimeException("Could not retrieve batch details from the blockchain for batchId: " + batchId);
        }
    }
}
