package i5.las2peer.services.privacyControl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.5.5.
 */
@SuppressWarnings("rawtypes")
public class TransactionLogRegistry extends Contract {
    public static final String BINARY = "0x608060405234801561001057600080fd5b50610453806100206000396000f3fe608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806330bad4851461005c578063f6eccb95146100ec578063fd8d9d2d14610145575b600080fd5b34801561006857600080fd5b506100956004803603602081101561007f57600080fd5b81019080803590602001909291905050506101d5565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b838110156100d85780820151818401526020810190506100bd565b505050509050019250505060405180910390f35b3480156100f857600080fd5b506101436004803603608081101561010f57600080fd5b81019080803590602001909291908035906020019092919080359060200190929190803590602001909291905050506102a6565b005b34801561015157600080fd5b5061017e6004803603602081101561016857600080fd5b81019080803590602001909291905050506102dc565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b838110156101c15780820151818401526020810190506101a6565b505050509050019250505060405180910390f35b6060806000808481526020019081526020016000208054905060405190808252806020026020018201604052801561021c5781602001602082028038833980820191505090505b50905060008090505b6000808581526020019081526020016000208054905081101561029c576000808581526020019081526020016000208181548110151561026157fe5b906000526020600020906005020160000154828281518110151561028157fe5b90602001906020020181815250508080600101915050610225565b5080915050919050565b6102d660a060405190810160405280428152602001868152602001858152602001848152602001838152506103ad565b50505050565b606080600080848152602001908152602001600020805490506040519080825280602002602001820160405280156103235781602001602082028038833980820191505090505b50905060008090505b600080858152602001908152602001600020805490508110156103a3576000808581526020019081526020016000208181548110151561036857fe5b906000526020600020906005020160040154828281518110151561038857fe5b9060200190602002018181525050808060010191505061032c565b5080915050919050565b60008082602001518152602001908152602001600020819080600181540180825580915050906001820390600052602060002090600502016000909192909190915060008201518160000155602082015181600101556040820151816002015560608201518160030155608082015181600401555050505056fea165627a7a723058205481a6831832e87b2e48035856f3ce5226d03a94044db5a417facf3c290bf7d90029";

    public static final String FUNC_CREATELOGENTRY = "createLogEntry";

    public static final String FUNC_GETLOGENTRIES = "getLogEntries";

    public static final String FUNC_GETDATAHASHESFORUSER = "getDataHashesForUser";

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<String, String>();
    }

    @Deprecated
    protected TransactionLogRegistry(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TransactionLogRegistry(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected TransactionLogRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected TransactionLogRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> createLogEntry(byte[] userId, byte[] source, byte[] operation, byte[] dataHash) {
        final Function function = new Function(
                FUNC_CREATELOGENTRY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId), 
                new org.web3j.abi.datatypes.generated.Bytes32(source), 
                new org.web3j.abi.datatypes.generated.Bytes32(operation), 
                new org.web3j.abi.datatypes.generated.Bytes32(dataHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<List> getLogEntries(byte[] userId) {
        final Function function = new Function(FUNC_GETLOGENTRIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<List> getDataHashesForUser(byte[] userId) {
        final Function function = new Function(FUNC_GETDATAHASHESFORUSER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    @Deprecated
    public static TransactionLogRegistry load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TransactionLogRegistry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static TransactionLogRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TransactionLogRegistry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static TransactionLogRegistry load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new TransactionLogRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TransactionLogRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new TransactionLogRegistry(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<TransactionLogRegistry> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(TransactionLogRegistry.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<TransactionLogRegistry> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TransactionLogRegistry.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<TransactionLogRegistry> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(TransactionLogRegistry.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<TransactionLogRegistry> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TransactionLogRegistry.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }
}
