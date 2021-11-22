package mwg.wb.business;

import mwg.wb.common.DidxHelper;
import mwg.wb.common.Logs;
import mwg.wb.model.LogLevel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(LogHelper.class);
	private static JSONParser jsonParser = new JSONParser();

//	protected ObjectMapper mapper = null;
//	mapper = new ObjectMapper();
//	DateFormat df = new SimpleDateFormat(GConfig.DateFormatStringNews);
//	mapper.setDateFormat(df);
//	mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
//	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	public static void WriteLog(Throwable e) {
		WriteLog(e, LogLevel.ERROR);
		if (DidxHelper.isLocal()) {
			e.printStackTrace();
		}
	}

	public static void WriteLog(Throwable e, LogLevel logLevel) {

		String logMessage = GetFullExceptionMessage(e);
		WriteLog(logMessage, logLevel);

	}

	public static void WriteLog(String logMessage, String h1) {

		WriteLog(logMessage, LogLevel.ERROR, h1);

	}

	public static void WriteLog(Throwable e, LogLevel logLevel, HttpServletRequest request) {

		try {
			if (request != null) {
				String logMessage = GetFullExceptionMessage(e);
				if (request.getMethod().equals("GET")) {
					String urlParams = request.getRequestURI() + "/" + request.getQueryString();

					WriteLog(logMessage, logLevel, urlParams);
				} else if (request.getMethod().equals("POST")) {

					String requestData = null;

					JSONObject requestJson = null;

					try {

//						if (request.getContentLength() > 0) {
//							requestData = CharStreams.toString(request.getReader());
//						}
//
//						if (requestData != null) {
//							requestJson = (JSONObject) jsonParser.parse(requestData);
//						}
//						if (requestJson != null) {
//							String urlParams = request.getRequestURL().toString() + ", params: "
//									+ requestJson.toJSONString();
//							WriteLog(logMessage, logLevel, urlParams);
//
//						}
//						String payloadRequest = getBody(request);
//						String urlParams = request.getRequestURL().toString() + ", body: " + payloadRequest;
//
//						
//
//						WriteLog(logMessage, logLevel, urlParams);
						WriteLog(e, logLevel);

					} catch (Throwable exx) {

						exx.printStackTrace();
					}

				}

			} else {
				WriteLog(e, logLevel);
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
			WriteLog(e, logLevel);
		}

	}

	public static void WriteLog(Throwable e, LogLevel logLevel, String params) {

		String logMessage = GetFullExceptionMessage(e);

		WriteLog(logMessage, logLevel, params);

	}

	public static String getBody(HttpServletRequest request) throws Throwable {

		// read the original payload into the payload variable
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			// read the payload into the StringBuilder
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				// make an empty string since there is no payload
				stringBuilder.append("");
			}
		} catch (Throwable ex) {
			throw ex;

		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Throwable iox) {
					// ignore
				}
			}
		}
		return stringBuilder.toString();
	}

	public static void WriteLog(String logMessage, LogLevel logLevel, String params) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();

			String buildLog = String.format("Module %s - Params %s - Message %s - HostName %s", "JAVA_API", params,
					logMessage, inetAddress.getHostName());

			if (logLevel == LogLevel.ERROR) {
				Logs.LogException(params+"\n\r"+logMessage);
				LOGGER.error("Logtype: ERROR - " + buildLog);
			} else if (logLevel == LogLevel.INFO) {
				LOGGER.info("Logtype: INFO - " + buildLog);
			} else if (logLevel == LogLevel.WARNING) {
				Logs.LogException(logMessage + "\n\r" + params);
				LOGGER.warn("Logtype: WARNING - " + buildLog);
			} else if (logLevel == LogLevel.DEBUG) {
				LOGGER.debug("Logtype: DEBUG - " + buildLog);
			} else if(logLevel == LogLevel.LOGTRACE){

				var traceMessage = logMessage + "\n\r Body: " + params + "\n-----------------------------------------\n";
				Logs.LogTrace(traceMessage);
			}
		} catch (Throwable ex) {
			// TODO Auto-generated catch block
			//ex.printStackTrace();
		}

	}

	public static void WriteLog(String logMessage, LogLevel logLevel) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();

			String buildLog = String.format("Module %s - Message %s - HostName %s", "JAVA_API", logMessage,
					inetAddress.getHostName());

			if (logLevel == LogLevel.ERROR) {
				Logs.LogException( logMessage);
				LOGGER.error("Logtype: ERROR - " + buildLog);
			} else if (logLevel == LogLevel.INFO) {
				LOGGER.info("Logtype: INFO - " + buildLog);
			} else if (logLevel == LogLevel.WARNING) {
				LOGGER.warn("Logtype: WARNING - " + buildLog);
			} else if (logLevel == LogLevel.DEBUG) {
				LOGGER.debug("Logtype: DEBUG - " + buildLog);
			}
		} catch (Throwable ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

	}

	public static String GetFullExceptionMessage(Throwable e) {
		try {
			var trace = Stream.of(e.getStackTrace()).map(m -> m.toString()).collect(Collectors.joining(", "));
			return e.toString() + ": " + e.getMessage() + " - " + trace;
		} catch (Throwable e2) {

			e2.printStackTrace();
			return "";
		}

	}

	public static void WriteLog(String string, LogLevel logLevel, HttpServletRequest request) {

		if (request != null && request.getMethod().equals("GET")) {
			String urlParams = request.getRequestURI() + "/" + request.getQueryString();

			WriteLog(string, logLevel, urlParams);
		} else {
			WriteLog(string, logLevel);
		}
	}

	public static void WriteLogTimer(String message, LogLevel logLevel, HttpServletRequest request) {
		if (request != null && request.getMethod().equals("GET")) {
			String urlParams = request.getRequestURI() + "/" + request.getQueryString();

			WriteLog(" TIMER_LOG " + message, logLevel, urlParams);
		} else {
			WriteLog(" TIMER_LOG " + message, logLevel);
		}

	}
}
