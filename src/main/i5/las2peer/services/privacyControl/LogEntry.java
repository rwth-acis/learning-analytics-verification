package i5.las2peer.services.privacyControl;

import java.time.LocalDateTime;

public class LogEntry {

	private LocalDateTime timestamp;
	private String source;
	private String operation;
	private String dataHash;
	
	public LogEntry(LocalDateTime timestamp, String source, String operation, String dataHash) {
		this.timestamp = timestamp;
		this.source = source;
		this.operation = operation;
		this.dataHash = dataHash;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getDataHash() {
		return dataHash;
	}

	public void setDataHash(String dataHash) {
		this.dataHash = dataHash;
	}
	
	@Override
	public String toString() {
		// TODO Implement
		return "";
	}
	
}
