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
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

import i5.las2peer.api.Context;
import i5.las2peer.api.ServiceException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.execution.ExecutionContext;
import i5.las2peer.registry.CredentialUtils;
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
	
	private ReadWriteRegistryClient registryClient;
	private DataAccessRegistry dataAccessRegistry;
	// private ConsentRegistry consentRegistry
	
	// private ConsentRegistry consentRegistry;
	// private String consentRegistryAddress;
	
	// TODO: Init function or constructor?
	public void init() {
		// TODO: Check if there are any fields to be set?
		// setFieldValues();
		
		try {
			ServiceAgentImpl agent = (ServiceAgentImpl) this.getAgent();
			EthereumNode node = (EthereumNode) agent.getRunningAtNode();
			registryClient = node.getRegistryClient();
			dataAccessRegistry = deployDataAccessRegistry();
			
			// TODO
			// consentRegistry = deployConsentRegistry();
			

		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	
	// Basic idea for a deployment function.
	// TODO: Test implementation in registryClient.
	private DataAccessRegistry deployDataAccessRegistry() {
		DataAccessRegistry contract = registryClient.deploySmartContract(DataAccessRegistry.class, DataAccessRegistry.BINARY);
		return contract;
	}
	
//	private ConsentRegistry deployConsentRegistry() {
//		ConsentRegistry contract = registryClient.deploySmartContract(ConsentRegistry.class, ConsentRegistry.BINARY);
//		return contract;
//	}

	
	// TODO: Check how to identify the user properly
	// TODO: Allow more complex consent structures (JSON objects?)
	private void getUserConsent(String userId) {
		
	}

	private void storeUserConsent(String userId) {
		
	}
	
	// TODO:
	private void storeDataAccessOperation(String userId, Object dataAccess) {
		
	}

	private void loadDataAccessOperation(String userId) {
		
	}
}
