package i5.las2peer.services.privacyControl;

public enum ConsentLevelEnum {
	NONE(0),
	EXTRACTION(1),
	ANALYSIS(2),
	ALL(3);
	
	private final int level;

	ConsentLevelEnum(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return this.level;
	}
}
