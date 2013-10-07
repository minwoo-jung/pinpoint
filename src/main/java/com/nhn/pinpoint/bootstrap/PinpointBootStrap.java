package com.nhn.pinpoint.bootstrap;

import com.nhn.pinpoint.ProductInfo;
import com.nhn.pinpoint.common.PinpointConstants;
import com.nhn.pinpoint.common.util.TransactionIdUtils;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;

import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class PinpointBootStrap {

    private static final Logger logger = Logger.getLogger(PinpointBootStrap.class.getName());

    private static final int LIMIT_LENGTH = 24;
    private static final String DELIMITER = TransactionIdUtils.TRANSACTION_ID_DELIMITER;

    public static final String BOOT_CLASS = "com.nhn.pinpoint.profiler.DefaultAgent";


    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (agentArgs != null) {
            logger.info(ProductInfo.CAMEL_NAME + " agentArgs:" + agentArgs);
        }

        ClassPathResolver classPathResolver = new ClassPathResolver();
        boolean agentJarNotFound = classPathResolver.findAgentJar();
        if (!agentJarNotFound) {
            // TODO 이거 변경해야 함.
            logger.severe("pinpoint-bootstrap-x.x.x.jar not found.");
            return;
        }

        if (!checkProfilerIdSize("pinpoint.agentId", PinpointConstants.AGENT_NAME_MAX_LEN)) {
            return;
        }
        if (!checkProfilerIdSize("pinpoint.applicationName", PinpointConstants.APPLICATION_NAME_MAX_LEN)) {
            return;
        }

        String configPath = getConfigPath(classPathResolver);
        if (configPath == null ) {
            // 설정파일을 못찾으므로 종료.
            return;
        }
        // 로그가 저장될 위치를 시스템 properties로 저장한다.
        saveLogFilePath(classPathResolver);

        try {
            // 설정파일 로드 이게 bootstrap에 있어야 되나는게 맞나?
            ProfilerConfig profilerConfig = new ProfilerConfig();
            profilerConfig.readConfigFile(configPath);

            // 이게 로드할 lib List임.
            List<URL> libUrlList = resolveLib(classPathResolver);
            AgentClassLoader agentClassLoader = new AgentClassLoader(libUrlList.toArray(new URL[libUrlList.size()]));
            agentClassLoader.setBootClass(BOOT_CLASS);
            agentClassLoader.boot(agentArgs, instrumentation, profilerConfig);

        } catch (Exception e) {
            logger.log(Level.SEVERE, ProductInfo.CAMEL_NAME + " start fail. Caused:" + e.getMessage(), e);
        }

    }

    private static boolean checkProfilerIdSize(String propertyName, int maxSize) {
        logger.info("check " + propertyName);
        final String value = System.getProperty(propertyName);
        if (value != null) {
            final byte[] bytes;
            try {
                bytes = toBytes(value);
            } catch (UnsupportedEncodingException e) {
                logger.warning("toBytes() fail. propertyName:" + propertyName + " propertyValue:" + value);
                return false;
            }
            if (bytes.length > maxSize) {
                logger.warning("invalid " + propertyName + ". too large bytes. length:" + bytes.length + " value:" + value);
                return false;
            }
        }
        logger.info("check success. " + propertyName + ":" + value);
        return true;
    }

    private static byte[] toBytes(String property) throws UnsupportedEncodingException {
        return property.getBytes("UTF-8");
    }

    private static void saveLogFilePath(ClassPathResolver classPathResolver) {
        String agentLogFilePath = classPathResolver.getAgentLogFilePath();
        logger.info("logPath:" + agentLogFilePath);

        System.setProperty(ProductInfo.NAME + "." + "log", agentLogFilePath);
    }

    private static String getConfigPath(ClassPathResolver classPathResolver) {
        final String configName = ProductInfo.NAME + ".config";
        String pinpointConfigFormSystemProperty = System.getProperty(configName);
        if (pinpointConfigFormSystemProperty != null) {
            logger.info(configName + " systemProperty found. " + pinpointConfigFormSystemProperty);
            return pinpointConfigFormSystemProperty;
        }

        String classPathAgentConfigPath = classPathResolver.getAgentConfigPath();
        if (classPathAgentConfigPath != null) {
            logger.info("classpath " + configName +  " found. " + classPathAgentConfigPath);
            return classPathAgentConfigPath;
        }

        logger.severe(configName + " file not found.");
        return null;
    }


    private static List<URL> resolveLib(ClassPathResolver classPathResolver)  {
        // 절대경로만 처리되지 않나함. 상대 경로(./../agentlib/lib등)일 경우의 처리가 있어야 될것 같음.
        String agentJarFullPath = classPathResolver.getAgentJarFullPath();
        String agentLibPath = classPathResolver.getAgentLibPath();
        List<URL> urlList = classPathResolver.resolveLib();
        String agentConfigPath = classPathResolver.getAgentConfigPath();

        if (logger.isLoggable(Level.INFO)) {
            logger.info("agentJarPath:" + agentJarFullPath);
            logger.info("agentLibPath:" + agentLibPath);
            logger.info("agent lib list:" + urlList);
            logger.info("agent config:" + agentConfigPath);
        }

        return urlList;
    }



}
