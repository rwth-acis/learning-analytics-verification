package i5.las2peer.services.privacyControl;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

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
	
	/**
	 * Function that is invoked by a LMS proxy to check for the consent of a given user.
	 * TODO: Implement logic. Currently only for testing message sending and access restriction... 
	 * TODO: Include additional parameters.
	 * 
	 * @param User to check consent for.
	 * @returns boolean True/false based on user consent.
	 */
	public boolean checkUserConsent(String email) {
		logger.warning("Service requesting consent information for user: " + email);
		boolean consentGiven = true;
		if (email.equalsIgnoreCase("alice@example.org")) {
			logger.warning("Consent not given. Permission denied.");
			// getUserConsentFromBlockchain(input);
			consentGiven = false;
		}
		logger.warning("Consent given. Permission granted.");
		return consentGiven;
	}
	
	// TODO:
	private void logDataAccessOperation(Object someInput) {
		
	}
	
	// TODO:
	private void getUserConsentFromBlockchain(String userId) {
		
	}
	
}
