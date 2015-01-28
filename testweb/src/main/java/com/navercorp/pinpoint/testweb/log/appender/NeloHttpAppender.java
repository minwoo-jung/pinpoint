package com.navercorp.pinpoint.testweb.log.appender;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggingEvent;

import com.nhncorp.nelo2.Constants;

public class NeloHttpAppender extends com.nhncorp.nelo2.log4j.NeloHttpAppender {
    
    @SuppressWarnings("unchecked")
    HashMap<String, String> getEventToMap(LoggingEvent event) {
        HashMap<String, String> return_map = new HashMap<String, String>();
        setField(return_map, Constants.NELO_FIELD_CLIENT_IP, getClientIp(event));

        setField(return_map, Constants.NELO_FIELD_PROJECT_NAME, getSoftwareId(event));
        setField(return_map, Constants.NELO_FIELD_PROJECT_VERSION, getSoftwareVersion(event));
        setField(return_map, Constants.NELO_FIELD_ERROR_LEVEL, getErrorLevel(event));
        setField(return_map, Constants.NELO_FIELD_LOG_SOURCE, getLogSource(event));
        setField(return_map, Constants.NELO_FIELD_LOG_TYPE, getLogType(event));
        setField(return_map, Constants.NELO_FIELD_HOST, getHost(event));
        setField(return_map, Constants.NELO_FIELD_SENDTIME, Long.toString(getSendTime(event)));
        setField(return_map, Constants.NELO_FIELD_BODY, getBody(event));
        setField(return_map, Constants.NELO_FIELD_USER_ID, getValueFromMDC(event, "userId"));
        setField(return_map, Constants.NELO_FIELD_URL, getValueFromMDC(event, "url"));
        setField(return_map, Constants.NELO_FIELD_COOKIE, getValueFromMDC(event, "cookie"));
        setField(return_map, Constants.NELO_FIELD_FORM_DATA, getValueFromMDC(event, "form"));
        setField(return_map, Constants.NELO_FIELD_LOCATION, getLocation(event));
        setField(return_map, Constants.NELO_FIELD_EXCEPTION, getThrownInfo(event));
        setField(return_map, Constants.NELO_FIELD_ERROR_CODE, getErrorCode(event));
        setField(return_map, Constants.NELO_FIELD_REQUEST_HEADER, getHeaderInfo(event));
        setField(return_map, Constants.NELO_FIELD_CHARSET_NAME, encoding);
        setField(return_map, Constants.NELO_FIELD_PHASE, getPhase());
        
        setField(return_map, "transactionId", getValueFromMDC(event, "transactionId"));
        
//      LocationInfo locationInfo = event.getLocationInformation();
//      if (locationInfo != null) {
//          setField(return_map, Constants.NELO_FIELD_CLASSNAME, StringUtils.defaultIfEmpty(locationInfo.getClassName(), "") + ":" + StringUtils.defaultIfEmpty(locationInfo.getMethodName(), ""));
//      }
        setField(return_map, Constants.NELO_FIELD_ENABLE_ADD_PHASE_TO_PROJECT_NAME, getEnableAddPhaseToProjectName());
        setField(return_map, Constants.NELO_FIELD_PLATFORM, System.getProperty("os.name"));

        Map<String, Object> mdcMessage = MDC.getContext();
        if (mdcMessage == null || mdcMessage.keySet() == null) {
            printDebugConsole("mdcMessage IS NULL");
        } else {
            for (Entry<String, Object> each : mdcMessage.entrySet()) {
                if (!checkMDCReserved(each.getKey())) {
                    printDebugConsole("[KEY : " + each.getKey() + " / Value : " + each.getValue() + "]");
                    setCustomField(return_map, each.getKey(), each.getValue());
                }
            }
        }
        printDebugConsole("[NELO] message to be sent : \r\n " + return_map);
        return return_map;
    }

    private String getTransactionId() {
        // TODO Auto-generated method stub
        return null;
    }
}
