package i5.las2peer.services.privacyControl;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.Node;
import i5.las2peer.p2p.EthereumNode;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

import i5.las2peer.api.Context;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.execution.ExecutionContext;
import i5.las2peer.registry.ReadWriteRegistryClient;
import i5.las2peer.registry.contracts.ServiceRegistry;
import i5.las2peer.registry.contracts.UserRegistry;
import i5.las2peer.registry.data.RegistryConfiguration;
import i5.las2peer.registry.exceptions.EthereumException;
import i5.las2peer.security.AgentImpl;
import i5.las2peer.security.PassphraseAgentImpl;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.security.EthereumAgent;
import i5.las2peer.serialization.SerializationException;
import i5.las2peer.serialization.SerializeTools;

/**
 * TODO: Potentially rename the service!
 * Privacy Control Service
 * 
 * This is las2peer service that manages consent and personal data access restrictions,
 * as well as logging of data access for verification purposes.
 * 
 */
@Api
@SwaggerDefinition(
		info = @Info(
				title = "Privacy Control Service",
				version = "0.1.0",
				description = "Service for consent management and data access control.",
				contact = @Contact(
						name = "Lennart Bengtson",
						url = "rwth-aachen.de",
						email = "lennart.bengtson@rwth-aachen.de")))
@ServicePath("/privacy")
public class PrivacyControlService extends RESTService {
	
	private final static L2pLogger logger = L2pLogger.getInstance(PrivacyControlService.class.getName());
	
	// private ConsentRegistry consentRegistry;
	// private String consentRegistryAddress;
	
	// TODO: Init function or constructor?
	public void init() {
		// TODO: Check if there are any fields to be set?
		// setFieldValues();
		
		
		// TODO
		// deployConsentRegistry();
		
	}
	
	
	/**
	 * Function that is invoked by a LMS proxy to check for the consent of a given user.
	 * 
	 * 
	 * @param User to check consent for.
	 * @returns boolean True/false based on user consent.
	 */
	// TODO: Implement logic. Currently only for testing message sending and access restriction... 
	// TODO: Include additional parameters.
	public boolean checkUserConsent(String email) {
		logger.warning("Service requesting consent information for user: " + email);
		boolean consentGiven = true;
		if (email.equalsIgnoreCase("alice@example.org")) {
			logger.warning("Consent not given. Permission denied.");
			// getUserConsent(input);
			consentGiven = false;
		}
		logger.warning("Consent given. Permission granted.");
		return consentGiven;
	}
	
	// TODO:
	private void logDataAccessOperation(Object someInput) {
		
	}
	
	// TODO: Check how to identify the user properly
	private void getUserConsent(String userId) {
		
	}
	
	// TODO: Check how to identify the user properly
	// TODO: Allow more complex consent structures (JSON objects?)
	private void registerUserConsent(String userId) {
		
	}
	
	private String deployConsentRegistry() {
		// TODO: Follow a similar approach?! Challenge: get credentials, GAS_PRICE etc.
		// TODO: Extend las2peer api with option to deploy smart contracts and get Contract's address in return.
		//		Example contract = Example.deploy(this.web3j,
		//				  credentials,
		//				  ManagedTransaction.GAS_PRICE,
		//				  Contract.GAS_LIMIT).send();
		String contractAddress = null;
		
		return contractAddress;
	}
	
}
