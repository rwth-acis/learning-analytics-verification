package i5.las2peer.services.privacyControl;

import java.io.File;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.w3c.dom.Element;

import i5.las2peer.api.ServiceException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.execution.ExecutionContext;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.EthereumNode;
import i5.las2peer.registry.ReadWriteRegistryClient;
import i5.las2peer.registry.Util;
import i5.las2peer.registry.exceptions.EthereumException;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.serialization.MalformedXMLException;
import i5.las2peer.serialization.XmlTools;
import i5.las2peer.tools.CryptoException;
import i5.las2peer.tools.CryptoTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import jdk.nashorn.internal.runtime.Context;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

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
@ServicePath("/Privacy")
public class PrivacyControlService extends RESTService {

	private final static L2pLogger logger = L2pLogger.getInstance(PrivacyControlService.class.getName());

	private final static String DEFAULT_CONFIG_FILE = "etc/consentConfiguration.xml";

	private EthereumNode node;
	private ReadWriteRegistryClient registryClient;

	private TransactionLogRegistry transactionLogRegistry;
	private String transactionLogRegistryAddress;

	private ConsentRegistry consentRegistry;
	private String consentRegistryAddress;

	// TODO Create lookup structure for consent levels and operations/functions/services
	private Map<Integer, ConsentLevel> consentLevelMap;

	// ------------------------------ Initialization -----------------------------

	/**
	 * Initializes the privacy control service instance.
	 * Reads information about available consent levels from XML configuration file.
	 * Deploys necessary smart contracts.
	 */
	public void init() {
		// Read consent levels from configuration file.
		// TODO allow to change the configuration at a later stage? 
		consentLevelMap = new HashMap<Integer, ConsentLevel>();
		try {
			File xmlFile = new File(DEFAULT_CONFIG_FILE);
			if (xmlFile.exists()) {
				Element root = XmlTools.getRootElement(xmlFile, "las2peer:consent");
				List<Element> elements = XmlTools.getElementList(root, "consentLevel");
				for (Element elem : elements) {
					ConsentLevel cl = ConsentLevel.createFromXml(elem);
					consentLevelMap.put(cl.getLevel(), cl);
				}
			} else {
				// TODO Implement behavior on error
			}
		} catch (Exception e) {
			logger.warning("Unable to read from XML. Please check for correct format.");
			e.printStackTrace();
		}

		// Deploy smart contracts from wrapper classes
		// TODO Check how this works with re-deployment. Might have to change towards a config file to store contract addresses.
		try {
			ServiceAgentImpl agent = (ServiceAgentImpl) this.getAgent();
			node = (EthereumNode) agent.getRunningAtNode();
			registryClient = node.getRegistryClient();

			consentRegistry = deployConsentRegistry();
			consentRegistryAddress = consentRegistry.getContractAddress();

			transactionLogRegistry = deployTransactionLogRegistry();
			transactionLogRegistryAddress = transactionLogRegistry.getContractAddress();
		} catch (ServiceException e) {
			logger.warning("Initilization/Deployment of smart contracts failed!");
			e.printStackTrace();
		}

	}
	
	// ------------------------------ Bot communication ----------------------------
	
	@GET
	@Path("/consentLevels")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Returned consent levels.") })
	public Response getConsentLevels() throws ParseException {
		logger.warning("Consent levels requested.");
		String consentLevelString = "";
		Set<Integer> consentLevels = consentLevelMap.keySet();
		
		for (Integer i : consentLevels) {
			ConsentLevel cl = consentLevelMap.get(i);
			consentLevelString += ("Level " + cl.getLevel() + ":\n");
			consentLevelString += "Authorisierte Services: ";
			for (String service : cl.getServices()) {
				consentLevelString += (service + ", ");
			}
			consentLevelString += "\n";
			consentLevelString += "Authorisierte Zugriffsoperationen: ";
			for (String func : cl.getFunctions()) {
				consentLevelString += (func + ", ");
			}
			consentLevelString += "\n\n";
		}
		
		// TODO Check how this is handled. Especially the closeContext.
		JSONObject responseBody = new JSONObject();
		responseBody.put("text", consentLevelString);
		responseBody.put("closeContext", "false");
		return Response.ok().entity(responseBody).build();
	}
	
