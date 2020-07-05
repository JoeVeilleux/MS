package com.joev.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple HTTP client
 */
public class SHClient {
	private static final Logger logger = LogManager.getLogger(SHClient.class);
	
	/** Request methods */
	public enum RM {POST, GET, PUT, DELETE}
	
	/** Request properties */
	public enum RP {
		ACC_JSON("Accept", "application/json"),
		ACC_TEXT("Accept", "text/*"),
		CON_JSON("Content-Type", "application/json"),
		;
		String attr;
		String val;
		RP(String attr, String val) {
			this.attr = attr;
			this.val = val;
		}
	}
	
	/**
	 * Utility routine to construct and drive an HTTP request, returning the HTTP response
	 * @param url URL to be contacted
	 * @param requestMethod POST, GET, PUT, or DELETE (see 'RM' enum)
	 * @param body request body text (may be null)
	 * @param requestProperties list of request properties (see 'RP' enum)
	 * @return SHResp encapsulating the interesting elements from the HTTP response
	 * @throws Exception
	 */
	public static SHResp doHttp(String url, RM requestMethod, String body, RP... requestProperties) throws IOException {
		// Set up the HttpURLConnection
		URL reqUrl = new URL(url);
		HttpURLConnection con = (HttpURLConnection) reqUrl.openConnection();
		con.setRequestMethod(requestMethod.toString());
		HashMap<String, List<String>> rpLog = new HashMap<>();
		for (RP rp : requestProperties) {
			rpLog.put(rp.attr, Arrays.asList(rp.val));
			con.setRequestProperty(rp.attr, rp.val);
		}
		if (body != null) {
			con.setDoOutput(true);
			try(OutputStream os = con.getOutputStream()) {
				byte[] bodyBytes = body.getBytes("utf-8");
				os.write(bodyBytes, 0, bodyBytes.length);
			}
		}
		logger.info("Performing HTTP Request: URL={} Method={} Properties={} Body={}",
			reqUrl, requestMethod, rpLog, body);
		// Drive the request and collect the response
		SHResp response = new SHResp(con);
		con.disconnect();
		return response;
	}
	
	/**
	 * Data of interest extracted from the HTTP response
	 */
	public static class SHResp {
		public int responseCode;
		public String responseMessage;
		public String responseBody;
		public Map<String, List<String>> responseHeaders;
		
		public SHResp(HttpURLConnection con) throws IOException {
			this.responseCode = con.getResponseCode();
			this.responseMessage = con.getResponseMessage();
			// Get response body either from the input stream (if success) or error stream (if failure)
			InputStream is =
				(responseCode < 300)
				? con.getInputStream()
				: con.getErrorStream()
			;
			StringBuilder sb = new StringBuilder();
			if (is != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
			}
			this.responseBody = sb.toString();
			this.responseHeaders = con.getHeaderFields();
		}
	}
	
	public static void logResponse(SHResp response) {
		logger.info("Response code: {}/{}", response.responseCode, response.responseMessage);
		logger.info("Response body: {}", response.responseBody);
		logger.info("Response headers: {}", response.responseHeaders);
	}

	public static void main(String[] args) throws Exception {
		SHResp r = doHttp("http://www.google.com", RM.GET, null, RP.ACC_TEXT);
		logResponse(r);
	}
}
