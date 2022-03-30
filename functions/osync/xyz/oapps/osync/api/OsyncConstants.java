
package xyz.oapps.osync.api;

public class OsyncConstants {
	private static final int DIRECTION_LEFT = 1;
	private static final int DIRECTION_RIGHT = 2;
	private static final int DIRECTION_BOTH = 3;

	public class IntegrationStatus {
		public static final String RUNNING = "RUNNING";
		public static final String COMPLETE = "COMPLETE";
		public static final String ERROR = "ERROR";
		public static final String RESTART = "RESTART";
		public static final String NOT_STARTED = "NOT_STARTED";
		public static final String FORCEPAUSE = "FORCE_PAUSE";
		public static final String AUTHENTICATION_FAILED= "AUTH_FAILED";
	}

	public enum IntegrationDirection {
		DIRECTION_LEFT, DIRECTION_RIGHT, DIRECTION_BOTH
	}

	public enum IntegrationSyncStatus {
		START, PAUSE, STOP
	}

	public class AccessLevel {
		public static final int PUBLIC = 1;
		public static final int USER = 5;
		public static final int ADMIN = 20;
		public static final int CRON = 50;
		public static final int OSYNC_ADMIN = 100;

	}
}
