import Tracker.EventTracker;

public class start {

	public static long StartTime;

	start() {
		EventTracker.Write("Starting...", 0);
		new windows();
	}

	public static void main(String args[]) {
		StartTime = System.currentTimeMillis();
		EventTracker.init("logs/Log.txt");
		new start();
	}

	public static long getRunTime() {
		return System.currentTimeMillis() - StartTime;
	}

}
