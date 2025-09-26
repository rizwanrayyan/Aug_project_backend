package com.example.saveetha_ec.service;

import com.example.saveetha_ec.contracts.DigitalGoldToken;
import com.example.saveetha_ec.model.UserDetail;
import com.example.saveetha_ec.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticEIP1559GasProvider;
import org.web3j.utils.Convert;


import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

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
    // We remove the contract and txManager from class-level fields to create them dynamically

    private final UserRepository userRepository;

    public BlockchainService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public String mintTokens(Long userId, BigInteger amount, byte[] dataHash) throws Exception {
        UserDetail user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + userId));

        String toAddress = user.getWalletAddress();
        if (toAddress == null || toAddress.isBlank()) {
            throw new IllegalStateException("User " + userId + " does not have a wallet address set.");
        }

        // --- THE FIX: DYNAMIC NONCE AND TRANSACTION MANAGER ---

        // 1. Get the latest transaction count (nonce) from the network for our backend wallet.
        // We use 'PENDING' to include transactions that are still in the mempool.
        BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING).send().getTransactionCount();
        System.out.println("Using nonce: " + nonce);

        long chainId = 80002; // Polygon Amoy testnet chain ID

        // 2. Create a new Transaction Manager for THIS transaction with the correct nonce.
        RawTransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);

        // 3. Define robust EIP-1559 gas fees.
        BigInteger gasLimit = BigInteger.valueOf(500_000L); // A safe limit for this function
        BigInteger maxFeePerGas = Convert.toWei("150", Convert.Unit.GWEI).toBigInteger();
        BigInteger maxPriorityFeePerGas = Convert.toWei("30", Convert.Unit.GWEI).toBigInteger();
        ContractGasProvider gasProvider = new StaticEIP1559GasProvider(chainId, maxFeePerGas, maxPriorityFeePerGas, gasLimit);

        // 4. Load the contract instance using our new, single-use transaction manager.
        DigitalGoldToken contract = DigitalGoldToken.load(contractAddress, web3j, txManager, gasProvider);

        // 5. Send the transaction.
        var transactionReceipt = contract.purchaseGold(toAddress, amount, dataHash).send();

        if (!transactionReceipt.isStatusOK()) {
            throw new RuntimeException("Minting transaction failed. Status: " + transactionReceipt.getStatus()
                + ". Revert reason: " + transactionReceipt.getRevertReason());
        }

        System.out.println("✅ Transaction successful! Hash: " + transactionReceipt.getTransactionHash());
        return transactionReceipt.getTransactionHash();
    }
}