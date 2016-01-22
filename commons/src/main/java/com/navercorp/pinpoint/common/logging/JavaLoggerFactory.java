package com.navercorp.pinpoint.common.logging;

public class JavaLoggerFactory {
    private static PLoggerBinder loggerBinder = new JavaLoggerBinder();

    public static void unregister(PLoggerBinder loggerBinder) {
        // Limited to remove only the ones already registered
        // when writing a test case, logger register/unregister logic must be located in beforeClass and afterClass
        if (loggerBinder == JavaLoggerFactory.loggerBinder) {
            JavaLoggerFactory.loggerBinder = null;
        }
    }

    public static PLogger getLogger(String name) {
        if (loggerBinder == null) {
            // this prevents null exception: need to return Dummy until a Binder is assigned
            return DummyPLogger.INSTANCE;
        }
        return loggerBinder.getLogger(name);
    }

    public static PLogger getLogger(Class clazz) {
        if (clazz == null) {
            throw new NullPointerException("class must not be null");
        }
        return getLogger(clazz.getName());
    }
}
