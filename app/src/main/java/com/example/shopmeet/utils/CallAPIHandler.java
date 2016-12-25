package com.example.shopmeet.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class CallAPIHandler {
	static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
    private final char PARAMETER_DELIMITER = '&';
    private final char PARAMETER_EQUALS_CHAR = '=';
	public final static int TIMEOUT = 3000;
    private final String TAG = CallAPIHandler.class.getSimpleName();
 
    public CallAPIHandler() {
    	
    }
 
    public String requestGET(String sUrl, String sParameters) {
    	try {
	    	URL url = new URL(sUrl);
	    	HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
	        connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
			connection.setConnectTimeout(TIMEOUT);
	
	        int responseCode = connection.getResponseCode();
	
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line = "";
	        StringBuilder responseOutput = new StringBuilder();
	        while((line = br.readLine()) != null ) {
	            responseOutput.append(line);
	        }
	        br.close();
	
	        return responseOutput.toString();
    	} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	return null;
    }
    public String requestPOST(String sUrl, String sParameters) {
    	try {
    		URL url = new URL(sUrl);
	    	HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
	        connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
			connection.setConnectTimeout(TIMEOUT);
	        connection.setDoOutput(true);
	        DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
	        dStream.writeBytes(sParameters);
	        dStream.flush();
	        dStream.close();
	        int responseCode = connection.getResponseCode();
	
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line = "";
	        StringBuilder responseOutput = new StringBuilder();
	        while((line = br.readLine()) != null ) {
	            responseOutput.append(line);
	        }
	        br.close();
	    
	        return responseOutput.toString();
    	} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	return null;
    }
    public String createQueryStringForParameters(Map<String, String> parameters) {
        StringBuilder parametersAsQueryString = new StringBuilder();
        if (parameters != null) {
            boolean firstParameter = true;
             
            for (String parameterName : parameters.keySet()) {
                if (!firstParameter) {
                    parametersAsQueryString.append(PARAMETER_DELIMITER);
                } 
                 
                parametersAsQueryString.append(parameterName)
                    .append(PARAMETER_EQUALS_CHAR)
                    .append(URLEncoder.encode(
                        parameters.get(parameterName)));
                 
                firstParameter = false;
            }
        }
        return parametersAsQueryString.toString();
    }
    
}
