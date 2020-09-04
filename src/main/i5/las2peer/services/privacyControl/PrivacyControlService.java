package i5.las2peer.services.privacyControl;

import java.io.File;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.web3j.tuples.generated.Tuple3;

import i5.las2peer.api.ServiceException;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.EthereumNode;
import i5.las2peer.registry.ReadWriteRegistryClient;
import i5.las2peer.registry.Util;
import i5.las2peer.registry.exceptions.EthereumException;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.serialization.MalformedXMLException;
import i5.las2peer.serialization.XmlTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
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
	
	private final static String DEFAULT_CONFIG_FILE = "etc/consentConfiguration.xml";
	
	private ReadWriteRegistryClient registryClient;
	private DataAccessRegistry dataAccessRegistry;
	private String dataAccessRegistryAddress;
	
	private TransactionLogRegistry transactionLogRegistry;
	private String transactionLogRegistryAddress;
	
	private ConsentRegistry consentRegistry;
	private String consentRegistryAddress;
	
	private Map<Integer, ConsentLevel> consentLevels;
	
	
	// ------------------------------ Initialization -----------------------------
	
	/**
	 * Initializes the privacy control service instance.
	 * Reads information about available consent levels from XML configuration file.
	 * Deploys necessary smart contracts.
	 */
	public void init() {
		// TODO: Check if there are any fields to be set?
		// setFieldValues();
		
		// Read consent levels from configuration file.
		consentLevels = new HashMap<Integer, ConsentLevel>();
		try {
			File xmlFile = new File(DEFAULT_CONFIG_FILE);
			if (xmlFile.exists()) {
				Element root = XmlTools.getRootElement(xmlFile, "las2peer:consent");
				List<Element> elements = XmlTools.getElementList(root, "consentLevel");
				for (Element elem : elements) {
					ConsentLevel cl = ConsentLevel.createFromXml(elem);
					consentLevels.put(cl.getLevel(), cl);
				}
			} else {
				// TODO Implement behavior on error
			}
		} catch (Exception e) {
			logger.warning("Unable to read from XML. Please check for correct format.");
			e.printStackTrace();
		}
		
		// Deploy smart contracts from wrapper classes
		try {
			ServiceAgentImpl agent = (ServiceAgentImpl) this.getAgent();
			EthereumNode node = (EthereumNode) agent.getRunningAtNode();
			registryClient = node.getRegistryClient();
			
			dataAccessRegistry = deployDataAccessRegistry();
			dataAccessRegistryAddress = dataAccessRegistry.getContractAddress();
			
			consentRegistry = deployConsentRegistry();
			consentRegistryAddress = consentRegistry.getContractAddress();
			
			transactionLogRegistry = deployTransactionLogRegistry();
			transactionLogRegistryAddress = transactionLogRegistry.getContractAddress();
		} catch (ServiceException e) {
			logger.warning("Initilization/Deployment of smart contracts failed!");
			e.printStackTrace();
		}
		
	}
	
	// ------------------------------ Consent handling -----------------------------
	
	private ConsentRegistry deployConsentRegistry() {
		ConsentRegistry contract = registryClient.deploySmartContract(ConsentRegistry.class, ConsentRegistry.BINARY);
		return contract;
	}

	public String getConsentRegistryAddress() {
		return consentRegistryAddress;
	}
	
	/**
	 * Function that is invoked by a LMS proxy to check for the consent of a given user.
	 * 
	 * @param User (represented by email) to check consent for.
	 * @throws EthereumException 
	 * @returns boolean True/false based on user consent.
	 */
	// TODO: Include additional parameters.
	// TODO: Check how to identify the calling service.
	public boolean checkUserConsent(String email) throws EthereumException {
		logger.warning("Service requesting consent information for user: " + email);
		
		// TODO: Set required consent level based on calling service
		ConsentLevelEnum consentLevel = ConsentLevelEnum.EXTRACTION;
		if (getUserConsent(email, consentLevel)) {
			return true;
		}
		return false;
	}

	/**
	 * Function that queries the consent of a given user from the Ethereum blockchain.
	 * 
	 * @param User (represented by email) to check consent for.
	 * @throws EthereumException 
	 * @returns boolean True/false based on user consent.
	 */
	private boolean getUserConsent(String userEmail, ConsentLevelEnum consentLevel) throws EthereumException {
		logger.warning("Getting consent level for user " + userEmail + " from ConsentRegistry.");
		boolean consentGiven = false;
		Tuple3<String, byte[], BigInteger> consentAsTuple;
		try {
			consentAsTuple = consentRegistry.getConsent(Util.padAndConvertString(userEmail, 32)).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("No consent registered.", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
		
		if (userEmail.equals(Util.recoverString(consentAsTuple.getValue2()))) {
			if (BigInteger.valueOf(consentLevel.getLevel()).compareTo(consentAsTuple.getValue3()) <= 0) {
				consentGiven = true;
				logger.warning("Consent level sufficient!");
			} else {
				logger.warning("Consent level insufficient!");
			}
		} else {
			logger.warning("UserID was not matched?!");
		}
		return consentGiven;
	}
	
	public void storeUserConsent(String userEmail, BigInteger consentLevel) throws EthereumException {
		try {
			consentRegistry.setConsent(Util.padAndConvertString(userEmail, 32), consentLevel).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Not a number?!", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
	}
	
	// ------------------------------ Consent testing -----------------------------
	
	public void storeUserConsent(String userEmail) throws EthereumException {
		BigInteger consentLevel = new BigInteger("3");
		try {
			consentRegistry.setConsent(Util.padAndConvertString(userEmail, 32), consentLevel).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Not a number?!", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
	}
	
	public BigInteger getConsentLevel(String userEmail) throws EthereumException {
		Tuple3<String, byte[], BigInteger> consentAsTuple;
		try {
			consentAsTuple = consentRegistry.getConsent(Util.padAndConvertString(userEmail, 32)).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("No consent registered.", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
		return consentAsTuple.getValue3();
	}
	
	
	
	// ------------------------- Transaction logging (draft) ----------------------------
	
		private TransactionLogRegistry deployTransactionLogRegistry() {
			TransactionLogRegistry contract = registryClient.deploySmartContract(TransactionLogRegistry.class, TransactionLogRegistry.BINARY);
			return contract;
		}
		
		public String getTransactionLogRegistryAddress() {
			return transactionLogRegistryAddress;
		}
		
		public String testLogEntries() throws EthereumException {
			try {
				createLogEntry("alice@example.org", "Moodle LMS", "err_grades_diesdas", "76582352i3uh5k2j3bjk");
				createLogEntry("alice@example.org", "Moodle LMS", "err_grades_diesjenes", "8237468276582352i3uh5k2j3bjk");
			} catch (Exception e) {
				logger.warning("Storage of log entry failed");
				e.printStackTrace();
			}
			
			return getLogEntries("alice@example.org");
		}
		
		
		public void createLogEntry(String userEmail, String service, String operation, String dataHash) throws EthereumException {
			try {
				transactionLogRegistry.createLogEntry(Util.padAndConvertString(userEmail, 32), Util.padAndConvertString(service, 32), Util.padAndConvertString(operation, 32), Util.padAndConvertString(dataHash, 32)).sendAsync().get();
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("An argument was not formatted correctly.", e);
			} catch (Exception e) {
				throw new EthereumException(e);
			}
		}
		
		@SuppressWarnings("unchecked")
		public String getLogEntries(String userEmail) throws EthereumException {
			List<BigInteger> result;
			try {
				result = (List<BigInteger>) transactionLogRegistry.getLogEntries(Util.padAndConvertString(userEmail, 32)).send();
				result.stream().forEach(s -> logger.warning("Result: " + s + " formatted: " + Instant.ofEpochMilli(s.longValue())));
			} catch (Exception e) {
				throw new EthereumException(e);
			}
			return result.toString();
		}
	
	
	// ------------------------- DataAccessRegistry (Testing only) ----------------------------
	
	private DataAccessRegistry deployDataAccessRegistry() {
		DataAccessRegistry contract = registryClient.deploySmartContract(DataAccessRegistry.class, DataAccessRegistry.BINARY);
		return contract;
	}
	
	public String getDataAccessRegistryAddress() {
		return dataAccessRegistryAddress;
	}
	
	
	public void storeDataAccess(String message) throws EthereumException {
		BigInteger bigInt = new BigInteger(message);
		try {
			dataAccessRegistry.store(bigInt).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Not a number?!", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
	}
	
	public String fetchDataAccess() throws EthereumException {
		BigInteger result = BigInteger.ZERO;
		try {
			result = dataAccessRegistry.retrieve().send();
		} catch (Exception e) {
			throw new EthereumException(e);
		}
		return result.toString();
	}
	
}
