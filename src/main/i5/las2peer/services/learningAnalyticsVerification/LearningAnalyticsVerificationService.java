package i5.las2peer.services.learningAnalyticsVerification;

import java.io.File;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.w3c.dom.Element;

import i5.las2peer.api.Context;
import i5.las2peer.execution.ExecutionContext;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.EthereumNode;
import i5.las2peer.registry.ReadWriteRegistryClient;
import i5.las2peer.registry.Util;
import i5.las2peer.registry.exceptions.EthereumException;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.serialization.XmlTools;
import i5.las2peer.services.learningAnalyticsVerification.Consent.ConsentLevel;
import i5.las2peer.services.learningAnalyticsVerification.Consent.ConsentRegistry;
import i5.las2peer.services.learningAnalyticsVerification.Verification.VerificationRegistry;
import i5.las2peer.tools.CryptoException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * Learning Analytics Verification Service
 * 
 * This is a las2peer service that manages consent and access restrictions to personal LA data,
 * as well as the creation of references to LA data for verification purposes.
 * 
 */
@Api
@SwaggerDefinition(
		info = @Info(
				title = "Learning Analytics Verification Service",
				version = "1.0.0",
				description = "Service for consent management and verification of learning analytics data.",
				contact = @Contact(
						name = "Lennart Bengtson",
						url = "rwth-aachen.de",
						email = "lennart.bengtson@rwth-aachen.de")))
@ServicePath("/verification")
public class LearningAnalyticsVerificationService extends RESTService {

	private final static L2pLogger logger = L2pLogger.getInstance(LearningAnalyticsVerificationService.class.getName());

	private final static String DEFAULT_CONFIG_FILE = "etc/consentConfiguration.xml";
	private final static String DEFAULT_MESSAGES_DIR = "etc/messages";

	private static ReadWriteRegistryClient registryClient;

	private static VerificationRegistry verificationRegistry;
	private static String verificationRegistryAddress;

	private static ResourceBundle userMessages;
	
	private static ConsentRegistry consentRegistry;
	private static String consentRegistryAddress;

	private static HashMap<Integer, ConsentLevel> consentLevelMap = new HashMap<Integer, ConsentLevel>();
	private static ConcurrentMap<String, List<BigInteger>> consentCache = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, String> userToMoodleToken = new ConcurrentHashMap<>();

	private static HashSet<String> consentProcessing = new HashSet<String>();
	private static HashSet<String> choosingFunction = new HashSet<String>();
	private static HashSet<String> processingAction = new HashSet<String>();

	private static boolean initialized = false;

	// ------------------------------ Initialization -----------------------------

	public LearningAnalyticsVerificationService() {
	}

