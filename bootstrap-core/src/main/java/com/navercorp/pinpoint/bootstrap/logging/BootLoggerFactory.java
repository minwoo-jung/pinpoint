package com.navercorp.pinpoint.bootstrap.logging;

public class BootLoggerFactory {
    private static final PLoggerBinder loggerBinder = new BootLoggerBinder();

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
