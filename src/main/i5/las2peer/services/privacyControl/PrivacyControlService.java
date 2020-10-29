package i5.las2peer.services.privacyControl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.w3c.dom.Element;
import org.web3j.tuples.generated.Tuple4;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.security.AgentException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AgentOperationFailedException;
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
import i5.las2peer.serialization.XmlTools;
import i5.las2peer.services.privacyControl.Consent.ConsentLevel;
import i5.las2peer.services.privacyControl.Consent.ConsentRegistry;
import i5.las2peer.services.privacyControl.TransactionLogging.LogEntry;
import i5.las2peer.services.privacyControl.TransactionLogging.TransactionLogRegistry;
import i5.las2peer.tools.CryptoException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
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
				description = "Service for consent management and verification of learning analytics data.",
				contact = @Contact(
						name = "Lennart Bengtson",
						url = "rwth-aachen.de",
						email = "lennart.bengtson@rwth-aachen.de")))
@ServicePath("/privacy")
@ManualDeployment
public class PrivacyControlService extends RESTService {

	private final static L2pLogger logger = L2pLogger.getInstance(PrivacyControlService.class.getName());

	private final static String DEFAULT_CONFIG_FILE = "etc/consentConfiguration.xml";

	private String lrsDomain;
	private String lrsAuth;
	private static ConcurrentMap<String, String> userToMoodleToken = new ConcurrentHashMap<>();

	private static EthereumNode node;
	private static ReadWriteRegistryClient registryClient;

	private static TransactionLogRegistry transactionLogRegistry;
	private static String transactionLogRegistryAddress;

	private static ConsentRegistry consentRegistry;
	private static String consentRegistryAddress;

	private static HashMap<Integer, ConsentLevel> consentLevelMap = new HashMap<Integer, ConsentLevel>();
	private static HashSet<String> consentProcessing = new HashSet<String>();
	private static HashSet<String> choosingFunction = new HashSet<String>();

	private static ConcurrentMap<String, List<BigInteger>> consentCache = new ConcurrentHashMap<>();

	private static boolean initialized = false;

	// ------------------------------ Initialization -----------------------------

	public PrivacyControlService() {
		setFieldValues();
	}

	@POST
	@Path("/init")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Privacy control service initialized") })
	public Response init() {
		if (initialized) {
			return Response.status(Status.BAD_REQUEST).entity("Already initialized").build();
		}
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
			return Response.status(Status.BAD_REQUEST).entity("Unable to read consent configuration").build();
		}
		logger.info("Successfully read from XML consent configuration file.");

		// Get smart contract addresses from configuration file.
		LaRegistryConfiguration config = new LaRegistryConfiguration();
		consentRegistryAddress = config.getConsentRegistryAddress();
		transactionLogRegistryAddress = config.getTransactionLogRegistryAddress();

		logger.info("Sucessfully loaded migrated smart contracts...");
		logger.info("TransactionLogRegistry deployed at: " + transactionLogRegistryAddress);
		logger.info("ConsentRegistry deployed at: " + consentRegistryAddress);

