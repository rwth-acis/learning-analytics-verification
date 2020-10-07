package i5.las2peer.services.privacyControl;

import i5.las2peer.api.Configurable;

public class LaRegistryConfiguration extends Configurable {
	
	private String transactionLogRegistryAddress;
	private String consentRegistryAddress;
	
	public LaRegistryConfiguration() {
		setFieldValues();
	}
	
	public String getConsentRegistryAddress() {
		return consentRegistryAddress;
	}
	
	public String getTransactionLogRegistryAddress() {
		return transactionLogRegistryAddress;
	}
}
