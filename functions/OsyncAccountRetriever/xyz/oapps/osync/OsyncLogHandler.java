package xyz.oapps.osync;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xyz.oapps.osync.entity.AuthorizationEntity;

public class OsyncLogHandler extends Handler {

	static ThreadLocal<String> currentRequestId = new ThreadLocal<String>();
	static ThreadLocal<String> currentIntegId = new ThreadLocal<String>();
	static ThreadLocal<JSONArray> logsJsonArray = new ThreadLocal<JSONArray>();
	static ThreadLocal<HttpServletRequest> current_request = new ThreadLocal<HttpServletRequest>();
	static ThreadLocal<HttpServletResponse> current_response = new ThreadLocal<HttpServletResponse>();
	
	int index = 0;

	public void publishRequest() {
		try {
			JSONObject json = new JSONObject();
			json.putOpt("timestamp", SyncHandler.toISO8601Format(System.currentTimeMillis()));
			json.putOpt("req_id", currentRequestId.get());
			json.putOpt("type", "access");
			if (current_request.get() != null) {
				HttpServletRequest request = current_request.get();
				JSONObject req = new JSONObject();
				req.putOpt("uri", request.getRequestURI());
				req.putOpt("remote", request.getRemoteAddr());
				req.putOpt("user-agent", request.getHeader("User-Agent"));
				req.putOpt("params", request.getParameterMap());
				Enumeration<String> headerNames = request.getHeaderNames();
				if (headerNames != null) {
					JSONObject headers = new JSONObject();
					while (headerNames.hasMoreElements()) {
						String name = headerNames.nextElement();
						headers.putOpt(name, request.getHeader(name));
					}
					req.putOpt("headers", headers);
				}
				req.putOpt("method", request.getMethod());
				json.put("request", req);
			}
			postLogs(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void publish(LogRecord record) {
		try {
			JSONObject json = new JSONObject();
			json.putOpt("timestamp", SyncHandler.toISO8601Format(record.getMillis()));
			json.putOpt("req_id", currentRequestId.get());
			json.putOpt("integ_id", currentIntegId.get());
			json.putOpt("type", "application");
			JSONObject log = new JSONObject();
			log.putOpt("seq_no", record.getSequenceNumber());
			log.putOpt("logger-name", record.getLoggerName());
			log.putOpt("source", record.getSourceClassName());
			log.putOpt("level", record.getLevel());
			log.putOpt("method", record.getSourceMethodName());
			json.putOpt("log", log);
			json.putOpt("index", ++index);

			try {
				json.putOpt("message", MessageFormat.format(record.getMessage(), record.getParameters()));
			} catch (Exception e) {
				json.putOpt("message", record.getMessage());
			}
			json.putOpt("thread-id", record.getThreadID());

			Throwable thrown = record.getThrown();
			if (thrown != null) {
				Writer result = new StringWriter();
				thrown.printStackTrace(new PrintWriter(result));
				json.putOpt("cause", result.toString());
			}

			if (CurrentContext.getCurrentContext() != null) {
				JSONObject user = new JSONObject();
				user.putOpt("osync_id", CurrentContext.getCurrentOsyncId());
				AuthorizationEntity token = CurrentContext.getCurrentContext().getToken();
				if (token != null) {
					user.putOpt("user_id", token.getOsyncUserId());
					user.putOpt("token_row_id", CurrentContext.getCurrentContext().getToken().getRowId());
				}
				user.putOpt("user-access-level", CurrentContext.getCurrentContext().getUserAccessLevel());
				json.putOpt("account", user);
			}
//			postLogs(json);
//			System.out.println("log-index-debug index:" + index);
//			System.out.println("log-index-debug level:" + record.getLevel());
//			System.out.println("log-index-debug level:" + record.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void postLogs(JSONObject json) throws Exception {

		JSONArray jsonArray = logsJsonArray.get();
		if (jsonArray == null) {
			jsonArray = new JSONArray();
			logsJsonArray.set(jsonArray);
		}
		jsonArray.put(json);
		if (jsonArray.length() > 100) {
			System.out.println("Writing to loggly..." + jsonArray.length());
			jsonArray = postToLoggly(jsonArray);
			logsJsonArray.set(jsonArray);
		}
	}

	private JSONArray postToLoggly(JSONArray jsonArray) {
		try {
			if (jsonArray == null) {
				return null;
			}
			long time = System.currentTimeMillis();
			StringBuilder body = new StringBuilder();
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					body.append(jsonArray.getJSONObject(i).toString()).append('\n');
				} catch (JSONException e) {
					System.out.println("error in record conversion: " + i);
				}
			}
			URL url = new URL("https://logs-01.loggly.com/bulk/8fbcb29d-6b36-4804-bd51-5c113578ebe5/tag/bulk/");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setDoOutput(true);
			con.setReadTimeout(3000);
			con.setConnectTimeout(1000);
			try (OutputStream os = con.getOutputStream()) {
				time = System.currentTimeMillis();
				byte[] input = body.toString().getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				br.read();
			}
			System.out.println("Time taken to post " + jsonArray.length() + " messages : "
					+ (System.currentTimeMillis() - time) + "ms");
			return new JSONArray();
		} catch (Exception e) {
			System.out.println("Error while pushing to loggly: " + e.getMessage());
			return jsonArray;
		}
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws Exception {
		OsyncLogHandler olh = new OsyncLogHandler();
		int i = 0;
		while (i < 1) {
			JSONObject json = new JSONObject();
			json.putOpt("req_id", "req_id_buffer_append_un_comment_" + i);
			json.putOpt("integ_id", "integ_id_buffer_append_comment");
			json.putOpt("log_seq_no", "log_seq_no");
			json.putOpt("date", new Date(System.currentTimeMillis()));
			json.putOpt("logger-name", "logger-name");
			try {
				throw new OsyncException(OsyncException.Code.API_ERROR);
			} catch (Exception e) {
				Writer result = new StringWriter();
				e.printStackTrace(new PrintWriter(result));
				json.putOpt("cause", result.toString());
			}
			json.putOpt("source", "source");
			json.putOpt("method", "method");
			long time = System.currentTimeMillis();
			olh.postLogs(json);
			System.out.println(json.toString());
//			System.out.println("Time taken : " + (System.currentTimeMillis() - time) + "ms");
			i++;
		}
		olh.clear();
	}

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub

	}

	public void setCurrentIntegId(String integId) {
		currentIntegId.set(integId);
	}

	public void setCurrentRequestId(String uuid) {
		currentRequestId.set(uuid);
	}

	public void clear() {
		try {
			currentRequestId.set(null);
		} catch (Exception e) {
		}
		try {
			currentIntegId.set(null);
		} catch (Exception e) {
		}
		try {
			postToLoggly(logsJsonArray.get());
		} catch (Exception e) {
		}
		try {
			logsJsonArray.set(null);
		} catch (Exception e) {
		}
		try {
			current_request.set(null);
		} catch (Exception e) {
		}
		try {
			current_response.set(null);
		} catch (Exception e) {
		}
		index = 0;
	}

	public static HttpServletRequest getCurrentRequest() {
		return current_request.get();
	}
	
	public void setCurrentRequest(HttpServletRequest request) {
		current_request.set(request);
	}

	public void setCurrentResponse(HttpServletResponse response) {
		current_response.set(response);
	}
	
	public static HttpServletResponse getCurrentResponse() {
		return current_response.get();
	}

}
