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
    public static final String BINARY = "0x608060405234801561001057600080fd5b50610ebe806100206000396000f3fe608060405260043610610078576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806307a0315d1461007d5780633407bbbe146100b85780635b1e5c57146101485780637715ae34146102ce57806381e7afc11461039f578063d253bc99146103f2575b600080fd5b34801561008957600080fd5b506100b6600480360360208110156100a057600080fd5b81019080803590602001909291905050506104c1565b005b3480156100c457600080fd5b506100f1600480360360208110156100db57600080fd5b8101908080359060200190929190505050610584565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b83811015610134578082015181840152602081019050610119565b505050509050019250505060405180910390f35b34801561015457600080fd5b506102cc6004803603608081101561016b57600080fd5b81019080803590602001909291908035906020019064010000000081111561019257600080fd5b8201836020820111156101a457600080fd5b803590602001918460208302840111640100000000831117156101c657600080fd5b919080806020026020016040519081016040528093929190818152602001838360200280828437600081840152601f19601f820116905080830192505050505050509192919290803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019064010000000081111561024657600080fd5b82018360208201111561025857600080fd5b8035906020019184600183028401116401000000008311171561027a57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610694565b005b3480156102da57600080fd5b50610307600480360360208110156102f157600080fd5b81019080803590602001909291905050506109f5565b604051808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200184815260200183815260200180602001828103825283818151815260200191508051906020019060200280838360005b8381101561038857808201518184015260208101905061036d565b505050509050019550505050505060405180910390f35b3480156103ab57600080fd5b506103d8600480360360208110156103c257600080fd5b8101908080359060200190929190505050610bb0565b604051808215151515815260200191505060405180910390f35b3480156103fe57600080fd5b506104bf6004803603604081101561041557600080fd5b81019080803590602001909291908035906020019064010000000081111561043c57600080fd5b82018360208201111561044e57600080fd5b8035906020019184602083028401116401000000008311171561047057600080fd5b919080806020026020016040519081016040528093929190818152602001838360200280828437600081840152601f19601f820116905080830192505050505050509192919290505050610c24565b005b6104ca81610bb0565b151561053e576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f4e6f20636f6e73656e742073746f72656420666f72207468697320757365722e81525060200191505060405180910390fd5b60606105806080604051908101604052803373ffffffffffffffffffffffffffffffffffffffff16815260200142815260200184815260200183815250610ce4565b5050565b606061058f82610bb0565b1515610603576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f4e6f20636f6e73656e742073746f72656420666f72207468697320757365722e81525060200191505060405180910390fd5b60008083815260200190815260200160002060030180548060200260200160405190810160405280929190818152602001828054801561068857602002820191906000526020600020906000905b82829054906101000a900460ff1660ff16815260200190600101906020826000010492830192600103820291508084116106515790505b50505050509050919050565b60606040805190810160405280600481526020017fc73c4234000000000000000000000000000000000000000000000000000000008152509050606085856040516020018083815260200180602001828103825283818151815260200191508051906020019060200280838360005b8381101561071e578082015181840152602081019050610703565b505050509050019350505050604051602081830303815290604052905073__Delegation____________________________63a491459d838387876040518563ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018080602001806020018573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200180602001848103845288818151815260200191508051906020019080838360005b838110156107fe5780820151818401526020810190506107e3565b50505050905090810190601f16801561082b5780820380516001836020036101000a031916815260200191505b50848103835287818151815260200191508051906020019080838360005b83811015610864578082015181840152602081019050610849565b50505050905090810190601f1680156108915780820380516001836020036101000a031916815260200191505b50848103825285818151815260200191508051906020019080838360005b838110156108ca5780820151818401526020810190506108af565b50505050905090810190601f1680156108f75780820380516001836020036101000a031916815260200191505b5097505050505050505060006040518083038186803b15801561091957600080fd5b505af415801561092d573d6000803e3d6000fd5b5050505061093a86610bb0565b156109ad576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f43616e6e6f74206f76657277726974652073746f72656420636f6e73656e742e81525060200191505060405180910390fd5b6109ed6080604051908101604052808673ffffffffffffffffffffffffffffffffffffffff16815260200142815260200188815260200187815250610ce4565b505050505050565b60008060006060610a0585610bb0565b1515610a79576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f4e6f20636f6e73656e742073746f72656420666f72207468697320757365722e81525060200191505060405180910390fd5b610a81610d79565b600080878152602001908152602001600020608060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001600182015481526020016002820154815260200160038201805480602002602001604051908101604052809291908181526020018280548015610b7e57602002820191906000526020600020906000905b82829054906101000a900460ff1660ff1681526020019060010190602082600001049283019260010382029150808411610b475790505b505050505081525050905080600001518160200151826040015183606001518090509450945094509450509193509193565b6000806000808481526020019081526020016000209050600073ffffffffffffffffffffffffffffffffffffffff168160000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415915050919050565b610c2d82610bb0565b15610ca0576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260208152602001807f43616e6e6f74206f76657277726974652073746f72656420636f6e73656e742e81525060200191505060405180910390fd5b610ce06080604051908101604052803373ffffffffffffffffffffffffffffffffffffffff16815260200142815260200184815260200183815250610ce4565b5050565b806000808360400151815260200190815260200160002060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060208201518160010155604082015181600201556060820151816003019080519060200190610d72929190610dbb565b5090505050565b608060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff1681526020016000815260200160008019168152602001606081525090565b82805482825590600052602060002090601f01602090048101928215610e515791602002820160005b83821115610e2257835183826101000a81548160ff021916908360ff1602179055509260200192600101602081600001049283019260010302610de4565b8015610e4f5782816101000a81549060ff0219169055600101602081600001049283019260010302610e22565b505b509050610e5e9190610e62565b5090565b610e8f91905b80821115610e8b57600081816101000a81549060ff021916905550600101610e68565b5090565b9056fea165627a7a723058204948aaa68bc915fd7bebd374abfef66e39693501f64cf75369426b61fe9193890029";

    public static final String FUNC_HASSTOREDCONSENT = "hasStoredConsent";

    public static final String FUNC_STORECONSENT = "storeConsent";

    public static final String FUNC_DELEGATEDSTORECONSENT = "delegatedStoreConsent";

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

    public RemoteFunctionCall<TransactionReceipt> delegatedStoreConsent(byte[] userId, List<BigInteger> consentLevels, String consentee, byte[] signature) {
        final Function function = new Function(
                FUNC_DELEGATEDSTORECONSENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint8>(
                        org.web3j.abi.datatypes.generated.Uint8.class,
                        org.web3j.abi.Utils.typeMap(consentLevels, org.web3j.abi.datatypes.generated.Uint8.class)), 
                new org.web3j.abi.datatypes.Address(consentee), 
                new org.web3j.abi.datatypes.DynamicBytes(signature)), 
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
