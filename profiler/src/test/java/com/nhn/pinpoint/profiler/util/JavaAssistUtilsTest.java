package com.nhn.pinpoint.profiler.util;

import javassist.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author emeroad
 */
public class JavaAssistUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(JavaAssistUtilsTest.class.getName());
    private ClassPool pool;

    @Before
    public void setUp() throws Exception {
        pool = new ClassPool();
        pool.appendSystemPath();
    }

    @Test
    public void testGetParameterDescription() throws Exception {
        CtClass ctClass = pool.get("java.lang.String");
        CtMethod substring = ctClass.getDeclaredMethod("substring", new CtClass[]{CtClass.intType});

        String ctDescription = JavaAssistUtils.getParameterDescription(substring.getParameterTypes());
        logger.info(ctDescription);

        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{int.class});
        logger.info(clsDescription);
        Assert.assertEquals(ctDescription, clsDescription);
    }


    @Test
    public void javaArraySize() {
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize(""), 0);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("[]"), 1);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("[][][]"), 3);

        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("int"), 0);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("int[]"), 1);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("int[][][]"), 3);


        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("java.lang.String"), 0);
        Assert.assertEquals(JavaAssistUtils.getJavaObjectArraySize("java.lang.String[][]"), 2);
    }


    @Test
    public void toJvmSignature() {
        Assert.assertEquals(JavaAssistUtils.toJvmSignature(""), "");

        Assert.assertEquals(JavaAssistUtils.toJvmSignature("int"), "I");
        Assert.assertEquals(JavaAssistUtils.toJvmSignature("int[]"), "[I");
        Assert.assertEquals(JavaAssistUtils.toJvmSignature("int[][][]"), "[[[I");

        Assert.assertEquals(JavaAssistUtils.toJvmSignature("void"), "V");

        Assert.assertEquals(JavaAssistUtils.toJvmSignature("java.lang.String"), "Ljava/lang/String;");
        Assert.assertEquals(JavaAssistUtils.toJvmSignature("java.lang.String[][]"), "[[Ljava/lang/String;");

    }

    @Test
    public void javaTypeToJvmSignature() {
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{}), "()");

        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int"}), "(I)");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"int", "double"}), "(ID)");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature( new String[]{"byte", "float", "short"}), "(BFS)");


        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String"}), "(Ljava/lang/String;)");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"java.lang.String", "long"}), "(Ljava/lang/String;J)");

        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"long", "java.lang.Object", "boolean"}), "(JLjava/lang/Object;Z)");
        Assert.assertEquals(JavaAssistUtils.javaTypeToJvmSignature(new String[]{"char", "long", "java.lang.Object", "boolean"}), "(CJLjava/lang/Object;Z)");
    }


    @Test
    public void testParseParameterDescriptor() throws Exception {
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("()V"), new String[]{});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(I)I"), new String[]{"int"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(ID)I"), new String[]{"int", "double"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(BFS)I"), new String[]{"byte", "float", "short"});


        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;)I"), new String[]{"java.lang.String"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;J)I"), new String[]{"java.lang.String", "long"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(JLjava/lang/Object;Z)I"), new String[]{"long", "java.lang.Object", "boolean"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(CJLjava/lang/Object;Z)I"), new String[]{"char", "long", "java.lang.Object", "boolean"});

    }

    @Test
    public void testParseParameterDescriptor_array() throws Exception {

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([I)I"), new String[]{"int[]"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([IJ)I"), new String[]{"int[]", "long"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([J[I)I"), new String[]{"long[]", "int[]"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([Ljava/lang/String;)"), new String[]{"java.lang.String[]"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/String;[[J)"), new String[]{"java.lang.String", "long[][]"});
        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("(Ljava/lang/Object;[[Ljava/lang/String;)"), new String[]{"java.lang.Object", "java.lang.String[][]"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([[[Ljava/lang/String;)"), new String[]{"java.lang.String[][][]"});

        Assert.assertArrayEquals(JavaAssistUtils.parseParameterSignature("([[[I)"), new String[]{"int[][][]"});
    }


    @Test
    public void testGetLineNumber() throws Exception {
//        pool.appendClassPath(new ClassClassPath(AbstractHttpClient.class));
        CtClass ctClass = pool.get("org.apache.http.impl.client.AbstractHttpClient");
        CtClass params = pool.get("org.apache.http.params.HttpParams");
        // non-javadoc, see interface HttpClient
//        public synchronized final HttpParams getParams() {
//            if (defaultParams == null) {
//                defaultParams = createHttpParams();
//            }
//            return defaultParams;
//        }

        CtMethod setParams = ctClass.getDeclaredMethod("setParams", new CtClass[]{params});
        int lineNumber = JavaAssistUtils.getLineNumber(setParams);
        logger.info("line:" + lineNumber);

        logger.info(setParams.getName());
        logger.info(setParams.getLongName());

        String[] paramName = JavaAssistUtils.getParameterVariableName(setParams);
        logger.info(Arrays.toString(paramName));
        Assert.assertEquals(paramName.length, 1);
        Assert.assertEquals(paramName[0], "params");

        String[] parameterType = JavaAssistUtils.parseParameterSignature(setParams.getSignature());
        String[] parameterType2 = JavaAssistUtils.getParameterType(setParams.getParameterTypes());
        logger.info(Arrays.toString(parameterType));
        Assert.assertArrayEquals(parameterType, parameterType2);

        String s = ApiUtils.mergeParameterVariableNameDescription(parameterType, paramName);
        logger.info(s);
    }

    @Test
    public void testVariableNameError1() throws Exception {
        CtClass ctClass = pool.get("com.mysql.jdbc.ConnectionImpl");
        CtClass params = pool.get("org.apache.http.params.HttpParams");
        CtMethod setParams = ctClass.getDeclaredMethod("setAutoCommit", new CtClass[]{CtClass.booleanType});
        int lineNumber = JavaAssistUtils.getLineNumber(setParams);
        logger.info("line:" + lineNumber);

        logger.info(setParams.getName());
        logger.info(setParams.getLongName());

        String[] paramName = JavaAssistUtils.getParameterVariableName(setParams);
        logger.info(Arrays.toString(paramName));
        Assert.assertEquals(paramName.length, 1);
        Assert.assertEquals(paramName[0], "autoCommitFlag");

        String[] parameterType = JavaAssistUtils.parseParameterSignature(setParams.getSignature());
        String[] parameterType2 = JavaAssistUtils.getParameterType(setParams.getParameterTypes());
        logger.info(Arrays.toString(parameterType));
        Assert.assertArrayEquals(parameterType, parameterType2);

        String s = ApiUtils.mergeParameterVariableNameDescription(parameterType, paramName);
        logger.info(s);
    }

    @Test
    public void testVariableNameError2() throws Exception {
        CtClass ctClass = pool.get("com.mysql.jdbc.StatementImpl");
        CtClass params = pool.get("java.lang.String");
        CtMethod setParams = ctClass.getDeclaredMethod("executeQuery", new CtClass[]{params});
        int lineNumber = JavaAssistUtils.getLineNumber(setParams);

        logger.info(setParams.getName());
        logger.info(setParams.getLongName());

        String[] paramName = JavaAssistUtils.getParameterVariableName(setParams);
        logger.info(Arrays.toString(paramName));
        Assert.assertEquals(paramName.length, 1);
        Assert.assertEquals(paramName[0], "sql");

        String[] parameterType = JavaAssistUtils.parseParameterSignature(setParams.getSignature());
        String[] parameterType2 = JavaAssistUtils.getParameterType(setParams.getParameterTypes());
        logger.info(Arrays.toString(parameterType));
        Assert.assertArrayEquals(parameterType, parameterType2);

        String s = ApiUtils.mergeParameterVariableNameDescription(parameterType, paramName);
        logger.info(s);
    }


    @Test
    public void testGetParameterDescription2() throws Exception {
        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{String.class, Integer.class});
        logger.info(clsDescription);
        Assert.assertEquals("(java.lang.String, java.lang.Integer)", clsDescription);
    }

}
