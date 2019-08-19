package com.navercorp.pinpoint.web.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NaverJobFailMessageSender implements JobFailMessageSender {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String mexServerUrl;
    private final List<String> cellPhoneNumbers;
    private final String batchEnv;
    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private final String senderNumber;

    public NaverJobFailMessageSender(NaverBatchConfiguration naverBatchConfiguration, BatchConfiguration batchConfiguration, RestTemplate restTemplate) {
        this.mexServerUrl = naverBatchConfiguration.getMexServerUrl();
        this.cellPhoneNumbers = naverBatchConfiguration.getCellPhoneNumberList();
        this.senderNumber = naverBatchConfiguration.getSenderNumber();
        this.restTemplate = restTemplate;
        this.batchEnv = batchConfiguration.getBatchEnv();
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("api-key", naverBatchConfiguration.getApiKey());
    }

	@Override
	public void sendSMS(JobExecution jobExecution) {
		final String jobName = jobExecution.getJobInstance().getJobName();
		final Date startTime = jobExecution.getStartTime();
        final String message = "[PINPOINT-" + batchEnv + "]batch job fail\n jobName : " + jobName + "\n start : " + startTime + "\n end : NOW";

        for (String number : cellPhoneNumbers) {
            try {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("sender", senderNumber);
                parameters.put("receiver", number);
                parameters.put("text", message);
                parameters.put("countryCode", "82");
                parameters.put("type", "sms");

                HttpEntity httpEntity = new HttpEntity(parameters, headers);
                ResponseEntity<Map> response = this.restTemplate.exchange(mexServerUrl, HttpMethod.POST, httpEntity, Map.class);

                if (response.getStatusCode() != HttpStatus.OK) {
                    logger.error("fail send sms message for batch fail. response message : {}", response.getBody().toString());
                }
            } catch (HttpStatusCodeException ex ) {
                logger.error("fail send sms message for batch fail. reponseBody message: {}", ex.getResponseBodyAsString(), ex);
            } catch (Exception ex) {
                logger.error("fail send sms message for batch fail. Caused: {}", ex.getMessage(), ex);
            }
        }
	}

	@Override
	public void sendEmail(JobExecution jobExecution) {
	}

}