		// Deploy smart contracts from wrapper classes
		try {
			ServiceAgentImpl agent = (ServiceAgentImpl) Context.getCurrent().getServiceAgent();
			node = (EthereumNode) agent.getRunningAtNode();
			registryClient = node.getRegistryClient();

			consentRegistry = deployConsentRegistry();
			transactionLogRegistry = deployTransactionLogRegistry();
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
	@Path("/showGreeting")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Returned menu.") })
	public Response showGreeting(String body) throws ParseException {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		String channel = "";

		try {
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			channel = bodyObj.getAsString("channel");
			String email = bodyObj.getAsString("email");

			UserAgentImpl agent = getAgentFromUserEmail(email);
			if (agent == null) {
				JSONObject err = new JSONObject();
				err.put("text", "Zu Deiner Email ist kein las2peer User registriert. Bitte registriere Dich um diesen Service zu nutzen.");
				err.put("closeContext", "true");
				return Response.ok().entity(err).build();
			}

			if (consentProcessing.contains(channel)) {
				// Proceed with storage
				logger.info("Continuing consent storage for user " + agent.getLoginName());

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
					storeUserConsentLevels(agent.getLoginName(), levels);
					consentProcessing.remove(channel);

					// Build response and close context.
					JSONObject res = new JSONObject();
					res.put("text", "Deine Einwilligung wurde erfolgreich gespeichert. \n Melde Dich, falls ich noch etwas fuer Dich tun kann.");
					res.put("closeContext", "true");
					return Response.ok().entity(res).build();

				} catch (NumberFormatException e) {
					// Build error but don't close context when ID is not valid.
					JSONObject err = new JSONObject();
					err.put("text", "Bitte gib eine oder mehrere gueltige Nummern ein.");
					err.put("closeContext", "false");
					return Response.ok().entity(err).build();
				} catch (EthereumException e) {
					e.printStackTrace();
				}
				consentProcessing.remove(channel);
			}

			if (choosingFunction.contains(channel)) {
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
						res.put("text", "Melde Dich, falls ich noch etwas fuer Dich tun kann.");
						res.put("closeContext", "true");
						return Response.ok().entity(res).build();

					case 1: // consentLevels();
						logger.info("Showing consent options...");
						String consentLevelString = getConsentLevelsFormatted();

						res = new JSONObject();
						res.put("text", "" + consentLevelString);
						res.put("closeContext", "true");
						return Response.ok().entity(res).build();

					case 2: // storeConsent(body);
						logger.info("Starting consent storage for user " + agent.getLoginName());
						consentProcessing.add(channel);
						String consentLevels = getConsentLevelsFormatted();

						StringBuilder storageStringBuilder = new StringBuilder();
						storageStringBuilder.append("Hier kannst Du festlegen, welche Deiner Daten aus welchem System gesammelt und analysiert werden duerfen. \n");
						storageStringBuilder.append("'Services' bezeichnet die Systeme, in denen die Daten erfasst werden (z.B. Moodle). \n");
						storageStringBuilder.append("'Aktionen' bestimmt die Art der Daten (z.B. 'completed' fuer ein abgeschlossenes Quiz oder 'posted' fuer einen verfassten Forumseintrag). \n");
						storageStringBuilder.append("Dazu hast Du folgende Optionen: \n");
						storageStringBuilder.append(consentLevels);
						storageStringBuilder.append("Bitte gib die Nummern der Optionen an, mit denen Du einverstanden bist (bei mehreren: '1,2')");
						
						res = new JSONObject();
						res.put("text", storageStringBuilder.toString());
						res.put("closeContext", "false");
						return Response.ok().entity(res).build();

					case 3: // showConsent(body);
						List<BigInteger> givenConsent;
						try {
							givenConsent = getConsentLevelsForLoginName(agent.getLoginName());
							StringBuilder stringBuilder = new StringBuilder();
							if (givenConsent == null || givenConsent.isEmpty()) {
								stringBuilder.append("Aktuell liegt keine Einwilligung von Dir vor.");
							} else {
								stringBuilder.append("Du hast Deine Einwilligungen zu folgenden Optionen gegeben: \n");
								for (BigInteger consent : givenConsent) {
									stringBuilder.append(consentLevelMap.get(consent.intValue()).toString());
									stringBuilder.append("\n");
								}
							}
							
							stringBuilder.append("\n Melde Dich, falls ich noch etwas fuer Dich tun kann.");

							res = new JSONObject();
							res.put("text", stringBuilder.toString());
							res.put("closeContext", "true");
							return Response.ok().entity(res).build();
						} catch (EthereumException e) {
							e.printStackTrace();
						}
						break;

					case 4: // revokeConsent(body);
						logger.info("Revoking consent for user " + agent.getLoginName());
						try {
							revokeUserConsent(agent.getLoginName());

							res = new JSONObject();
							res.put("text", "Deine Einstellungen wurden angepasst. \n Melde Dich, falls ich noch etwas fuer Dich tun kann.");
							res.put("closeContext", "true");
							return Response.ok().entity(res).build();
						} catch (EthereumException e) {
							e.printStackTrace();
						}
						break;

					case 5: // verifyData(body)
						logger.info("LRS Data requested for user " + agent.getLoginName());
						String resText = getStatementsForUserEmail(email);

						res = new JSONObject();
						res.put("text", resText + "\n Melde Dich, falls ich noch etwas fuer Dich tun kann.");
						res.put("closeContext", "true");
						return Response.ok().entity(res).build();

					default:
						break;
					}

				} catch (NumberFormatException e) {
					JSONObject err = new JSONObject();
					err.put("text", "Bitte gib eine der Ziffern ein, um eine Funktion zu nutzen oder '0' zum Abbrechen.");
					err.put("closeContext", "false");
					return Response.ok().entity(err).build();
				}
			} else {
				choosingFunction.add(channel);
				String greeting = 
						"Hallo, ich kann folgendes fuer dich tun: \n" 
								+ "[1] Informationen zu moeglichen Datenverarbeitungseinwilligungen anzeigen. \n"
								+ "[2] Einwilligung zur Datenverarbeitung abgeben. \n"
								+ "[3] Einwilligung zur Datenverarbeitung anzeigen. \n"
								+ "[4] Einwilligung zur Datenverarbeitung widerrufen. \n"
								+ "[5] Gespeicherte Daten aus Learning Record Store anzeigen. \n"
								+ "[0] Abbrechen. \n"
								+ "Um eine Funktion zu nutzen, gib die entsprechende Nummer ein.";

				JSONObject res = new JSONObject();
				res.put("text", greeting);
				res.put("closeContext", "false");
				return Response.ok().entity(res).build();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		choosingFunction.remove(channel);

		JSONObject res = new JSONObject();
		res.put("text", "Etwas ist bei deiner Anfrage schiefgegangen.");
		res.put("closeContext", "true");
		return Response.ok().entity(res).build();
	}

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
					message = "Consent level set.") })
	public Response storeConsent(String body) {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		String channel = "";

		try {
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			channel = bodyObj.getAsString("channel");

			// Get corresponding agent
			UserAgentImpl agent = getAgentFromUserEmail(bodyObj.getAsString("email"));
			if (agent == null) {
				JSONObject err = new JSONObject();
				err.put("text", "Zu deiner Email ist kein las2peer User registriert. Bitte registriere Dich um diesen Service zu nutzen.");
				err.put("closeContext", "true");
				return Response.ok().entity(err).build();
			}

			// Check if consent storage was already started.
			if (consentProcessing.contains(channel)) {
				logger.info("Continue consent storage for user " + agent.getLoginName());

				// Get consent level from message
				String chosenConsentLevels = bodyObj.getAsString("msg").split("\\.")[0];

				try {
					List<BigInteger> levels = new ArrayList<BigInteger>();
					for (String s : chosenConsentLevels.split("\\,")) {
						// TODO Validate if consent object with that level exists!
						levels.add(new BigInteger(s));
					}
					storeUserConsentLevels(agent.getLoginName(), levels);

				} catch (NumberFormatException e) {
					// Build error but don't close context when ID is not valid.
					JSONObject err = new JSONObject();
					err.put("text", "Bitte gib eine oder mehrere gueltige Ids ein.");
					err.put("closeContext", "false");
					return Response.ok().entity(err).build();
				}

				consentProcessing.remove(channel);

				// Build response and close context.
				JSONObject res = new JSONObject();
				res.put("text", "Einwilligung erfolgreich gespeichert.");
				res.put("closeContext", "true");
				return Response.ok().entity(res).build();
			} else {
				logger.info("Starting consent storage for user " + agent.getLoginName());
				consentProcessing.add(channel);
				String consentLevelString = getConsentLevelsFormatted();

				// Build response and close context.
				JSONObject res = new JSONObject();
				res.put("text", "Bitte gib die Ids (z.B. 1,2) der Datenverarbeitungsoptionen an, mit denen Du einverstanden bist. \n" + consentLevelString);
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

		if (!channel.isEmpty()) {
			consentProcessing.remove(channel);
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
				err.put("text", "Zu deiner Email ist kein las2peer User registriert. Bitte registriere Dich um diesen Service zu nutzen.");
				err.put("closeContext", "true");
				return Response.ok().entity(err).build();
			}

			// Retrieve stored consent levels
			List<BigInteger> givenConsent = getConsentLevelsForLoginName(agent.getLoginName());

			String resText = "";
			if (givenConsent == null || givenConsent.isEmpty()) {
				resText +=  "Aktuell liegt uns keine Einwilligung zu Deinem User vor.";
			} else {
				resText += "Es liegt eine Einwilligung zu folgenden Services/Zugriffsarten vor: \n";
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
				errorMsg.put("text", "Zu deiner Email ist kein las2peer User registriert. Bitte registriere Dich um diesen Service zu nutzen.");
				errorMsg.put("closeContext", "true");
				return Response.ok().entity(errorMsg).build();
			}

			revokeUserConsent(agent.getLoginName());

			JSONObject responseBody = new JSONObject();
			responseBody.put("text", "Deine Einstellungen wurden angepasst.");
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
		errorMsg.put("text", "Etwas ist bei der Anfrage schiefgegangen.");
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
				errorMsg.put("text", "Zu deiner Email ist kein las2peer User registriert. Bitte registriere Dich um diesen Service zu nutzen.");
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

	@POST
	@Path("/verifyData")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Verifed xApi statements.") })
	public Response verifyData(String body) {
		logger.info("Data requested...");
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		String resText = "";

		try {
			JSONObject bodyObj = (JSONObject) parser.parse(body);
			resText += getStatementsForUserEmail(bodyObj.getAsString("email"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		JSONObject res = new JSONObject();
		res.put("text", resText);
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
	public boolean checkUserConsent(String userEmail, String action) throws EthereumException {
		UserAgentImpl agent = getAgentFromUserEmail(userEmail);
		if (agent != null) {
			// Get calling service from execution context
			ServiceAgentImpl callingAgent = (ServiceAgentImpl) ExecutionContext.getCurrent().getCallerContext().getMainAgent();
			String callingAgentName = callingAgent.getServiceNameVersion().getSimpleClassName().toLowerCase();

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
			// TODO Create log entries for failed extraction attempts?
		}
		return false;
	}

	/**
	 * Stores given consent level(s) for a user.
	 * 
	 * @param User (represented by login name) to store consent for.
	 * @param BigInteger necessary level of consent (as defined in config file)
	 * @throws EthereumException
	 */
	public void storeUserConsentLevels(String userName, List<BigInteger> consentLevels) throws EthereumException {
		try {
			consentRegistry.storeConsent(Util.padAndConvertString(userName, 32), consentLevels).sendAsync().get();
			if (consentCache.containsKey(userName)) {				
				logger.info("Resetting consent cache.");
				consentCache.remove(userName);
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
	public void revokeUserConsent(String userName) throws EthereumException {
		try {
			consentRegistry.revokeConsent(Util.padAndConvertString(userName, 32)).sendAsync().get();
			if (consentCache.containsKey(userName)) {				
				logger.info("Resetting consent cache.");
				consentCache.remove(userName);
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
	public List<BigInteger> getConsentLevelsForLoginName(String userName) throws EthereumException {
		if (consentCache.containsKey(userName)) {
			logger.info("Loading consent from cache.");
			return consentCache.get(userName);
		}

		// Try to get information from the blockchain and return.
		List<BigInteger> consentLevels;
		try {
			consentLevels = consentRegistry.getUserConsentLevels(Util.padAndConvertString(userName, 32)).sendAsync().get();
			consentCache.put(userName, consentLevels);
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

		UserAgentImpl agent = getAgentFromUserEmail(email);
		if (agent != null) {
			ServiceAgentImpl callingAgent = (ServiceAgentImpl) ExecutionContext.getCurrent().getCallerContext().getMainAgent();
			String callingAgentName = callingAgent.getServiceNameVersion().getSimpleClassName().toLowerCase();

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

			try {
				transactionLogRegistry.createLogEntry(Util.padAndConvertString(agent.getLoginName(), 32), Util.padAndConvertString(callingAgentName, 32), Util.padAndConvertString(verb, 32), hash).sendAsync().get();
			} catch (Exception e) {
				throw new EthereumException(e);
			}
		}

		// TODO Change this for production use.
		// Store token for lookup.
		String token = xApiStatement.split("\\*")[1];
		userToMoodleToken.put(email, token);
	}

	@SuppressWarnings("deprecation")
	public String getLogEntries(String userName) throws EthereumException {
		Tuple4<List<BigInteger>, List<byte[]>, List<byte[]>, List<byte[]>> initialResult;
		StringBuilder resBuilder = new StringBuilder();
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

			if (logs.isEmpty()) {
				resBuilder.append("Derzeit liegen keine geloggten Datenzugriffe zu deinem Account vor.");
			} else {
				resBuilder.append("Es wurden folgende Datenzugriffe geloggt: \n\n");
				for (LogEntry l : logs) {
					resBuilder.append(l.toString());
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new EthereumException(e);
		}
		return resBuilder.toString();
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

	// --------------------------- Verification ----------------------------

	/**
	 * Queries all xAPIstatements from the LRS and filters relevant statements based on the given userEmail.
	 * Statements are verified with the logs on the Ethereum blockchain, formatted and returned.
	 * 
	 * @param User (represented by email) to revoke consent for.
	 * @return formatted xAPIstatements from LRS for the given user
	 */
	private String getStatementsForUserEmail(String userEmail) {
		String token = userToMoodleToken.get(userEmail);
		String statementsRaw = getStatementsFromLRS(token);

		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		StringBuilder resBuilder = new StringBuilder();
		resBuilder.append("Folgende Learning Analytics Daten liegen zu Dir vor: \n \n");

		try {
			JSONObject obj = (JSONObject) parser.parse(statementsRaw);
			JSONArray statements = (JSONArray) obj.get("statements");

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
					stringBuilder.append("\n");
					stringBuilder.append(action);
					stringBuilder.append(": ");
					stringBuilder.append(object);
					stringBuilder.append("\n");
					if (isVerified) {
						stringBuilder.append("Blockchain-verifizierter Datensatz \n");			
					} else {
						stringBuilder.append("Datensatz nicht verifiziert");
					}

					resBuilder.append(stringBuilder.toString());
					resBuilder.append("\n");
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (EthereumException e) {
			e.printStackTrace();
		}
		return resBuilder.toString();
	}

	/**
	 * Gets all xAPI statements for the given clientID (moodleToken).
	 * 
	 * @param Token to identify the relevant client
	 * @return xAPIstatements in JSON String
	 */
	private String getStatementsFromLRS(String token)  {
		logger.info("Starting extraction of LRS data...");
		StringBuffer response = new StringBuffer();
		String clientKey;
		String clientSecret;
		Object clientId = null;
		try {
			clientId = searchIfClientExists(token);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if(!(clientId).equals("noClientExists")) {
			clientKey = (String) ((JSONObject) clientId).get("basic_key");
			clientSecret = (String) ((JSONObject) clientId).get("basic_secret");
			String auth = Base64.getEncoder().encodeToString((clientKey + ":" + clientSecret).getBytes());

			try {
				URL url = new URL(lrsDomain + "/data/xAPI/statements");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("X-Experience-API-Version","1.0.3");
				conn.setRequestProperty("Authorization", "Basic " + auth);
				conn.setRequestProperty("Cache-Control", "no-cache");

				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				conn.disconnect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return response.toString();
		} else {
			return "";
		}
	}

	/**
	 * Gets the corresponding LRS-client-object for a given ClientID (in this case a moodleToken).
	 * 
	 * @param Token to identify the relevant client
	 * @return LRS-Client
	 * @throws IOException
	 * @throws ParseException
	 */
	private Object searchIfClientExists(String token) throws IOException, ParseException {
		URL url = null;
		try {
			try {
				String clientURL = lrsDomain + "/api/v2/client";
				url = new URL(clientURL);
				HttpURLConnection conn = null;
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("X-Experience-API-Version","1.0.3");
				conn.setRequestProperty("Authorization", "Basic " + lrsAuth);
				conn.setRequestProperty("Cache-Control", "no-cache");
				conn.setUseCaches(false);

				InputStream is = conn.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				StringBuilder response = new StringBuilder();
				while((line = rd.readLine()) != null) {
					response.append(line);
				}
				Object obj= JSONValue.parse(response.toString());

				for(int i = 0 ; i < ((JSONArray ) obj).size(); i ++) {
					JSONObject client = (JSONObject) ((JSONArray) obj).get(i);
					if(client.get("title").equals(token)) {
						return client.get("api");
					}
				}
			}  catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			System.out.println(e);
			return "Error";
		}
		return "noClientExists";
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
			result = transactionLogRegistry.hasHashBeenRecorded(hash).sendAsync().get();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("An argument was not formatted correctly.", e);
		} catch (Exception e) {
			throw new EthereumException(e);
		}
		return result;
	}

	// --------------------------- Utility  ----------------------------

	private UserAgentImpl getAgentFromUserEmail(String userEmail) {
		if (userEmail == null || userEmail.isEmpty()) {
			return null;
		}

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
