package i5.las2peer.services.privacyControl;

import java.io.File;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.w3c.dom.Element;
import org.web3j.tuples.generated.Tuple4;

import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.ServiceException;
import i5.las2peer.api.security.AgentException;
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
import i5.las2peer.services.privacyControl.Consent.ConsentLevel;
import i5.las2peer.services.privacyControl.Consent.ConsentRegistry;
import i5.las2peer.services.privacyControl.TransactionLogging.LogEntry;
import i5.las2peer.services.privacyControl.TransactionLogging.TransactionLogRegistry;
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
@ServicePath("/privacy")
public class PrivacyControlService extends RESTService {

	private final static L2pLogger logger = L2pLogger.getInstance(PrivacyControlService.class.getName());

	private final static String DEFAULT_CONFIG_FILE = "etc/consentConfiguration.xml";

	private static EthereumNode node;
	private static ReadWriteRegistryClient registryClient;

	private static TransactionLogRegistry transactionLogRegistry;
	private static String transactionLogRegistryAddress;

	private static ConsentRegistry consentRegistry;
	private static String consentRegistryAddress;

	private static HashMap<Integer, ConsentLevel> consentLevelMap = new HashMap<Integer, ConsentLevel>();
	private static HashMap<String, String> consentProcessingActive = new HashMap<String, String>();
	
	private static boolean initialized = false;

	// ------------------------------ Initialization -----------------------------
	
	public PrivacyControlService() {
		super();
		
		// Workaround, as each call to the service triggers the constructor.
		// TODO Find proper solution
		if (!initialized) {
			// Wait for service to be started before executing the initialization
			// Necessary because node (the service is running at) would not be known otherwise
			new java.util.Timer().schedule( 
			        new java.util.TimerTask() {
			            @Override
			            public void run() {
			                init();
			            }
			        }, 
			        10000 
			);
		}
	}
	
	/**
	 * Initializes the privacy control service instance.
	 * Reads information about available consent levels from XML configuration file.
	 * Loads/Deploys necessary smart contracts.
	 */
	public void init() {
		logger.info("Initializing privacy control service...");
		
		// Read consent levels from configuration file.
		try {
			File xmlFile = new File(DEFAULT_CONFIG_FILE);
			Element root = XmlTools.getRootElement(xmlFile, "las2peer:consent");
			List<Element> elements = XmlTools.getElementList(root, "consentLevel");
			for (Element elem : elements) {
				ConsentLevel cl = ConsentLevel.createFromXml(elem);
				consentLevelMap.put(cl.getLevel(), cl);
			}
		} catch (Exception e) {
			logger.warning("Unable to read from XML. Please make sure file exists and is correctly formatted.");
			e.printStackTrace();
		}
		logger.info("Successfully read from XML consent configuration file.");
		
		logger.info("Reading contract addresses from configuration file.");
		
		// Get smart contract addresses from configuration file.
		LaRegistryConfiguration config = new LaRegistryConfiguration();
		consentRegistryAddress = config.getConsentRegistryAddress();
		transactionLogRegistryAddress = config.getTransactionLogRegistryAddress();
		
		logger.info("TransactionLogRegistry deployed at: " + transactionLogRegistryAddress);
		logger.info("ConsentRegistry deployed at: " + consentRegistryAddress);
		
		logger.info("Loading deployed smart contracts...");
		
		// Deploy smart contracts from wrapper classes
		try {
			ServiceAgentImpl agent = (ServiceAgentImpl) this.getAgent();
			node = (EthereumNode) agent.getRunningAtNode();
			registryClient = node.getRegistryClient();

			consentRegistry = deployConsentRegistry();
			transactionLogRegistry = deployTransactionLogRegistry();
			initialized = true;
		} catch (ServiceException e) {
			logger.warning("Initilization of smart contracts failed!");
			e.printStackTrace();
		}
		
		logger.info("Done. Proceeding");
	}

	// ------------------------------ Bot communication ----------------------------

	@GET
	@Path("/consentLevels")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Returned consent levels.") })
	public Response consentLevels() throws ParseException {
		String consentLevelString = getConsentLevelsFormatted();
		JSONObject responseBody = new JSONObject();
		responseBody.put("text", "" + consentLevelString);
		responseBody.put("closeContext", "true");
		return Response.ok().entity(responseBody).build();
	}