	@POST
	@Path("/storeConsent")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Returned consent levels.") })
	public Response storeUserConsent(String body) {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		
		try {
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			logger.warning(bodyObj.toJSONString());
			
			// Get corresponding agent
			UserAgentImpl agent = getAgentFromUserEmail(bodyObj.getAsString("email"));
			if (agent == null) {
				// TODO Build error response
			}
			
			// TODO Add action parameter "level" in bot action
			// TODO Check how multiple items can be transmitted here.
			BigInteger consentLevel = new BigInteger(bodyObj.getAsString("level"));
			storeUserConsentLevels(agent.getLoginName(), Arrays.asList(consentLevel));
			
			// TODO Check how this is handled. Especially the closeContext.
			JSONObject responseBody = new JSONObject();
			responseBody.put("text", "Your consent was successfully stored.");
			responseBody.put("closeContext", "false");
			return Response.ok().entity(responseBody).build();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EthereumException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject errorMsg = new JSONObject();
		errorMsg.put("text", "Your request failed.");
		errorMsg.put("closeContext", "true");
		return Response.ok().entity(errorMsg).build();
	}

	// ------------------------------ Consent handling -----------------------------

	private ConsentRegistry deployConsentRegistry() {
		ConsentRegistry contract = registryClient.deploySmartContract(ConsentRegistry.class, ConsentRegistry.BINARY);
		return contract;
	}

	public String getConsentRegistryAddress() {
		return consentRegistryAddress;
	}

	public boolean checkUserConsent(String userEmail) throws EthereumException {
		return checkUserConsent(userEmail, "");
	}

	/**
	 * Function that is invoked by a service (e.g LMS proxy) to check for the consent of a given user.
	 * 
	 * @param User (represented by login name) to check consent for.
	 * @param Action that the consent is requested for
	 * @throws EthereumException 
	 * @returns boolean True/false based on user consent.
	 */
	public boolean checkUserConsent(String userEmail, String action) throws EthereumException {
		UserAgentImpl agent = getAgentFromUserEmail(userEmail);
		if (agent != null) {
			// Get calling service from execution context
			ServiceAgentImpl callingAgent = (ServiceAgentImpl) ExecutionContext.getCurrent().getCallerContext().getMainAgent();
			String callingAgentName = callingAgent.getServiceNameVersion().getSimpleClassName().toLowerCase();
			logger.warning("Requesting service name: " + callingAgentName);

			for (BigInteger level : getConsentLevelsForLoginName(agent.getLoginName())) {
				ConsentLevel consent = consentLevelMap.get(level.intValue());

				for (String service : consent.getServices()) {
					if (callingAgentName.contains(service.toLowerCase())) {
						
						// Check in functions if an action is transmitted.
						if (!action.isEmpty()) {
							for (String function : consent.getFunctions()) {
								if (function.toLowerCase().contains(action.toLowerCase())) {
									return true;
								}
							}
						}
					}
				}
			}			
		}
		return false;
	}

	/**
	 * Stores the consent level(s) for a user.
	 * 
	 * @param User (represented by login name) to store consent for.
	 * @param BigInteger necessary level of consent (as defined in config file)
	 * @throws EthereumException
	 */
	public void storeUserConsentLevels(String userName, List<BigInteger> consentLevels) throws EthereumException {
		try {
			consentRegistry.storeConsent(Util.padAndConvertString(userName, 32), consentLevels).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("One of the parameters used for setting the user consent is invalid.", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
	}
	
	/**
	 * Revokes the consent previously stored for the given user entirely, by storing a blank consent.
	 * 
	 * @param User (represented by login name) to revoke consent for.
	 * @throws EthereumException
	 */
	public void revokeUserConsent(String userName) throws EthereumException {
		try {
			consentRegistry.revokeConsent(Util.padAndConvertString(userName, 32)).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("One of the parameters used for setting the user consent is invalid.", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
	}

	/**
	 * Return the consent levels stored for the given user.
	 * 
	 * @param User (represented by login name) to revoke consent for.
	 * @return List of all consent levels stored for the given user
	 * @throws EthereumException
	 */
	@SuppressWarnings("unchecked")
	public List<BigInteger> getConsentLevelsForLoginName(String userName) throws EthereumException {
		List<BigInteger> consentLevels;
		try {
			consentLevels = consentRegistry.getUserConsentLevels(Util.padAndConvertString(userName, 32)).sendAsync().get();
		} catch (Exception e) {
			throw new EthereumException("No consent registered.", e);
		}
		return consentLevels;
	}


	// ------------------------------ Consent testing (to be (re)moved) -----------------------------

	public void storeUserConsentTest(String userEmail) throws EthereumException {
		UserAgentImpl agent = getAgentFromUserEmail(userEmail);

		List<BigInteger> consentLevels = new ArrayList<BigInteger>();
		consentLevels.add(new BigInteger("0"));
		consentLevels.add(new BigInteger("1"));
		try {
			consentRegistry.storeConsent(Util.padAndConvertString(agent.getLoginName(), 32), consentLevels).sendAsync().get();
		} catch (Exception e) {
			throw new EthereumException("Consent registration failed.", e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<BigInteger> getConsentLevelsForEmail(String userEmail) throws EthereumException {
		UserAgentImpl agent = getAgentFromUserEmail(userEmail);

		List<BigInteger> consentLevels;
		try {
			consentLevels = consentRegistry.getUserConsentLevels(Util.padAndConvertString(agent.getLoginName(), 32)).sendAsync().get();
		} catch (Exception e) {
			throw new EthereumException("No consent registered.", e);
		}
		return consentLevels;
	}

	// ------------------------- Transaction logging ----------------------------

	private TransactionLogRegistry deployTransactionLogRegistry() {
		TransactionLogRegistry contract = registryClient.deploySmartContract(TransactionLogRegistry.class, TransactionLogRegistry.BINARY);
		return contract;
	}

	public String getTransactionLogRegistryAddress() {
		return transactionLogRegistryAddress;
	}
	
	public void createLogEntry(String userEmail, String action, String statement) throws CryptoException, EthereumException {
		UserAgentImpl agent = getAgentFromUserEmail(userEmail);
		
		ServiceAgentImpl callingAgent = (ServiceAgentImpl) ExecutionContext.getCurrent().getCallerContext().getMainAgent();
		String callingAgentName = callingAgent.getServiceNameVersion().getSimpleClassName().toLowerCase();
		
		// Create hash to store on chain
		byte[] hash = Util.soliditySha3(statement);
		
		// TODO Store in las2peer shared storage?!
		try {
			transactionLogRegistry.createLogEntry(Util.padAndConvertString(agent.getLoginName(), 32), Util.padAndConvertString(callingAgentName, 32), Util.padAndConvertString(action, 32), hash).sendAsync().get();
		} catch (Exception e) {
			throw new EthereumException(e);
		}
	}

	public void createLogEntry(String userName, String service, String operation, String dataHash) throws EthereumException {
		try {
			transactionLogRegistry.createLogEntry(Util.padAndConvertString(userName, 32), Util.padAndConvertString(service, 32), Util.padAndConvertString(operation, 32), Util.padAndConvertString(dataHash, 32)).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("An argument was not formatted correctly.", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public String getLogEntries(String userName) throws EthereumException {
		List<BigInteger> result;
		try {
			result = (List<BigInteger>) transactionLogRegistry.getLogEntries(Util.padAndConvertString(userName, 32)).send();
			result.stream().forEach(s -> logger.warning("Result: " + s + " formatted: " + Instant.ofEpochMilli(s.longValue())));
		} catch (Exception e) {
			e.printStackTrace();
			throw new EthereumException(e);
		}
		return result.toString();
	}
	
	@SuppressWarnings("unchecked")
	public String getDataHashes(String userName) throws EthereumException {
		List<byte[]> result;
		try {
			result = (List<byte[]>) transactionLogRegistry.getDataHashesForUser(Util.padAndConvertString(userName, 32)).send();
			result.stream().forEach(s -> logger.warning("Hash: " + s.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new EthereumException(e);
		}
		return result.toString();
	}

	public String testLogEntries() throws EthereumException {
		try {
			createLogEntry("alice@example.org", "Moodle LMS", "gradereport_user_get_grade_items", "76582352i3uh5k2j3bjk");
			createLogEntry("alice@example.org", "Moodle LMS", "mod_forum_get_discussion_posts", "8237468276582352i3uh5k2j3bjk");
		} catch (Exception e) {
			logger.warning("Storage of log entry failed");
			e.printStackTrace();
		}

		return getLogEntries("alice@example.org");
	}
	
	
	// --------------------------- Helper functions ----------------------------
	
	private UserAgentImpl getAgentFromUserEmail(String userEmail) {
		UserAgentImpl agent = null;
		try {
			String agentId = node.getAgentIdForEmail(userEmail);
			agent = (UserAgentImpl) node.getAgent(agentId);
		} catch (Exception e) {
			logger.warning("Getting agent from userEmail failed.");
			e.printStackTrace();
		}
		return agent;
	}

}
