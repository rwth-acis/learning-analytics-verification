package i5.las2peer.services.privacyControl.TransactionLogging;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
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
    private static final String BINARY = "0x608060405234801561001057600080fd5b506107af806100206000396000f3fe608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806330bad48514610067578063bc523a68146101cf578063f6eccb9514610222578063fd8d9d2d1461027b575b600080fd5b34801561007357600080fd5b506100a06004803603602081101561008a57600080fd5b810190808035906020019092919050505061030b565b6040518080602001806020018060200180602001858103855289818151815260200191508051906020019060200280838360005b838110156100ef5780820151818401526020810190506100d4565b50505050905001858103845288818151815260200191508051906020019060200280838360005b83811015610131578082015181840152602081019050610116565b50505050905001858103835287818151815260200191508051906020019060200280838360005b83811015610173578082015181840152602081019050610158565b50505050905001858103825286818151815260200191508051906020019060200280838360005b838110156101b557808201518184015260208101905061019a565b505050509050019850505050505050505060405180910390f35b3480156101db57600080fd5b50610208600480360360208110156101f257600080fd5b81019080803590602001909291905050506105b1565b604051808215151515815260200191505060405180910390f35b34801561022e57600080fd5b506102796004803603608081101561024557600080fd5b81019080803590602001909291908035906020019092919080359060200190929190803590602001909291905050506105e2565b005b34801561028757600080fd5b506102b46004803603602081101561029e57600080fd5b8101908080359060200190929190505050610618565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b838110156102f75780820151818401526020810190506102dc565b505050509050019250505060405180910390f35b6060806060806060600080878152602001908152602001600020805490506040519080825280602002602001820160405280156103575781602001602082028038833980820191505090505b5090506060600080888152602001908152602001600020805490506040519080825280602002602001820160405280156103a05781602001602082028038833980820191505090505b5090506060600080898152602001908152602001600020805490506040519080825280602002602001820160405280156103e95781602001602082028038833980820191505090505b50905060606000808a8152602001908152602001600020805490506040519080825280602002602001820160405280156104325781602001602082028038833980820191505090505b50905060008090505b6000808b815260200190815260200160002080549050811015610599576000808b81526020019081526020016000208181548110151561047757fe5b906000526020600020906005020160000154858281518110151561049757fe5b90602001906020020181815250506000808b8152602001908152602001600020818154811015156104c457fe5b90600052602060002090600502016002015484828151811015156104e457fe5b90602001906020020181815250506000808b81526020019081526020016000208181548110151561051157fe5b906000526020600020906005020160030154838281518110151561053157fe5b90602001906020020181815250506000808b81526020019081526020016000208181548110151561055e57fe5b906000526020600020906005020160040154828281518110151561057e57fe5b9060200190602002018181525050808060010191505061043b565b50838383839750975097509750505050509193509193565b60008060016000848152602001908152602001600020541415156105d857600190506105dd565b600090505b919050565b61061260a060405190810160405280428152602001868152602001858152602001848152602001838152506106e9565b50505050565b6060806000808481526020019081526020016000208054905060405190808252806020026020018201604052801561065f5781602001602082028038833980820191505090505b50905060008090505b600080858152602001908152602001600020805490508110156106df57600080858152602001908152602001600020818154811015156106a457fe5b90600052602060002090600502016004015482828151811015156106c457fe5b90602001906020020181815250508080600101915050610668565b5080915050919050565b600080826020015181526020019081526020016000208190806001815401808255809150509060018203906000526020600020906005020160009091929091909150600082015181600001556020820151816001015560408201518160020155606082015181600301556080820151816004015550505080600001516001600083608001518152602001908152602001600020819055505056fea165627a7a72305820f15879a315c648a5efc09339001abfd02462ac5efd98aa64bfcae631dd35ce520029";

    public static final String FUNC_CREATELOGENTRY = "createLogEntry";

    public static final String FUNC_GETLOGENTRIES = "getLogEntries";

    public static final String FUNC_GETDATAHASHESFORUSER = "getDataHashesForUser";

    public static final String FUNC_HASHASHBEENRECORDED = "hasHashBeenRecorded";

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

    public RemoteFunctionCall<Boolean> hasHashBeenRecorded(byte[] hash) {
        final Function function = new Function(FUNC_HASHASHBEENRECORDED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(hash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
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
