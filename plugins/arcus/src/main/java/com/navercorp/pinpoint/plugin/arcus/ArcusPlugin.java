package com.navercorp.pinpoint.plugin.arcus;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.plugin.arcus.filter.ArcusMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.FrontCacheMemcachedMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.MemcachedMethodFilter;

public class ArcusPlugin implements ProfilerPlugin, ArcusConstants {
    
    @Override
    public void setUp(ProfilerPluginSetupContext context) {
        ArcusPluginConfig config = new ArcusPluginConfig(context.getConfig());
        context.setAttribute(ArcusConstants.ATTRIBUTE_CONFIG, config);
        
        boolean arcus = config.isArcus();
        boolean memcached = config.isMemcached();


        if (arcus) {
            addArcusClientEditor(context, config);
            addCollectionFutureEditor(context);
        }
        
        if (arcus || memcached) {
            addBaseOperationImplEditor(context);        
            addCacheManagerEditor(context);
            
            addGetFutureEditor(context);
            // TODO ImmedateFuture doesn't have setOperation(Operation) method.
//            editors.add(getImmediateFutureEditor(context));
            addOperationFutureEditor(context);
            
            addFrontCacheGetFutureEditor(context);
            addFrontCacheMemcachedClientEditor(context, config);
            addMemcachedClientEditor(context, config);
        }
    }
    
    private void addArcusClientEditor(ProfilerPluginSetupContext context, ArcusPluginConfig config) {
        boolean traceKey = config.isArcusKeyTrace();
        
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.ArcusClient");
        builder.condition(new ClassCondition() {
                @Override
                public boolean check(ClassLoader classLoader, InstrumentClass target) {
                    return target.hasMethod("addOp", new String[] {"java.lang.String", "net.spy.memcached.ops.Operation"}, "net.spy.memcached.ops.Operation");
                }
        });
        
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor");
        
        MethodEditorBuilder mb2 = builder.editMethods(new ArcusMethodFilter());
        mb2.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
    }
    
    private void addCacheManagerEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.CacheManager");
        builder.injectMetadata(METADATA_SERVICE_CODE);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor");
    }

    
    private void addBaseOperationImplEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.protocol.BaseOperationImpl");
        builder.injectMetadata(METADATA_SERVICE_CODE);
    }
    
    private void addFrontCacheGetFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.plugin.FrontCacheGetFuture");
        builder.injectMetadata(MEATDATA_CACHE_NAME);
        builder.injectMetadata(METADATA_CACHE_KEY);
        
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor");
        
        MethodEditorBuilder mb2 = builder.editMethod("get", "long", "java.util.concurrent.TimeUnit");
        mb2.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");
        
        MethodEditorBuilder mb3 = builder.editMethod("get");
        mb3.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");
    }
    
    private void addFrontCacheMemcachedClientEditor(ProfilerPluginSetupContext context, ArcusPluginConfig config) {
        boolean traceKey = config.isMemcachedKeyTrace();
        
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.plugin.FrontCacheMemcachedClient");
        builder.condition(new ClassCondition() {

            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.hasDeclaredMethod("putFrontCache", new String[] { "java.lang.String", "java.util.concurrent.Future", "long" });
            }
            
        });
        
        MethodEditorBuilder mb = builder.editMethods(new FrontCacheMemcachedMethodFilter());
        mb.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
    }


    private void addMemcachedClientEditor(ProfilerPluginSetupContext context, ArcusPluginConfig config) {
        boolean traceKey = config.isMemcachedKeyTrace();
        
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.MemcachedClient");
        builder.condition(new ClassCondition() {

            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.hasDeclaredMethod("addOp", new String[] { "java.lang.String", "net.spy.memcached.ops.Operation" });
            }
            
        });
        
        builder.injectMetadata(METADATA_SERVICE_CODE);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.AddOpInterceptor");
        
        MethodEditorBuilder mb2 = builder.editMethods(new MemcachedMethodFilter());
        mb2.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
    }

    private void getFutureEditor(ProfilerPluginSetupContext context, ClassEditorBuilder builder) {
        builder.injectMetadata(ArcusConstants.METADATA_OPERATION);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor");
    }
        
    private void addCollectionFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.internal.CollectionFuture");
        getFutureEditor(context, builder);
    }
    
    private void addGetFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.internal.GetFuture");
        getFutureEditor(context, builder);
    }

    private void addOperationFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.internal.OperationFuture");
        getFutureEditor(context, builder);
    }

    private void getImmediateFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("net.spy.memcached.internal.ImmediateFuture");
        getFutureEditor(context, builder);
    }

}