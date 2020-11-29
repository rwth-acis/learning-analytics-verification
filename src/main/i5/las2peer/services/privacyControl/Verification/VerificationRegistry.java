package i5.las2peer.services.privacyControl.Verification;

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
import org.web3j.tuples.generated.Tuple2;
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
public class VerificationRegistry extends Contract {
    private static final String BINARY = "0x608060405234801561001057600080fd5b506105b0806100206000396000f3fe608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806330bad48514610067578063bc523a681461013f578063bf829d5214610192578063fd8d9d2d146101d7575b600080fd5b34801561007357600080fd5b506100a06004803603602081101561008a57600080fd5b8101908080359060200190929190505050610267565b604051808060200180602001838103835285818151815260200191508051906020019060200280838360005b838110156100e75780820151818401526020810190506100cc565b50505050905001838103825284818151815260200191508051906020019060200280838360005b8381101561012957808201518184015260208101905061010e565b5050505090500194505050505060405180910390f35b34801561014b57600080fd5b506101786004803603602081101561016257600080fd5b81019080803590602001909291905050506103d4565b604051808215151515815260200191505060405180910390f35b34801561019e57600080fd5b506101d5600480360360408110156101b557600080fd5b810190808035906020019092919080359060200190929190505050610405565b005b3480156101e357600080fd5b50610210600480360360208110156101fa57600080fd5b810190808035906020019092919050505061042d565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b83811015610253578082015181840152602081019050610238565b505050509050019250505060405180910390f35b6060806060600080858152602001908152602001600020805490506040519080825280602002602001820160405280156102b05781602001602082028038833980820191505090505b5090506060600080868152602001908152602001600020805490506040519080825280602002602001820160405280156102f95781602001602082028038833980820191505090505b50905060008090505b600080878152602001908152602001600020805490508110156103c6576000808781526020019081526020016000208181548110151561033e57fe5b906000526020600020906003020160000154838281518110151561035e57fe5b90602001906020020181815250506000808781526020019081526020016000208181548110151561038b57fe5b90600052602060002090600302016002015482828151811015156103ab57fe5b90602001906020020181815250508080600101915050610302565b508181935093505050915091565b60008060016000848152602001908152602001600020541415156103fb5760019050610400565b600090505b919050565b610429606060405190810160405280428152602001848152602001838152506104fe565b5050565b606080600080848152602001908152602001600020805490506040519080825280602002602001820160405280156104745781602001602082028038833980820191505090505b50905060008090505b600080858152602001908152602001600020805490508110156104f457600080858152602001908152602001600020818154811015156104b957fe5b90600052602060002090600302016002015482828151811015156104d957fe5b9060200190602002018181525050808060010191505061047d565b5080915050919050565b60008082602001518152602001908152602001600020819080600181540180825580915050906001820390600052602060002090600302016000909192909190915060008201518160000155602082015181600101556040820151816002015550505080600001516001600083604001518152602001908152602001600020819055505056fea165627a7a723058203bcdae26d0197d33ae18cfe924572270b455ac396354e4eb642b6a0c812bff950029";

    public static final String FUNC_CREATELOGENTRY = "createLogEntry";

    public static final String FUNC_GETLOGENTRIES = "getLogEntries";

    public static final String FUNC_GETDATAHASHESFORUSER = "getDataHashesForUser";

    public static final String FUNC_HASHASHBEENRECORDED = "hasHashBeenRecorded";

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<String, String>();
    }

    @Deprecated
    protected VerificationRegistry(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected VerificationRegistry(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected VerificationRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected VerificationRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> createLogEntry(byte[] userId, byte[] dataHash) {
        final Function function = new Function(
                FUNC_CREATELOGENTRY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId), 
                new org.web3j.abi.datatypes.generated.Bytes32(dataHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple2<List<BigInteger>, List<byte[]>>> getLogEntries(byte[] userId) {
        final Function function = new Function(FUNC_GETLOGENTRIES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}, new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteFunctionCall<Tuple2<List<BigInteger>, List<byte[]>>>(function,
                new Callable<Tuple2<List<BigInteger>, List<byte[]>>>() {
                    @Override
                    public Tuple2<List<BigInteger>, List<byte[]>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<List<BigInteger>, List<byte[]>>(
                                convertToNative((List<Uint256>) results.get(0).getValue()), 
                                convertToNative((List<Bytes32>) results.get(1).getValue()));
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
    public static VerificationRegistry load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new VerificationRegistry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static VerificationRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new VerificationRegistry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static VerificationRegistry load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new VerificationRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static VerificationRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new VerificationRegistry(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<VerificationRegistry> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(VerificationRegistry.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<VerificationRegistry> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(VerificationRegistry.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<VerificationRegistry> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(VerificationRegistry.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<VerificationRegistry> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(VerificationRegistry.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }
}
