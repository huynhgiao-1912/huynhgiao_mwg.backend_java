package mwg.wb.common;

public class ResultMessage {
	public enum ResultCode {
		Success, Error, Cancel, NotFound, InvalidRequest, Retry,ReConnect
	}

	public String Message;

	public ResultCode Code;

	public String StackTrace;

}
