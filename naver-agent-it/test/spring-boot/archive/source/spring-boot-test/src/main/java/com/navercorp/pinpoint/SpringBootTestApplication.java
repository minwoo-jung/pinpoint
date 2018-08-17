package com.navercorp.pinpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;

@SpringBootApplication
public class SpringBootTestApplication {

    private static final String TEST_ID_KEY = "pinpoint.test.id";
    private static final String TEST_CLASS_KEY = "pinpoint.test.class";

    public static void main(String[] args) {
        SpringApplication.run(SpringBootTestApplication.class, args);

        Properties properties = System.getProperties();
        final String testId = properties.getProperty(TEST_ID_KEY);
        final String testClass = properties.getProperty(TEST_CLASS_KEY);

        SpringBootIntegrationTestRunner testRunner = new SpringBootIntegrationTestRunner(testId, testClass);
        testRunner.run();
    }

    private static class SpringBootIntegrationTestRunner {

        private static final String IT_RUNNER_CLASS =
                "com.navercorp.pinpoint.plugin.spring.boot.runner.SpringBootIntegrationTestRunner";

        private final String testId;
        private final String testClass;

        private SpringBootIntegrationTestRunner(String testId, String testClass) {
            this.testId = testId;
            this.testClass = testClass;
        }

        private void run() {
            try {
                Class<?> testClass = ClassLoader.getSystemClassLoader().loadClass(IT_RUNNER_CLASS);
                Constructor<?> constructor = testClass.getConstructor(String.class, String.class);
                Method runMethod = testClass.getDeclaredMethod("run");
                runMethod.invoke(constructor.newInstance(this.testId, this.testClass));
            } catch (Exception e) {
                throw new RuntimeException("Error running test [" + this.testClass + "].", e);
            }
        }
    }
}