	@POST
	@Path("/storeConsent")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Set consent level.") })
	public Response storeConsent(String body) {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		
		try {
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			String channel = bodyObj.getAsString("channel");
			
			// Get corresponding agent
			UserAgentImpl agent = getAgentFromUserEmail(bodyObj.getAsString("email"));
			if (agent == null) {
				JSONObject err = new JSONObject();
				err.put("text", "No las2peer agent is available for your registered email. Make sure you have a las2peer account set up.");
				err.put("closeContext", "true");
				return Response.ok().entity(err).build();
			}
			
			// Check if consent storage was already started.
			if (consentProcessingActive.get(channel) != null) {
				logger.info("Storing consent for user " + agent.getLoginName());
				
				// Get consent level from message
				String chosenConsentLevels = bodyObj.getAsString("msg").split("\\.")[0];
				
				List<BigInteger> levels = new ArrayList<BigInteger>();
				for (String s : chosenConsentLevels.split("\\,")) {
					// TODO Validate if consent object with that level exists!
					levels.add(new BigInteger(s));
				}
				
				storeUserConsentLevels(agent.getLoginName(), levels);
				
				consentProcessingActive.remove(channel);
				
				// Build response and close context.
				JSONObject res = new JSONObject();
				res.put("text", "Einwilligung erfolgreich gespeichert.");
				res.put("closeContext", "true");
				return Response.ok().entity(res).build();
			} else {
				logger.info("Starting consent storage for user " + agent.getLoginName());
				consentProcessingActive.put(channel, "Storage");
				String consentLevelString = getConsentLevelsFormatted();
				
				// Build response and close context.
				JSONObject res = new JSONObject();
				res.put("text", consentLevelString + "Bitte gib die Nummern (mehrere getrennt mit ',') der Einwilligungslevel an, mit denen du einverstanden bist.");
				res.put("closeContext", "false");
				return Response.ok().entity(res).build();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EthereumException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject err = new JSONObject();
		err.put("text", "Etwas ist bei der Anfrage schiefgegangen.");
		err.put("closeContext", "true");
		return Response.ok().entity(err).build();
	}


	@POST
	@Path("/showConsent")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Consent retrieved and displayed.") })
	public Response showConsent(String body) {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

		try {
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			
			// Get corresponding agent
			UserAgentImpl agent = getAgentFromUserEmail(bodyObj.getAsString("email"));
			if (agent == null) {
				JSONObject err = new JSONObject();
				err.put("text", "No las2peer agent is available for your registered email. Make sure you have a las2peer account set up.");
				err.put("closeContext", "true");
				return Response.ok().entity(err).build();
			}
			
			// Retrieve stored consent levels
			List<BigInteger> givenConsent = getConsentLevelsForLoginName(agent.getLoginName());
			
			String resText = "";
			if (givenConsent == null || givenConsent.isEmpty()) {
				resText +=  "Aktuell liegt uns keine Einwilligung vor";
			} else {
				resText += "Aktuell liegt eine Einwilligung zu den folgenden Datenverarbeitungsleveln vor: \n";
				for (BigInteger consent : givenConsent) {
					resText += consentLevelMap.get(consent.intValue()).toString();
					resText += "\n";
				}
			}

			JSONObject res = new JSONObject();
			res.put("text", resText);
			res.put("closeContext", "true");
			return Response.ok().entity(res).build();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EthereumException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject err = new JSONObject();
		err.put("text", "Etwas ist bei der Anfrage schiefgegangen.");
		err.put("closeContext", "true");
		return Response.ok().entity(err).build();
	}

	@POST
	@Path("/revokeConsent")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Revoked consent.") })
	public Response revokeConsent(String body) {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

		try {
			JSONObject bodyObj = (JSONObject) parser.parse(body);

			// Get corresponding agent
			UserAgentImpl agent = getAgentFromUserEmail(bodyObj.getAsString("email"));
			if (agent == null) {
				JSONObject errorMsg = new JSONObject();
				errorMsg.put("text", "Your request failed.");
				errorMsg.put("closeContext", "true");
				return Response.ok().entity(errorMsg).build();
			}

			revokeUserConsent(agent.getLoginName());

			JSONObject responseBody = new JSONObject();
			responseBody.put("text", "Your consent was successfully revoked.");
			responseBody.put("closeContext", "true");
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

	@POST
	@Path("/showLogEntries")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Showed log entries.") })
	public Response showLogEntries(String body) {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

		try {
			JSONObject bodyObj = (JSONObject) parser.parse(body);

			// Get corresponding agent
			UserAgentImpl agent = getAgentFromUserEmail(bodyObj.getAsString("email"));
			if (agent == null) {
				JSONObject errorMsg = new JSONObject();
				errorMsg.put("text", "Your request failed.");
				errorMsg.put("closeContext", "true");
				return Response.ok().entity(errorMsg).build();
			}
			
			logger.info("Requesting logs for user " + agent.getLoginName());
			
			String res = "";
			res += getLogEntries(agent.getLoginName()).toString();

			logger.info("Showing logs: " + res);
			
			JSONObject responseBody = new JSONObject();
			responseBody.put("text", res);
			responseBody.put("closeContext", "true");
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
		// ConsentRegistry contract = registryClient.deploySmartContract(ConsentRegistry.class, ConsentRegistry.BINARY);
		ConsentRegistry contract = registryClient.loadSmartContract(ConsentRegistry.class, consentRegistryAddress);
		return contract;
	}

	public String getConsentRegistryAddress() {
		return consentRegistryAddress;
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
			logger.info("Requesting service name: " + callingAgentName);

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
			throw new IllegalArgumentException("One of the parameters used for revoking the user consent is invalid.", e);
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

	// ------------------------- Transaction logging ----------------------------

	private TransactionLogRegistry deployTransactionLogRegistry() {
		// TransactionLogRegistry contract = registryClient.deploySmartContract(TransactionLogRegistry.class, TransactionLogRegistry.BINARY);
		TransactionLogRegistry contract = registryClient.loadSmartContract(TransactionLogRegistry.class, transactionLogRegistryAddress);
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

	@SuppressWarnings("deprecation")
	public String getLogEntries(String userName) throws EthereumException {
		Tuple4<List<BigInteger>, List<byte[]>, List<byte[]>, List<byte[]>> initialResult;
		String result = "";
		try {
			initialResult = transactionLogRegistry.getLogEntries(Util.padAndConvertString(userName, 32)).send();
			
			List<BigInteger> timestamps = initialResult.getValue1();
			List<byte[]> sources = initialResult.getValue2();
			List<byte[]> operations = initialResult.getValue3();
			List<byte[]> hashes = initialResult.getValue4();
			
			List<LogEntry> logs = new ArrayList<LogEntry>();
			
			for (int i = 0; i < timestamps.size(); i++) {
				logger.warning("Found logentry with index " + i);
				LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamps.get(i).longValue()),
                        TimeZone.getDefault().toZoneId());
				LogEntry entry = new LogEntry(date, Util.recoverString(sources.get(i)), Util.recoverString(operations.get(i)), Util.bytesToHexString(hashes.get(i)));
				logs.add(entry);
			}
			
			for (LogEntry l : logs) {
				result += l.toString();
			}
			logger.warning("Printing " + logs.size() + " entries: \n" + result);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EthereumException(e);
		}
		return result;
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

	// --------------------------- Utility functions ----------------------------

	public UserAgentImpl getAgentFromUserEmail(String userEmail) {
		UserAgentImpl agent = null;
		try {
			String agentId = node.getAgentIdForEmail(userEmail);
			agent = (UserAgentImpl) node.getAgent(agentId);
		} catch (AgentNotFoundException e) {
			logger.warning("Agent not found.");
			e.printStackTrace();
		} catch (AgentOperationFailedException e) {
			logger.warning("Agent operation failed.");
			e.printStackTrace();
		} catch (AgentException e) {
			logger.warning("Something else went wrong.");
			e.printStackTrace();
		}
		return agent;
	}
	
	private String getConsentLevelsFormatted() {
		String consentLevelString = "";
		Set<Integer> consentLevels = consentLevelMap.keySet();

		for (Integer i : consentLevels) {
			ConsentLevel cl = consentLevelMap.get(i);
			consentLevelString += cl.toString();
			consentLevelString += "\n";
		}
		return consentLevelString;
	}

}
