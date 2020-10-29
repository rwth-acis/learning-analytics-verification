package i5.las2peer.services.privacyControl.Consent;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
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
public class ConsentRegistry extends Contract {
    private static final String BINARY = "0x608060405234801561001057600080fd5b50610950806100206000396000f3fe60806040526004361061006d576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806307a0315d146100725780633407bbbe146100ad5780637715ae341461013d57806381e7afc11461020e578063d253bc9914610261575b600080fd5b34801561007e57600080fd5b506100ab6004803603602081101561009557600080fd5b8101908080359060200190929190505050610330565b005b3480156100b957600080fd5b506100e6600480360360208110156100d057600080fd5b81019080803590602001909291905050506103f3565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b8381101561012957808201518184015260208101905061010e565b505050509050019250505060405180910390f35b34801561014957600080fd5b506101766004803603602081101561016057600080fd5b8101908080359060200190929190505050610503565b604051808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200184815260200183815260200180602001828103825283818151815260200191508051906020019060200280838360005b838110156101f75780820151818401526020810190506101dc565b505050509050019550505050505060405180910390f35b34801561021a57600080fd5b506102476004803603602081101561023157600080fd5b81019080803590602001909291905050506106be565b604051808215151515815260200191505060405180910390f35b34801561026d57600080fd5b5061032e6004803603604081101561028457600080fd5b8101908080359060200190929190803590602001906401000000008111156102ab57600080fd5b8201836020820111156102bd57600080fd5b803590602001918460208302840111640100000000831117156102df57600080fd5b919080806020026020016040519081016040528093929190818152602001838360200280828437600081840152601f19601f820116905080830192505050505050509192919290505050610732565b005b610339816106be565b15156103ad576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f4e6f20636f6e73656e742073746f72656420666f72207468697320757365722e81525060200191505060405180910390fd5b60606103ef6080604051908101604052803373ffffffffffffffffffffffffffffffffffffffff16815260200142815260200184815260200183815250610776565b5050565b60606103fe826106be565b1515610472576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f4e6f20636f6e73656e742073746f72656420666f72207468697320757365722e81525060200191505060405180910390fd5b6000808381526020019081526020016000206003018054806020026020016040519081016040528092919081815260200182805480156104f757602002820191906000526020600020906000905b82829054906101000a900460ff1660ff16815260200190600101906020826000010492830192600103820291508084116104c05790505b50505050509050919050565b60008060006060610513856106be565b1515610587576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f4e6f20636f6e73656e742073746f72656420666f72207468697320757365722e81525060200191505060405180910390fd5b61058f61080b565b600080878152602001908152602001600020608060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200160018201548152602001600282015481526020016003820180548060200260200160405190810160405280929190818152602001828054801561068c57602002820191906000526020600020906000905b82829054906101000a900460ff1660ff16815260200190600101906020826000010492830192600103820291508084116106555790505b505050505081525050905080600001518160200151826040015183606001518090509450945094509450509193509193565b6000806000808481526020019081526020016000209050600073ffffffffffffffffffffffffffffffffffffffff168160000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415915050919050565b6107726080604051908101604052803373ffffffffffffffffffffffffffffffffffffffff16815260200142815260200184815260200183815250610776565b5050565b806000808360400151815260200190815260200160002060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506020820151816001015560408201518160020155606082015181600301908051906020019061080492919061084d565b5090505050565b608060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff1681526020016000815260200160008019168152602001606081525090565b82805482825590600052602060002090601f016020900481019282156108e35791602002820160005b838211156108b457835183826101000a81548160ff021916908360ff1602179055509260200192600101602081600001049283019260010302610876565b80156108e15782816101000a81549060ff02191690556001016020816000010492830192600103026108b4565b505b5090506108f091906108f4565b5090565b61092191905b8082111561091d57600081816101000a81549060ff0219169055506001016108fa565b5090565b9056fea165627a7a723058201fb70bebebfda9b923fb59dd41a35682bfeb5d6ae941a4b04070cf67265148170029";

    public static final String FUNC_HASSTOREDCONSENT = "hasStoredConsent";

    public static final String FUNC_STORECONSENT = "storeConsent";

    public static final String FUNC_GETUSERCONSENTLEVELS = "getUserConsentLevels";

    public static final String FUNC_GETCONSENT = "getConsent";

    public static final String FUNC_REVOKECONSENT = "revokeConsent";

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<String, String>();
    }

    @Deprecated
    protected ConsentRegistry(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ConsentRegistry(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected ConsentRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected ConsentRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<Boolean> hasStoredConsent(byte[] userId) {
        final Function function = new Function(FUNC_HASSTOREDCONSENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> storeConsent(byte[] userId, List<BigInteger> consentLevels) {
        final Function function = new Function(
                FUNC_STORECONSENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint8>(
                        org.web3j.abi.datatypes.generated.Uint8.class,
                        org.web3j.abi.Utils.typeMap(consentLevels, org.web3j.abi.datatypes.generated.Uint8.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<List> getUserConsentLevels(byte[] userId) {
        final Function function = new Function(FUNC_GETUSERCONSENTLEVELS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint8>>() {}));
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

    public RemoteFunctionCall<Tuple4<String, BigInteger, byte[], List<BigInteger>>> getConsent(byte[] userId) {
        final Function function = new Function(FUNC_GETCONSENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<DynamicArray<Uint8>>() {}));
        return new RemoteFunctionCall<Tuple4<String, BigInteger, byte[], List<BigInteger>>>(function,
                new Callable<Tuple4<String, BigInteger, byte[], List<BigInteger>>>() {
                    @Override
                    public Tuple4<String, BigInteger, byte[], List<BigInteger>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<String, BigInteger, byte[], List<BigInteger>>(
                                (String) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (byte[]) results.get(2).getValue(), 
                                convertToNative((List<Uint8>) results.get(3).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> revokeConsent(byte[] userId) {
        final Function function = new Function(
                FUNC_REVOKECONSENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static ConsentRegistry load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ConsentRegistry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static ConsentRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ConsentRegistry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static ConsentRegistry load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new ConsentRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static ConsentRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new ConsentRegistry(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<ConsentRegistry> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(ConsentRegistry.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<ConsentRegistry> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ConsentRegistry.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<ConsentRegistry> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(ConsentRegistry.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<ConsentRegistry> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ConsentRegistry.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }
}
