package com.navercorp.pinpoint.web.batch;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Value;

public class NaverJobFailMessageSender implements JobFailMessageSender {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String mexServerUrl;
    private final List<String> cellPhoneNumbers;
    private final String serviceID;
    private static final String SENDER_NUMBER = "0317844499";
    
    @Value("#{batchProps['batch.server.env']}")
    private String batchEnv;
    
    public NaverJobFailMessageSender(String mexServerUrl, String serviceID, List<String> cellPhoneNumbers) {
        this.mexServerUrl = mexServerUrl;
        this.serviceID = serviceID;
        this.cellPhoneNumbers = cellPhoneNumbers;
    }

	@Override
	public void sendSMS(JobExecution jobExecution) {
		String jobName = jobExecution.getJobInstance().getJobName();
		Date startTime = jobExecution.getStartTime();
		
        String encodeMsg = encodeMessage("[PINPOINT-" + batchEnv + "]batch job fail\n jobName : " + jobName + "\n start : " + startTime + "\n end : NOW");

        for (String number : cellPhoneNumbers) {
            String url = mexServerUrl + "?serviceId=\"" + serviceID + "\""
                    + "&sendMdn=\"" + SENDER_NUMBER + "\""
                    + "&receiveMdnList=[\"" + number + "\"]"
                    + "&content=\"" + encodeMsg + "\"";

            HttpURLConnection connection = null;
            try {
                connection = openHttpURLConnection(url);
                connection.setRequestMethod("GET");
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    logger.error("fail send sms message for batch fail.");
                }
            } catch (IOException ex) {
                logger.error("fail send sms message for batch fail. Caused:" + ex.getMessage(), ex);
            } finally {
                close(connection);
            }
        }
	}
	
	private HttpURLConnection openHttpURLConnection(String url) throws IOException{
        URL submitURL = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) submitURL.openConnection();
        httpURLConnection.setConnectTimeout(3000);
        httpURLConnection.setReadTimeout(3000);
        httpURLConnection.setRequestProperty("Content-Language", "utf-8");
        return httpURLConnection;
    }

    private void close(HttpURLConnection connection) {
        if (connection != null) {
            try {
                final InputStream is = connection.getInputStream();
                is.close();
            } catch (IOException ignore) {
                // skip
            }
        }
    }

    private String encodeMessage(String message) {
        message = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r\n", "\\n").replace("\r", "\\n").replace("\n", "\\n");
        try {
            return URLEncoder.encode(message, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            logger.error("Can't encoding sms message.");
            return "batch job fail";
        }
    }

	@Override
	public void sendEmail(JobExecution jobExecution) {
	}

}
