package i5.las2peer.services.privacyControl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
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
    public static final String BINARY = "0x608060405234801561001057600080fd5b5061066b806100206000396000f3fe608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063140750ec1461005c5780637715ae341461012b5780637fee3b6e146101f5575b600080fd5b34801561006857600080fd5b506101296004803603604081101561007f57600080fd5b8101908080359060200190929190803590602001906401000000008111156100a657600080fd5b8201836020820111156100b857600080fd5b803590602001918460208302840111640100000000831117156100da57600080fd5b919080806020026020016040519081016040528093929190818152602001838360200280828437600081840152601f19601f820116905080830192505050505050509192919290505050610285565b005b34801561013757600080fd5b506101646004803603602081101561014e57600080fd5b81019080803590602001909291905050506102c9565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200183815260200180602001828103825283818151815260200191508051906020019060200280838360005b838110156101df5780820151818401526020810190506101c4565b5050505090500194505050505060405180910390f35b34801561020157600080fd5b5061022e6004803603602081101561021857600080fd5b81019080803590602001909291905050506103fe565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b83811015610271578082015181840152602081019050610256565b505050509050019250505060405180910390f35b6102c56080604051908101604052803373ffffffffffffffffffffffffffffffffffffffff16815260200142815260200184815260200183815250610491565b5050565b60008060606102d6610526565b600080868152602001908152602001600020608060405190810160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820154815260200160028201548152602001600382018054806020026020016040519081016040528092919081815260200182805480156103d357602002820191906000526020600020906000905b82829054906101000a900460ff1660ff168152602001906001019060208260000104928301926001038202915080841161039c5790505b5050505050815250509050806000015181604001518260600151809050935093509350509193909250565b606060008083815260200190815260200160002060030180548060200260200160405190810160405280929190818152602001828054801561048557602002820191906000526020600020906000905b82829054906101000a900460ff1660ff168152602001906001019060208260000104928301926001038202915080841161044e5790505b50505050509050919050565b806000808360400151815260200190815260200160002060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506020820151816001015560408201518160020155606082015181600301908051906020019061051f929190610568565b5090505050565b608060405190810160405280600073ffffffffffffffffffffffffffffffffffffffff1681526020016000815260200160008019168152602001606081525090565b82805482825590600052602060002090601f016020900481019282156105fe5791602002820160005b838211156105cf57835183826101000a81548160ff021916908360ff1602179055509260200192600101602081600001049283019260010302610591565b80156105fc5782816101000a81549060ff02191690556001016020816000010492830192600103026105cf565b505b50905061060b919061060f565b5090565b61063c91905b8082111561063857600081816101000a81549060ff021916905550600101610615565b5090565b9056fea165627a7a72305820bab9f42635578d0a3665689f45b1b2a7d6c37fd15fc136f61e81103d62a4cd0a0029";

    public static final String FUNC_SETCONSENT = "setConsent";

    public static final String FUNC_CHECKCONSENT = "checkConsent";

    public static final String FUNC_GETCONSENT = "getConsent";

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

    public RemoteFunctionCall<TransactionReceipt> setConsent(byte[] userId, List<BigInteger> consentLevels) {
        final Function function = new Function(
                FUNC_SETCONSENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint8>(
                        org.web3j.abi.datatypes.generated.Uint8.class,
                        org.web3j.abi.Utils.typeMap(consentLevels, org.web3j.abi.datatypes.generated.Uint8.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<List> checkConsent(byte[] userId) {
        final Function function = new Function(FUNC_CHECKCONSENT, 
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

    public RemoteFunctionCall<Tuple3<String, byte[], List<BigInteger>>> getConsent(byte[] userId) {
        final Function function = new Function(FUNC_GETCONSENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(userId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Bytes32>() {}, new TypeReference<DynamicArray<Uint8>>() {}));
        return new RemoteFunctionCall<Tuple3<String, byte[], List<BigInteger>>>(function,
                new Callable<Tuple3<String, byte[], List<BigInteger>>>() {
                    @Override
                    public Tuple3<String, byte[], List<BigInteger>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, byte[], List<BigInteger>>(
                                (String) results.get(0).getValue(), 
                                (byte[]) results.get(1).getValue(), 
                                convertToNative((List<Uint8>) results.get(2).getValue()));
                    }
                });
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
