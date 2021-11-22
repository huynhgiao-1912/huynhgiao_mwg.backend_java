package mwg.wb.client.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeTimers {
	private Map<String, CodeTimer> timers;
	private String loggingFile = null;
	int count = 0;

	public CodeTimers() {
		timers = new HashMap<>();
	}

	public CodeTimer getTimer(String name) {
		var timer = timers.get(name);
		// var timer = timers.get("timer-" + name);

		if (timer == null) {
			count++;
			timer = new CodeTimer(name);
			timer.setpost(count);
			timers.put(name, timer);
		}
		return timer;
	}

	public void setLogging(String fileName) {
		loggingFile = fileName;
	}

	public void start(String name) {
		getTimer(name).reset();

	}

	public void pause(String name) {
		var timer = getTimer(name);
		timer.pause();
//		if (loggingFile != null) {
//			Logs.getInstance().Log(true, "DIDX_LOG|" + loggingFile, timer.getName() + " elapsed time: "
//					+ timer.getElapsedTime() + "ms");
//		}
	}

	public String loggedTime() {
		long all = 0;
		String result = "";
		var lstCodeTimer = timers.values().stream()
				// .sorted(Comparator.comparing(CodeTimer::getName))
				.sorted(Comparator.comparing(CodeTimer::getPos)).collect(Collectors.toList());
		for (var x : lstCodeTimer) {
			if (x.getName().equals("all")) {
				all = x.getElapsedTime();
			}
			result += x.getName() + " took " + x.getElapsedTime() + "ms " + "  \r\n";
		}
		if (all > 1000 * 20) {

			return result;
		} else {
			return null;
		}
	}

	public String GetAllTime() {

		String result = "";
		try {
			var lstCodeTimer = timers.values().stream().sorted(Comparator.comparing(CodeTimer::getPos))
					.collect(Collectors.toList());
			for (var x : lstCodeTimer) {

				result += x.getName() + " : " + x.getElapsedTime() + "ms " + ", ";
			}
		}catch (Exception r){
			result = r.getMessage();
		}
		return result;
	}

	public CodeTimer[] getTimers() {
		return timers.values().toArray(CodeTimer[]::new);
	}
}
