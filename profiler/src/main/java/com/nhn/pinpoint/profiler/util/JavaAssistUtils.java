package com.nhn.pinpoint.profiler.util;

import javassist.*;
import javassist.bytecode.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public final class JavaAssistUtils {
    private final static String EMTPY_ARRAY = "()";
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final Logger logger = LoggerFactory.getLogger(JavaAssistUtils.class);

    private JavaAssistUtils() {
    }

    /**
     * test(int, java.lang.String) 일경우
     * (int, java.lang.String)로 생성된다.
     *
     * @param params
     * @return
     */
    public static String getParameterDescription(CtClass[] params) {
        if (params == null) {
            return EMTPY_ARRAY;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getName());
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }

    public static String[] parseParameterDescriptor(String descriptor) {
        if (descriptor == null) {
            throw new NullPointerException("descriptor must not be null");
        }
        final String[] parameterDesc = splitParameterDesc(descriptor);
        final String[] objectType = new String[parameterDesc.length];
        for (int i = 0; i < parameterDesc.length; i++) {
            String description = parameterDesc[i];
            objectType[i] = byteCodeDescToObjectType(description);
        }
        return objectType;
    }

    private static String byteCodeDescToObjectType(String description) {
        final char scheme = description.charAt(0);
        switch (scheme) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'V':
                return "void";
            case 'Z':
                return "boolean";
            case 'L':
                return toObjectType(description, 1);
            case '[': {
                return toArrayType(description);
            }
        }
        throw new IllegalArgumentException("invalid description :" + description);
    }

    private static String toArrayType(String description) {
        final int arraySize = getArraySize(description);

        final char scheme = description.charAt(arraySize);
        switch (scheme) {
            case 'B':
                return arrayType("byte", arraySize);
            case 'C':
                return arrayType("char", arraySize);
            case 'D':
                return arrayType("double", arraySize);
            case 'F':
                return arrayType("float", arraySize);
            case 'I':
                return arrayType("int", arraySize);
            case 'J':
                return arrayType("long", arraySize);
            case 'S':
                return arrayType("short", arraySize);
            case 'V':
                return arrayType("void", arraySize);
            case 'Z':
                return arrayType("boolean", arraySize);
            case 'L':
                final String objectType = toObjectType(description, arraySize + 1);
                return arrayType(objectType, arraySize);
            case '[': {
                throw new IllegalArgumentException("invalid description" + description);
            }
        }
        throw new IllegalArgumentException("invalid description :" + description);
    }

    private static String arrayType(String objectType, int arraySize) {
        final String array = "[]";
        final int arrayStringLength = array.length() * arraySize;
        StringBuilder sb = new StringBuilder(objectType.length() + arrayStringLength);
        sb.append(objectType);
        for (int i = 0; i < arraySize; i++) {
            sb.append(array);
        }
        return sb.toString();
    }

    private static int getArraySize(String description) {
        int arraySize = 0;
        for (int i = 0; i < description.length(); i++) {
            final char c = description.charAt(i);
            if (c == '[') {
                arraySize++;
            } else {
                break;
            }
        }
        return arraySize;
    }

    private static String toObjectType(String description, int startIndex) {
        final String assistClass = description.substring(startIndex, description.length());
        final String objectName = assistClass.replace('/', '.');
        if (objectName.isEmpty()) {
            throw new IllegalArgumentException("invalid description. objectName not found :" + description);
        }
        return objectName;
    }

    private static String[] splitParameterDesc(String descriptor) {
        final String parameterDesc = getParameterDesc(descriptor);
        if (parameterDesc.isEmpty()) {
            return EMPTY_STRING_ARRAY;
        }
        return parameterDesc.split(";");
    }


    private static String getParameterDesc(String descriptor) {
        final int start = descriptor.indexOf('(');
        if (start == -1) {
            throw new IllegalArgumentException("'(' not found. descriptor:" + descriptor);
        }
        final int end = descriptor.indexOf(')', start + 1);
        if (end == -1) {
            throw new IllegalArgumentException("')' not found. descriptor:" + descriptor);
        }
        return descriptor.substring(start + 1, end);
    }

	public static String[] getParameterType(Class[] paramsClass) {
        if (paramsClass == null) {
            return null;
        }
        String[] paramsString = new String[paramsClass.length];
        for (int i = 0; i < paramsClass.length; i++) {
            paramsString[i] = paramsClass[i].getName();
        }
        return paramsString;
    }

    public static String[] getParameterSimpleType(CtClass[] paramsClass) {
        if (paramsClass == null) {
            return null;
        }
        String[] paramsString = new String[paramsClass.length];
        for (int i = 0; i < paramsClass.length; i++) {
            paramsString[i] = paramsClass[i].getSimpleName();
        }
        return paramsString;
    }

    public static String[] getParameterType(CtClass[] paramsClass) {
        if (paramsClass == null) {
            return null;
        }
        String[] paramsString = new String[paramsClass.length];
        for (int i = 0; i < paramsClass.length; i++) {
            paramsString[i] = paramsClass[i].getName();
        }
        return paramsString;
    }

    public static String getParameterDescription(Class[] params) {
        if (params == null) {
            return EMTPY_ARRAY;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getName());
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }


    public static String getParameterDescription(String[] params) {
        if (params == null) {
            return EMTPY_ARRAY;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }

    public static CtClass[] getCtParameter(String[] args, ClassPool pool) throws NotFoundException {
        if (args == null) {
            return null;
        }
        CtClass[] params = new CtClass[args.length];
        for (int i = 0; i < args.length; i++) {
            params[i] = pool.getCtClass(args[i]);
        }
        return params;
    }


    public static int getLineNumber(CtBehavior method) {
        if (method == null) {
            return -1;
        }
        return method.getMethodInfo().getLineNumber(0);
    }


    public CtMethod findAllMethod(CtClass ctClass, String methodName, String[] args) throws NotFoundException {
        if (ctClass == null) {
            throw new NullPointerException("ctClass must not be null");
        }
        if (methodName == null) {
            throw new NullPointerException("methodName must not be null");
        }
        CtClass[] params = getCtParameter(args, ctClass.getClassPool());
        String paramDescriptor = Descriptor.ofParameters(params);
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod method : methods) {
            if (method.getName().equals(methodName) && method.getMethodInfo2().getDescriptor().startsWith(paramDescriptor)) {
                return method;
            }
        }
        throw new NotFoundException(methodName + "(..) is not found in " + ctClass.getName());
    }

    public static boolean isStaticBehavior(CtBehavior behavior) {
        if (behavior == null) {
            throw new NullPointerException("behavior must not be null");
        }
        int modifiers = behavior.getModifiers();
        return Modifier.isStatic(modifiers);
    }


    public static String[] getParameterVariableName(CtBehavior method) throws NotFoundException {
        if (method == null) {
            throw new NullPointerException("method must not be null");
        }
        LocalVariableAttribute localVariableAttribute = lookupLocalVariableAttribute(method);
        if (localVariableAttribute == null) {
            return getParameterDefaultVariableName(method);
        }
        return getParameterVariableName(method, localVariableAttribute);
    }

    /**
     * LocalVariable 메모리 공간을 얻어 온다.
     *
     * @param method
     * @return null일 경우 debug모드로 컴파일 되지 않아서 그럼.
     */
    public static LocalVariableAttribute lookupLocalVariableAttribute(CtBehavior method) {
        if (method == null) {
            throw new NullPointerException("method must not be null");
        }
        MethodInfo methodInfo = method.getMethodInfo2();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        AttributeInfo localVariableTable = codeAttribute.getAttribute(LocalVariableAttribute.tag);
        LocalVariableAttribute local = (LocalVariableAttribute) localVariableTable;
        return local;
    }

    public static String[] getParameterVariableName(CtBehavior method, LocalVariableAttribute localVariableAttribute) throws NotFoundException {
        // http://www.jarvana.com/jarvana/view/org/jboss/weld/servlet/weld-servlet/1.0.1-Final/weld-servlet-1.0.1-Final-sources.jar!/org/slf4j/instrumentation/JavassistHelper.java?format=ok
        // http://grepcode.com/file/repo1.maven.org/maven2/jp.objectfanatics/assertion-weaver/0.0.30/jp/objectfanatics/commons/javassist/JavassistUtils.java
        // 이거 참고함.
        if (localVariableAttribute == null) {
            // null이라는건 debug모드로 컴파일 되지 않았다는 의미이다.
            // parameter class명을 default로 넘기는 건 아래 메소드가 함. getParameterDefaultVariableName.
            return null;
        }

        dump(localVariableAttribute);
        CtClass[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        String[] parameterVariableNames = new String[parameterTypes.length];
        boolean thisExist = thisExist(method);

        int paramIndex = 0;
        for (int i = 0; i < localVariableAttribute.tableLength(); i++) {
            // start pc가 0이 아닐경우 parameter를 나타내는 localVariableName이 아님.
            if (localVariableAttribute.startPc(i) != 0) {
                continue;
            }
            int index = localVariableAttribute.index(i);
            if (index == 0 && thisExist) {
                // this 변수임. skip
                continue;
            }
            String variablename = localVariableAttribute.variableName(i);
            parameterVariableNames[paramIndex++] = variablename;
        }
        return parameterVariableNames;
    }

    private static boolean thisExist(CtBehavior method) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            return false;
        } else {
            // this 포함이므로 1;
            return true;
        }
    }

    private static void dump(LocalVariableAttribute lva) {
        if (logger.isDebugEnabled()) {
            StringBuilder buffer = new StringBuilder(1024);
            for (int i = 0; i < lva.tableLength(); i++) {
                buffer.append("\n");
                buffer.append(i);
                buffer.append("  start_pc:");
                buffer.append(lva.startPc(i));
                buffer.append("  index:");
                buffer.append(lva.index(i));
                buffer.append("  name:");
                buffer.append(lva.variableName(i));
                buffer.append("  nameIndex:");
                buffer.append(lva.nameIndex(i));
            }
            logger.debug(buffer.toString());
        }
    }


    public static String[] getParameterDefaultVariableName(CtBehavior method) throws NotFoundException {
        if (method == null) {
            throw new NullPointerException("method must not be null");
        }
        CtClass[] parameterTypes = method.getParameterTypes();
        String[] variableName = new String[parameterTypes.length];
        for (int i = 0; i < variableName.length; i++) {
            variableName[i] = parameterTypes[i].getSimpleName().toLowerCase();
        }
        return variableName;
    }
}
