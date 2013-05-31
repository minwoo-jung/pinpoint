package com.nhn.pinpoint.bootstrap;

import java.util.concurrent.Callable;

/**
 * contextClassLoader에 별도의 classLoader를 세팅하고 실행하는 template
 */
public class ContextClassLoaderExecuteTemplate<V> {
    private final ClassLoader classLoader;

    public ContextClassLoaderExecuteTemplate(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public V execute(Callable<V> callable) throws BootStrapException {
        Thread currentThread = Thread.currentThread();
        final ClassLoader before = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(ContextClassLoaderExecuteTemplate.this.classLoader);
        try {
            return callable.call();
        } catch (BootStrapException ex){
            throw ex;
        } catch (Exception ex) {
            throw new BootStrapException("execute fail. Caused:" + ex.getMessage(), ex);
        } finally {
            currentThread.setContextClassLoader(before);
        }
    }
}
