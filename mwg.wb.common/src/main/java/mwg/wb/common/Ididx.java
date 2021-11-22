package mwg.wb.common;

public interface Ididx {
	void InitObject(ObjectTransfer objectTransfer);
//	void InitObject(Object afactoryWrite, Object afactoryRead, Object apriceHelper, Object aproductHelper, Object agson,
//			Object aerpHelper);

	ResultMessage Refresh(MessageQueue message);

	ResultMessage RunScheduleTask();

}
