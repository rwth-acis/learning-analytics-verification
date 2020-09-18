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
import org.web3j.tuples.generated.Tuple4;
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
    public static final String BINARY = "0x608060405234801561001057600080fd5b50610700806100206000396000f3fe608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806330bad4851461005c578063f6eccb95146101c4578063fd8d9d2d1461021d575b600080fd5b34801561006857600080fd5b506100956004803603602081101561007f57600080fd5b81019080803590602001909291905050506102ad565b6040518080602001806020018060200180602001858103855289818151815260200191508051906020019060200280838360005b838110156100e45780820151818401526020810190506100c9565b50505050905001858103845288818151815260200191508051906020019060200280838360005b8381101561012657808201518184015260208101905061010b565b50505050905001858103835287818151815260200191508051906020019060200280838360005b8381101561016857808201518184015260208101905061014d565b50505050905001858103825286818151815260200191508051906020019060200280838360005b838110156101aa57808201518184015260208101905061018f565b505050509050019850505050505050505060405180910390f35b3480156101d057600080fd5b5061021b600480360360808110156101e757600080fd5b8101908080359060200190929190803590602001909291908035906020019092919080359060200190929190505050610553565b005b34801561022957600080fd5b506102566004803603602081101561024057600080fd5b8101908080359060200190929190505050610589565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b8381101561029957808201518184015260208101905061027e565b505050509050019250505060405180910390f35b6060806060806060600080878152602001908152602001600020805490506040519080825280602002602001820160405280156102f95781602001602082028038833980820191505090505b5090506060600080888152602001908152602001600020805490506040519080825280602002602001820160405280156103425781602001602082028038833980820191505090505b50905060606000808981526020019081526020016000208054905060405190808252806020026020018201604052801561038b5781602001602082028038833980820191505090505b50905060606000808a8152602001908152602001600020805490506040519080825280602002602001820160405280156103d45781602001602082028038833980820191505090505b50905060008090505b6000808b81526020019081526020016000208054905081101561053b576000808b81526020019081526020016000208181548110151561041957fe5b906000526020600020906005020160000154858281518110151561043957fe5b90602001906020020181815250506000808b81526020019081526020016000208181548110151561046657fe5b906000526020600020906005020160020154848281518110151561048657fe5b90602001906020020181815250506000808b8152602001908152602001600020818154811015156104b357fe5b90600052602060002090600502016003015483828151811015156104d357fe5b90602001906020020181815250506000808b81526020019081526020016000208181548110151561050057fe5b906000526020600020906005020160040154828281518110151561052057fe5b906020019060200201818152505080806001019150506103dd565b50838383839750975097509750505050509193509193565b61058360a0604051908101604052804281526020018681526020018581526020018481526020018381525061065a565b50505050565b606080600080848152602001908152602001600020805490506040519080825280602002602001820160405280156105d05781602001602082028038833980820191505090505b50905060008090505b60008085815260200190815260200160002080549050811015610650576000808581526020019081526020016000208181548110151561061557fe5b906000526020600020906005020160040154828281518110151561063557fe5b906020019060200201818152505080806001019150506105d9565b5080915050919050565b60008082602001518152602001908152602001600020819080600181540180825580915050906001820390600052602060002090600502016000909192909190915060008201518160000155602082015181600101556040820151816002015560608201518160030155608082015181600401555050505056fea165627a7a7230582008b7917c0eecf27b72350cd34a9aadc3fea8953b212378e998ec379910bcc9810029";

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

    public RemoteFunctionCall<Tuple4<List<BigInteger>, List<byte[]>, List<byte[]>, List<byte[]>>> getLogEntries(byte[] userId) {
        final Function function = new Function(FUNC_GETLOGENTRIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}, new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteFunctionCall<Tuple4<List<BigInteger>, List<byte[]>, List<byte[]>, List<byte[]>>>(function,
                new Callable<Tuple4<List<BigInteger>, List<byte[]>, List<byte[]>, List<byte[]>>>() {
                    @Override
                    public Tuple4<List<BigInteger>, List<byte[]>, List<byte[]>, List<byte[]>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<List<BigInteger>, List<byte[]>, List<byte[]>, List<byte[]>>(
                                convertToNative((List<Uint256>) results.get(0).getValue()), 
                                convertToNative((List<Bytes32>) results.get(1).getValue()), 
                                convertToNative((List<Bytes32>) results.get(2).getValue()), 
                                convertToNative((List<Bytes32>) results.get(3).getValue()));
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
