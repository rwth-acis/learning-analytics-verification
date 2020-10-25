package i5.las2peer.services.privacyControl.TransactionLogging;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry {
	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");
	
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
	
	public String getTimestampFormatted() {
		return timestamp.format(formatter);
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
		return getTimestampFormatted() + ": Source: " + getSource() + ", Type: " + getOperation() + "\n" + "Hash: " + getDataHash() + "\n";
	}
	
}
