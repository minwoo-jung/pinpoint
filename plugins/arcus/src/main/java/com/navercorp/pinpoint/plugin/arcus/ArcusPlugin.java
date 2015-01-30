package com.navercorp.pinpoint.plugin.arcus;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder.InterceptorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder.MethodEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.plugin.arcus.filter.ArcusMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.FrontCacheMemcachedMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.MemcachedMethodFilter;

public class ArcusPlugin implements ProfilerPlugin {
    @Override
    public List<ClassEditor> getClassEditors(ProfilerPluginContext context) {
        ArcusPluginConfig config = new ArcusPluginConfig(context.getConfig());
        
        boolean arcus = config.isArcus();
        boolean memcached = config.isMemcached();

        List<ClassEditor> editors = new ArrayList<ClassEditor>();

         if (arcus) {
            editors.add(getArcusClientEditor(context, config));
            editors.add(getCollectionFutureEditor(context));
        }
        
        if (arcus || memcached) {
            editors.add(getBaseOperationImplEditor(context));        
            editors.add(getCacheManagerEditor(context));
            
            editors.add(getGetFutureEditor(context));
            // TODO ImmedateFuture doesn't have setOperation(Operation) method.
//            editors.add(getImmediateFutureEditor(context));
            editors.add(getOperationFutureEditor(context));
            
            editors.add(getFrontCacheGetFutureEditor(context));
            editors.add(getFrontCacheMemcachedClientEditor(context, config));
            editors.add(getMemcachedClientEditor(context, config));
        }

        return editors;
    }
    
    private ClassEditor getArcusClientEditor(ProfilerPluginContext context, ArcusPluginConfig config) {
        boolean traceKey = config.isArcusKeyTrace();
        
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.ArcusClient");
        builder.condition(new ClassCondition() {
                @Override
                public boolean check(ClassLoader classLoader, InstrumentClass target) {
                    return target.hasMethod("addOp", new String[] {"java.lang.String", "net.spy.memcached.ops.Operation"}, "net.spy.memcached.ops.Operation");
                }
        });
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetMethod("setCacheManager", "net.spy.memcached.CacheManager");
        mb.injectInterceptor().interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor");
        
        MethodEditorBuilder mb2 = builder.editMethod();
        mb2.targetFilter(new ArcusMethodFilter());
        mb2.cacheApi();
        InterceptorBuilder ib2 = mb2.injectInterceptor();
        ib2.interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor");
        ib2.constructorArgs(traceKey);
        ib2.scope(Constants.ARCUS_SCOPE);
        
        return builder.build();
    }
    
    private ClassEditor getCacheManagerEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.CacheManager");
        
        builder.inject(ArcusMetadata.SERVICE_CODE);
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetConstrucotor("java.lang.String", "java.lang.String", "net.spy.memcached.ConnectionFactoryBuilder", "java.util.concurrent.CountDownLatch", "int", "int");
        mb.injectInterceptor().interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor");
        
        return builder.build();
    }

    
    private ClassEditor getBaseOperationImplEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.protocol.BaseOperationImpl");
        builder.inject(ArcusMetadata.SERVICE_CODE);

        return builder.build();
    }
    
    private ClassEditor getFrontCacheGetFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.plugin.FrontCacheGetFuture");
        builder.inject(ArcusMetadata.CACHE_NAME);
        builder.inject(ArcusMetadata.CACHE_KEY);
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetConstrucotor("net.sf.ehcache.Element");
        mb.injectInterceptor().interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor");
        
        MethodEditorBuilder mb2 = builder.editMethod();
        mb2.targetMethod("get", "long", "java.util.concurrent.TimeUnit");
        mb2.cacheApi();
        InterceptorBuilder ib2 = mb2.injectInterceptor();
        ib2.interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");
        ib2.scope(Constants.ARCUS_SCOPE);
        
        MethodEditorBuilder mb3 = builder.editMethod();
        mb3.targetMethod("get");
        mb3.cacheApi();
        InterceptorBuilder ib3 = mb3.injectInterceptor();
        ib3.interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");
        ib3.scope(Constants.ARCUS_SCOPE);
        
        return builder.build();
    }
    
    private ClassEditor getFrontCacheMemcachedClientEditor(ProfilerPluginContext context, ArcusPluginConfig config) {
        boolean traceKey = config.isMemcachedKeyTrace();
        
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.plugin.FrontCacheMemcachedClient");
        builder.condition(new ClassCondition() {

            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.hasDeclaredMethod("putFrontCache", new String[] { "java.lang.String", "java.util.concurrent.Future", "long" });
            }
            
        });
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetFilter(new FrontCacheMemcachedMethodFilter());
        mb.cacheApi();
        InterceptorBuilder ib = mb.injectInterceptor();
        ib.interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor");
        ib.constructorArgs(traceKey);
        ib.scope(Constants.ARCUS_SCOPE);
                        
        return builder.build();
    }


    private ClassEditor getMemcachedClientEditor(ProfilerPluginContext context, ArcusPluginConfig config) {
        boolean traceKey = config.isMemcachedKeyTrace();
        
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.MemcachedClient");
        builder.condition(new ClassCondition() {

            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.hasDeclaredMethod("addOp", new String[] { "java.lang.String", "net.spy.memcached.ops.Operation" });
            }
            
        });
        
        builder.inject(ArcusMetadata.SERVICE_CODE);
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetMethod("addOp", "java.lang.String", "net.spy.memcached.ops.Operation");
        mb.injectInterceptor().interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.AddOpInterceptor");
        
        MethodEditorBuilder mb2 = builder.editMethod();
        mb2.targetFilter(new MemcachedMethodFilter());
        mb2.cacheApi();
        InterceptorBuilder ib2 = mb2.injectInterceptor();
        ib2.interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor");
        ib2.scope(Constants.ARCUS_SCOPE);
        ib2.constructorArgs(traceKey);
                        
        return builder.build();
    }

    private ClassEditor getFutureEditor(ClassEditorBuilder builder) {
        builder.inject(ArcusMetadata.OPERATION);
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetMethod("setOperation", "net.spy.memcached.ops.Operation");
        mb.injectInterceptor().interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor");
        
        MethodEditorBuilder mb2 = builder.editMethod();
        mb2.targetMethod("get", "long", "java.util.concurrent.TimeUnit");
        mb2.cacheApi();
        InterceptorBuilder ib2 = mb2.injectInterceptor();
        ib2.interceptorClass("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor");
        ib2.scope(Constants.ARCUS_SCOPE);
        
        return builder.build();
    }
        
    private ClassEditor getCollectionFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.internal.CollectionFuture");
        return getFutureEditor(builder);
    }
    
    private ClassEditor getGetFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.internal.GetFuture");
        return getFutureEditor(builder);
    }

    private ClassEditor getOperationFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.internal.OperationFuture");
        return getFutureEditor(builder);
    }

    private ClassEditor getImmediateFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.internal.ImmediateFuture");
        return getFutureEditor(builder);
    }

}