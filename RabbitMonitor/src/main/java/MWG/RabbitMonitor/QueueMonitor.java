package MWG.RabbitMonitor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class QueueMonitor {
	public long ID;
	public String QueueName;
	public LocalDateTime IdleTime;
	public boolean IsIdle;
	public LocalDateTime TimeCheck;
	public LocalDateTime PreviousMonitor;
	public long NumberMessage;
	public int Type;// 1: warning too much message,2: warning delivery get=0,3: idle
	public double Delivery_Get;

	public String CreateMessage() {
		if (Type == 1) {
			return "[WARNI] " + QueueName + ", totalmessage: " + NumberMessage + ", deliver_get: " + Delivery_Get + ".";

		} else if (Type == 2) {
			return "[WARNI] " + QueueName + ", totalmessage: " + NumberMessage + ", deliver_get: " + Delivery_Get + ".";

		} else if (Type == 3) {
			long minute = GetMinuteIdle();
			if (minute >= 3 && minute < 5000) {
				return "[DANGER] " + QueueName + ", status: IDLE, time:  " + minute + " minutes, totalmessage: "
						+ NumberMessage + ", idle at:  " + IdleTime + ".";
			}
			return "";

		} else
			return "";
	}

	public long GetMinuteIdle() {
		return ChronoUnit.MINUTES.between(IdleTime, TimeCheck);
	}

}