	@POST
	@Path("/init")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "LA Verification Service initialized") })
	public Response init() {
		if (initialized) {
			return Response.status(Status.BAD_REQUEST).entity("Already initialized").build();
		}
		logger.info("Initializing LA verification service...");
		
		try {
			// Read texts from resourceBundle
			File file = new File(DEFAULT_MESSAGES_DIR);
	        URL[] urls = {file.toURI().toURL()};
	        ClassLoader loader = new URLClassLoader(urls);
	        userMessages = ResourceBundle.getBundle("UserMessages", Locale.getDefault(), loader);	
		} catch (Exception e) {
			e.printStackTrace();
		}

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
			return Response.status(Status.BAD_REQUEST).entity("Unable to read consent configuration").build();
		}
		logger.info("Successfully read from XML consent configuration file.");

		// Get smart contract addresses from configuration file.
		LaRegistryConfiguration config = new LaRegistryConfiguration();
		consentRegistryAddress = config.getConsentRegistryAddress();
		verificationRegistryAddress = config.getVerificationRegistryAddress();

		logger.info("Sucessfully loaded migrated smart contracts...");
		logger.info("VerificationRegistry deployed at: " + verificationRegistryAddress);
		logger.info("ConsentRegistry deployed at: " + consentRegistryAddress);

		// Deploy smart contracts from wrapper classes
		try {
			ServiceAgentImpl agent = (ServiceAgentImpl) Context.getCurrent().getServiceAgent();
			registryClient = ((EthereumNode) agent.getRunningAtNode()).getRegistryClient();

			consentRegistry = deployConsentRegistry();
			verificationRegistry = deployVerificationRegistry();
			initialized = true;
		} catch (Exception e) {
			logger.warning("Initilization of smart contracts failed!");
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).entity("Initilization of smart contracts failed!").build();
		}
		return Response.status(Status.OK).entity("Initialization successful.").build();
	}

	// ------------------------------ Bot communication ----------------------------

	@POST
	@Path("/showConsentMenu")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Returned menu.") })
	public Response showConsentMenu(String body) throws ParseException {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		String channel = "";

		try {
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			channel = bodyObj.getAsString("channel");
			String email = bodyObj.getAsString("email");

			if (email == null || email.isEmpty()) {
				JSONObject err = new JSONObject();
				err.put("text", userMessages.getString("errEmailAdressNotFound"));
				err.put("closeContext", "true");
				return Response.ok().entity(err).build();
			}

			if (processingAction.contains(channel)) {
				logger.info("Got message while processing...");
				JSONObject res = new JSONObject();
				res.put("text", userMessages.getString("errWaitForActionToBeFinished"));
				res.put("closeContext", "false");
				return Response.ok().entity(res).build();
				
			} else if (consentProcessing.contains(channel)) {
				// Proceed with storage
				logger.info("Continuing consent storage for user " + email);

				// Get consent level from message
				String chosenConsentLevels = bodyObj.getAsString("msg").split("\\.")[0];

				try {
					List<BigInteger> levels = new ArrayList<BigInteger>();
					for (String s : chosenConsentLevels.split("\\,")) {
						if (!consentLevelMap.containsKey(Integer.valueOf(s))) {
							throw new NumberFormatException(s);
						}
						levels.add(new BigInteger(s));
					}
					consentProcessing.remove(channel);
					
					processingAction.add(channel);
					storeUserConsentLevels(email, levels);
					processingAction.remove(channel);
					
					// Build response and close context.
					JSONObject res = new JSONObject();
					res.put("text", userMessages.getString("confirmationConsentStorage"));
					res.put("closeContext", "true");
					return Response.ok().entity(res).build();

				} catch (NumberFormatException e) {
					// Build error but don't close context when ID is not valid.
					JSONObject err = new JSONObject();
					err.put("text", userMessages.getString("pleaseInsertValidID"));
					err.put("closeContext", "false");
					return Response.ok().entity(err).build();
				} catch (EthereumException e) {
					e.printStackTrace();
				}
				consentProcessing.remove(channel);
				
			} else if (choosingFunction.contains(channel)) {
				// If options have been displayed, parse number and evaluate which function to execute.
				String chosenOption = bodyObj.getAsString("msg").split("\\.")[0];
				try {
					int i = Integer.valueOf(chosenOption);
					choosingFunction.remove(channel);
					JSONObject res = new JSONObject();

					switch (i) {
					case 0: // abort
						logger.info("Abort choosing function to execute...");
						res = new JSONObject();
						res.put("text", "");
						res.put("closeContext", "true");
						return Response.ok().entity(res).build();

					case 1: // storeConsent(body);
						logger.info("Starting consent storage for user " + email);
						consentProcessing.add(channel);
						String consentLevels = getConsentLevelsFormatted();

						StringBuilder storageStringBuilder = new StringBuilder();
						storageStringBuilder.append(userMessages.getString("menuConsentOptionsExplanation"));
						storageStringBuilder.append(consentLevels);
						storageStringBuilder.append(userMessages.getString("menuConsentOptionsPrompt"));
						
						res = new JSONObject();
						res.put("text", storageStringBuilder.toString());
						res.put("closeContext", "false");
						return Response.ok().entity(res).build();

					case 2: // showConsent(body);
						List<BigInteger> givenConsent;
						try {
							givenConsent = getConsentLevelsForUserEmail(email);
							StringBuilder stringBuilder = new StringBuilder();
							if (givenConsent == null || givenConsent.isEmpty()) {
								stringBuilder.append(userMessages.getString("noConsentForUserFound"));
							} else {
								stringBuilder.append(userMessages.getString("followingConsentForUserFound"));
								for (BigInteger consent : givenConsent) {
									stringBuilder.append(consentLevelMap.get(consent.intValue()).toString());
									stringBuilder.append("\n");
								}
							}

							res = new JSONObject();
							res.put("text", stringBuilder.toString());
							res.put("closeContext", "true");
							return Response.ok().entity(res).build();
						} catch (EthereumException e) {
							e.printStackTrace();
						}
						break;

					case 3: // revokeConsent(body);
						logger.info("Revoking consent for user " + email);
						try {
							processingAction.add(channel);
							revokeUserConsent(email);
							processingAction.remove(channel);
							
							res = new JSONObject();
							res.put("text", userMessages.getString("confirmationPreferencesUpdated"));
							res.put("closeContext", "true");
							return Response.ok().entity(res).build();
						} catch (EthereumException e) {
							e.printStackTrace();
						}
						break;

					default:
						break;
					}

				} catch (NumberFormatException e) {
					JSONObject err = new JSONObject();
					err.put("text", userMessages.getString("errChooseFunctionValidID"));
					err.put("closeContext", "false");
					return Response.ok().entity(err).build();
				}
				
			} else {
				choosingFunction.add(channel);
				JSONObject res = new JSONObject();
				res.put("text", userMessages.getString("menuConsentManagementPrompt"));
				res.put("closeContext", "false");
				return Response.ok().entity(res).build();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		choosingFunction.remove(channel);
		processingAction.remove(channel);
		
		JSONObject res = new JSONObject();
		res.put("text", userMessages.getString("errSomethingWentWrong"));
		res.put("closeContext", "true");
		return Response.ok().entity(res).build();
	}
	
	@POST
	@Path("/showServiceInformation")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Delivered service information text.") })
	public Response showServiceInformation(String body) {
		logger.info("Showing service information...");
		StringBuilder resBuilder = new StringBuilder();
		
		resBuilder.append("Hi, \n");
		resBuilder.append("du bist hier, weil einer deiner Kurse mit Learning Analytics analysiert wird. \n");
		resBuilder.append("Das bedeutet, wenn du Funktionen im Kursraum (z.B. im Moodle) nutzt, werden dabei Daten erzeugt. ");
		resBuilder.append("Zum Beispiel wird erfasst, wenn du ein Quiz abschliesst, eine Note bekommst, in ein Forum postest oder dir Material ansiehst. ");
		resBuilder.append("Diese Daten werden dann analysiert, z.B. um dir persoenliches Feedback zu deinem Lernprozess zu geben. \n" );
		resBuilder.append("Ich kann dir dabei helfen, mehr Kontrolle ueber deine persoenlichen Daten aus der Lernumgebung zu erhalten. \n \n");
		resBuilder.append("Erstens kannst du hier festlegen, welche Daten aus der Lernumgebung (Moodle) zur Analyse weiter gesendet werden. \n");
		resBuilder.append("Du legst dabei fest welche Art von Daten, aus welchen Systemen verwendet werden darf. \n");
		resBuilder.append("Wenn du es dir spaeter anders ueberlegst, kannst du hier die Zustimmung jederzeit wieder aendern. \n");
		resBuilder.append("Denk daran, dass es natuerlich die Qualitaet deines Feedbacks beeinflusst, wenn weniger aussagekraeftige Daten ueber dich vorliegen. \n");
		resBuilder.append("Schreib mich dazu an mit: 'optionen'\n \n");
		resBuilder.append("Zweitens kannst du dir die ueber dich gesammelten Daten anzeigen lassen. \n");
		resBuilder.append("Dabei werden dir alle Daten zu deiner Person angezeigt, die aus der Lernumgebung entnommen und zur Analyse abgespeichert wurden. \n");
		resBuilder.append("Bei jeder Entnahme deiner Daten aus der Lernumgebung wird eine Referenz dazu auf einer Blockchain abgelegt. ");
	    resBuilder.append("Beim Anzeigen deiner Daten werden diese mit der Blockchain-Referenz verglichen, sodass du sehen kannst, ob der Datensatz veraendert worden ist. ");
		resBuilder.append("Selbstverstaendlich werden zur Analyse nur verifizierte (nicht manipulierte) Daten verwendet. \n");
		resBuilder.append("Schreib mich dazu an mit: 'daten'\n \n");

		JSONObject res = new JSONObject();
		res.put("text", resBuilder.toString());
		res.put("closeContext", "true");
		return Response.ok().entity(res).build();
	}

	@POST
	@Path("/showLrsData")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Retrieved relevant learning analytics data.") })
	public Response showLrsData(String body) {
		logger.info("Data requested...");
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		StringBuilder resBuilder = new StringBuilder();
		
		try {
			resBuilder.append(userMessages.getString("followingDataStoredForUser"));
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			resBuilder.append(getStatementsForUserEmail(bodyObj.getAsString("email")));
			resBuilder.append(userMessages.getString("unverifiedDataNotUsed"));
			resBuilder.append("\n");
			resBuilder.append(userMessages.getString("menuDataDisplayCommand"));
		} catch (ParseException e) {
			e.printStackTrace();
			resBuilder.append(userMessages.getString("errSomethingWentWrong"));
		} 
		

		JSONObject res = new JSONObject();
		res.put("text", resBuilder.toString());
		res.put("closeContext", "true");
		return Response.ok().entity(res).build();
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
	public boolean checkUserConsent(String email, String action) throws EthereumException {
		// Get calling service from execution context
		ServiceAgentImpl callingAgent = (ServiceAgentImpl) ExecutionContext.getCurrent().getCallerContext().getMainAgent();
		String callingAgentName = callingAgent.getServiceNameVersion().getSimpleClassName().toLowerCase();

		for (BigInteger level : getConsentLevelsForUserEmail(email)) {
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
		// TODO Create log entries for failed extraction attempts?
		return false;
	}

	/**
	 * Stores given consent level(s) for a user.
	 * 
	 * @param User (represented by login name) to store consent for.
	 * @param BigInteger necessary level of consent (as defined in config file)
	 * @throws EthereumException
	 */
	public void storeUserConsentLevels(String email, List<BigInteger> consentLevels) throws EthereumException {
		byte[] emailHash = Util.soliditySha3(email); 
		try {
			consentRegistry.storeConsent(emailHash, consentLevels).sendAsync().get();
			if (consentCache.containsKey(email)) {				
				logger.info("Resetting consent cache.");
				consentCache.remove(email);
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("One of the parameters used for setting the user consent is invalid.", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
	}

	/**
	 * Revokes consent previously stored for the given user by storing blank consent.
	 * 
	 * @param User (represented by login name) to revoke consent for.
	 * @throws EthereumException
	 */
	public void revokeUserConsent(String email) throws EthereumException {
		byte[] emailHash = Util.soliditySha3(email); 
		try {
			consentRegistry.revokeConsent(emailHash).sendAsync().get();
			if (consentCache.containsKey(email)) {				
				logger.info("Resetting consent cache.");
				consentCache.remove(email);
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("One of the parameters used for revoking the user consent is invalid.", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
	}

	/**
	 * Returns consent levels stored for the given user.
	 * 
	 * @param User (represented by login name) to revoke consent for.
	 * @return List of all consent levels stored for the given user
	 * @throws EthereumException
	 */
	@SuppressWarnings("unchecked")
	public List<BigInteger> getConsentLevelsForUserEmail(String email) throws EthereumException {
		if (consentCache.containsKey(email)) {
			logger.info("Loading consent from cache.");
			return consentCache.get(email);
		}

		byte[] emailHash = Util.soliditySha3(email); 
		
		// Try to get information from the blockchain and return.
		List<BigInteger> consentLevels;
		try {
			consentLevels = consentRegistry.getUserConsentLevels(emailHash).sendAsync().get();
			consentCache.put(email, consentLevels);
		} catch (Exception e) {
			throw new EthereumException("No consent registered.", e);
		}
		return consentLevels;
	}

	// ------------------------- Verification ----------------------------

	private VerificationRegistry deployVerificationRegistry() {
		VerificationRegistry contract = registryClient.loadSmartContract(VerificationRegistry.class, verificationRegistryAddress);
		return contract;
	}

	public String getVerificationRegistryAddress() {
		return verificationRegistryAddress;
	}

	/**
	 * Creates log and hash based on the given xAPIstatement and stores information on the Ethereum blockchain.
	 * 
	 * @param xApiStatement for which to create the log entry.
	 * @throws CryptoException
	 * @throws EthereumException
	 */
	public void createLogEntry(String xApiStatement) throws CryptoException, EthereumException {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject statement = new JSONObject();
		try {
			statement = (JSONObject) parser.parse(xApiStatement);
		} catch (ParseException e) {
			throw new EthereumException(e);
		}
		String email = ((JSONObject) ((JSONObject) statement.get("actor")).get("account")).getAsString("name");
		String timestamp = statement.getAsString("timestamp");
		String verb = ((JSONObject) ((JSONObject) statement.get("verb")).get("display")).getAsString("en-US");
		String object = ((JSONObject) ((JSONObject) ((JSONObject) statement.get("object")).get("definition")).get("name")).getAsString("en-US");

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(timestamp);
		stringBuilder.append(email);
		stringBuilder.append(verb);
		stringBuilder.append(object);

		// Create hash to store on chain
		byte[] hash = Util.soliditySha3(stringBuilder.toString());
		byte[] emailHash = Util.soliditySha3(email); 

		try {
			verificationRegistry.createLogEntry(emailHash, hash).sendAsync().get();
		} catch (Exception e) {
			throw new EthereumException(e);
		}

		// TODO Change this for production use.
		// Store token for lookup.
		String token = xApiStatement.split("\\*")[1];
		userToMoodleToken.put(email, token);
	}

	@SuppressWarnings("unchecked")
	public String getDataHashes(String userName) throws EthereumException {
		List<byte[]> result;
		try {
			result = (List<byte[]>) verificationRegistry.getDataHashesForUser(Util.padAndConvertString(userName, 32)).send();
			result.stream().forEach(s -> logger.warning("Hash: " + s.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new EthereumException(e);
		}
		return result.toString();
	}

	// --------------------------- Verification ----------------------------

	/**
	 * Queries all xAPIstatements from the LRS via the proxy service and filters relevant statements based on the given userEmail.
	 * Statements are verified with the logs on the Ethereum blockchain, formatted and returned.
	 * 
	 * @param User (represented by email) to revoke consent for.
	 * @return formatted xAPIstatements from LRS for the given user
	 */
	private String getStatementsForUserEmail(String userEmail) {
		String token = userToMoodleToken.get(userEmail);
		String statementsRaw = "";
		try {
			statementsRaw = (String) Context.get().invoke("i5.las2peer.services.learningLockerService.LearningLockerService@1.0.0", "getStatementsFromLRS", token);
		} catch (Exception e) {
			return userMessages.getString("errNoDataFound");
		}

		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		StringBuilder resBuilder = new StringBuilder();

		try {
			JSONObject obj = (JSONObject) parser.parse(statementsRaw);
			JSONArray statements = (JSONArray) obj.get("statements");
			
			int verifiedStatements = 0;

			for (int i = 0; i < statements.size(); i++) {
				JSONObject statement = (JSONObject) statements.get(i);
				String email = ((JSONObject) ((JSONObject) statement.get("actor")).get("account")).getAsString("name");

				if (email.equals(userEmail)) {
					String timestamp = statement.getAsString("timestamp");
					String action = ((JSONObject) ((JSONObject) statement.get("verb")).get("display")).getAsString("en-US");
					String object = ((JSONObject) ((JSONObject) ((JSONObject) statement.get("object")).get("definition")).get("name")).getAsString("en-US");
					String toHash = timestamp + email + action + object;
					boolean isVerified = verifyXApiStatement(toHash);
					
					ZonedDateTime time = ZonedDateTime.parse(timestamp).withZoneSameInstant(ZoneId.of("Europe/Berlin"));
					
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(time));
					stringBuilder.append("   ");
					stringBuilder.append(action);
					stringBuilder.append(": ");
					stringBuilder.append(object);
					stringBuilder.append("   ");
					if (isVerified) {
						verifiedStatements++;
						stringBuilder.append(userMessages.getString("dataVerified"));			
					} else {
						stringBuilder.append(userMessages.getString("dataUnverified"));
					}
					resBuilder.append(stringBuilder.toString());
				}
			}
			resBuilder.append("\n");
			resBuilder.append(userMessages.getString("dataCount"));
			resBuilder.append(statements.size());
			resBuilder.append("\n");
			resBuilder.append(userMessages.getString("dataCountVerified"));
			resBuilder.append(verifiedStatements);
			resBuilder.append("\n");
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (EthereumException e) {
			e.printStackTrace();
		}
		
		return resBuilder.toString();
	}

	/**
	 * Checks for a given content string if a corresponding hash/logentry exists on the Ethereum blockchain.
	 * 
	 * @param content to verify with the Ethereum blockchain.
	 * @return true if hash exists, false otherwise
	 * @throws EthereumException
	 */
	private boolean verifyXApiStatement(String toHash) throws EthereumException {		
		boolean result = false;
		if (toHash.isEmpty()) {
			return result;
		}
		
		byte[] hash = Util.soliditySha3(toHash);

		try {
			result = verificationRegistry.hasHashBeenRecorded(hash).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("An argument was not formatted correctly.", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
		return result;
	}

	// --------------------------- Utility  ----------------------------

	private String getConsentLevelsFormatted() {
		StringBuilder stringBuilder = new StringBuilder();
		Set<Integer> consentLevels = consentLevelMap.keySet();

		for (Integer i : consentLevels) {
			ConsentLevel cl = consentLevelMap.get(i);
			stringBuilder.append(cl.toString());
			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}
}
