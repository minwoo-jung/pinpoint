package com.profiler.modifier.db.mssql;

import com.profiler.Agent;
import com.profiler.config.ProfilerConstant;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;
import javassist.CtClass;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MSSQLConnectionModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(MSSQLConnectionModifier.class.getName());

    public MSSQLConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/sourceforge/jtds/jdbc/ConnectionJDBC2";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        return changeMethods(javassistClassName, classFileBuffer);
    }

    private byte[] changeMethods(String javassistClassName, byte[] classfileBuffer) {
        try {
            CtClass cc = null;

            updateCreateStatementMethod(cc);
            updateCloseMethod(cc);

            printClassConvertComplete(javassistClassName);

            return cc.toBytecode();
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return null;
    }

    private void updateCreateStatementMethod(CtClass cc) throws Exception {
        CtClass[] params = new CtClass[2];
        params[0] = null;
        params[1] = null;
        CtMethod method = cc.getDeclaredMethod("createStatement", params);

        method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + ProfilerConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT + "); }");
    }

    private void updateCloseMethod(CtClass cc) throws Exception {
        CtMethod method = cc.getDeclaredMethod("close", null);
        method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + ProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + "); }");
    }
}
