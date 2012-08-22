package com.profiler.interceptor;

import javassist.*;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

public class InterceptorRegistryTest {
    @Test
    public void methodName() throws NoSuchMethodException {
        Method[] toString = Map.class.getDeclaredMethods();
        for(Method m : toString) {

            System.out.println(m);

            System.out.println(m.toGenericString());
        }

    }
   @Test
    public void interceptor() throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, IOException {

       InterceptorRegistry.addInterceptor("a", new AroundInterceptor() {
           @Override
           public void before(InterceptorContext ctx) {
               System.out.println("before ctx:" + ctx );
           }

           @Override
           public void after(InterceptorContext ctx) {
               System.out.println("after ctx:" + ctx);
           }
       });


        ClassPool p = ClassPool.getDefault();
       CtClass throwable = p.get(Throwable.class.getName());



       CtClass ctClass = p.get("com.profiler.interceptor.TestObject");
       System.out.println(ctClass);
       final CtMethod hello = ctClass.getMethod("hello", "(Ljava/lang/String;)Ljava/lang/String;");

       CtClass ctx = p.get(InterceptorContext.class.getName());
       hello.addLocalVariable("ctx", ctx);

       CtClass interceptor = p.get(AroundInterceptor.class.getName());

       hello.addLocalVariable("interceptor", interceptor);

       CtClass object = p.get(Object.class.getName());
       hello.addLocalVariable("result", object);

       hello.insertBefore("{" +
               "ctx = new com.profiler.interceptor.InterceptorContext();" +
               "ctx.setParameter($args);" +
//               InterceptorRegistry.class.getName() + ".getInterceptor(\"a\").before(ctx);" +
               "interceptor = (com.profiler.interceptor.AroundInterceptor) " + InterceptorRegistry.class.getName() + ".getInterceptor(\"a\");"+
               "interceptor.before(ctx);" +
               "}");
        hello.addCatch("{" +
//            " interceptor.after(ctx);"+
//           " com.profiler.interceptor.AroundInterceptor a = (com.profiler.interceptor.AroundInterceptor) " + InterceptorRegistry.class.getName() + ".getInterceptor(\"a\");"+
           " throw $e;" +
           "}", throwable);
       hello.insertAfter("{" +
                "interceptor.after(ctx); " +
               "}");


//

//       hello.setBody(generatedAroundInterceptor("com.profiler.interceptor.TestObject", "hello"));
//       hello.setBody("{ System.out.println(\"ddd\");  }", ClassMap map );
       hello.insertBefore(" System.out.println(\" before +  \");");
       hello.insertAfter(" System.out.println($_);");
//       hello.insertAfter(" System.out.println($r);");
//       hello.insertAfter(" System.out.println($w);");
       hello.insertAfter(" System.out.println($sig);");
       hello.insertAfter(" System.out.println($type);");
       hello.insertAfter(" System.out.println($class);");
//       hello.instrument(new ExprEditor() {
//         public void edit(MethodCall m)
//         throws CannotCompileException
//         {
//             try {
//                 System.out.println("method call" + m.getMethod().getName());
//             } catch (NotFoundException e) {
//                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//             }
//             String code = generatedAroundInterceptor("com.profiler.interceptor.TestObject", "hello");
//             m.replace(code);
//         }


//         });
//        hello.addCatch("System.out.println(\"catch\"); throw $e;", throwable);

//       hello.setName("__hello");
//       CtMethod method = CtNewMethod.make("public void hello() { try {__hello(); } catch(Throwable th){throw th;}}", ctClass);

//         CtMethod method = CtNewMethod.make("public void hello() { System.out.println(\"ddd\"); } catch(Throwable th){throw th;}}", ctClass);
//       ctClass.addMethod(method);



       ctClass.freeze();
//       ctClass.writeFile("./debug");
       ctClass.debugWriteFile("./debug");
       Class aClass = ctClass.toClass();
       TestObject o = (TestObject) aClass.newInstance();

//       ctClass.getMethod("toString", null);
//       ctClass.getDeclaredMethod("toString", null);

       try {
           o.hello("aaaaaa");
       } catch (Exception e) {
           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       }

//       o.hello();
   }

    private String generatedAroundInterceptor(String className, String methodName) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("  ctx = new com.profiler.interceptor.InterceptorContext();");
        sb.append("  ctx.setParameter($args);");
        sb.append("  ctx.setTarget(this);");
        sb.append(" ");
//        sb.append("  ctx.setMethodName(\"" + methodName + "\");");
//        sb.append("  System.out.println(\"args check : \" + $args );");
//        sb.append("  System.out.println(\"0 check : \" + $0 );");
//        sb.append("  System.out.println(\"1 check : \" + $1 );");
//        sb.append("  System.out.println(\"sig check : \" + $sig );");
//        sb.append("  System.out.println(\"class check : \" + $class );");
//        sb.append("  System.out.println(\" r check : \" + $r);");

        sb.append("}");
        sb.append("{");
        sb.append("  interceptor = (com.profiler.interceptor.AroundInterceptor) " + InterceptorRegistry.class.getName() + ".getInterceptor(\"a\");");
        sb.append("  interceptor.before(ctx);");
        sb.append("  result = null;");
//        println(sb, "before systemout \"ttt\"");
        sb.append("}");
        sb.append("try {");
        sb.append("  $_ = $proceed($$);");
        sb.append("  result = $_;");
        sb.append("}");
//        sb.append("catch(Throwable th) {");
//        sb.append("  System.out.println(\"test11\" + th);");
//        sb.append("  ctx.setException(th);");
//        sb.append("  System.out.println(\"catch\");");
//        sb.append("}");
        sb.append("finally {");
//        sb.append("  System.out.println(\"finally\");");
        sb.append("  ctx.setReturnValue(result);");
        sb.append("  interceptor.after(ctx);");
        sb.append("}");
//        System.out.println(sb);
        return sb.toString();
    }
    public void println(StringBuilder sb, String out) {
        sb.append("System.out.println(\"" + out.replace("\"", "\\\"") + "\");");
    }
}
