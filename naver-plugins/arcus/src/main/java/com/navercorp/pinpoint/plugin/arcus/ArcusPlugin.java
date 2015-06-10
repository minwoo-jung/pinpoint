package com.navercorp.pinpoint.plugin.arcus;

import static com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassConditions.*;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.BaseClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;
import com.navercorp.pinpoint.plugin.arcus.filter.ArcusMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.FrontCacheMemcachedMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.MemcachedMethodFilter;

public class ArcusPlugin implements ProfilerPlugin, ArcusConstants {

    @Override
    public void setup(ProfilerPluginContext context) {
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
            // editors.add(getImmediateFutureEditor(context));
            addOperationFutureEditor(context);

            addFrontCacheGetFutureEditor(context);
            addFrontCacheMemcachedClientEditor(context, config);
            addMemcachedClientEditor(context, config);
        }
    }

    private void addArcusClientEditor(ProfilerPluginContext context, final ArcusPluginConfig config) {

        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.ArcusClient");

        builder.conditional(hasMethod("addOp", "net.spy.memcached.ops.Operation", "java.lang.String", "net.spy.memcached.ops.Operation"),
                new ConditionalClassFileTransformerSetup() {
            
                    @Override
                    public void setup(ConditionalClassFileTransformerBuilder conditional) {
                        boolean traceKey = config.isArcusKeyTrace();

                        conditional.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor");
        
                        MethodTransformerBuilder mb = conditional.editMethods(new ArcusMethodFilter());
                        mb.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
                    }
                }
        );

        context.addClassFileTransformer(builder.build());
    }

    private void addCacheManagerEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.CacheManager");
        builder.injectMetadata(METADATA_SERVICE_CODE);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor");

        context.addClassFileTransformer(builder.build());
    }

    private void addBaseOperationImplEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.protocol.BaseOperationImpl");
        builder.injectMetadata(METADATA_SERVICE_CODE);

        context.addClassFileTransformer(builder.build());
    }

    private void addFrontCacheGetFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.plugin.FrontCacheGetFuture");
        builder.injectMetadata(MEATDATA_CACHE_NAME);
        builder.injectMetadata(METADATA_CACHE_KEY);

        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor");

        MethodTransformerBuilder mb2 = builder.editMethod("get", "long", "java.util.concurrent.TimeUnit");
        mb2.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");

        MethodTransformerBuilder mb3 = builder.editMethod("get");
        mb3.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");

        context.addClassFileTransformer(builder.build());
    }

    private void addFrontCacheMemcachedClientEditor(ProfilerPluginContext context, final ArcusPluginConfig config) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.plugin.FrontCacheMemcachedClient");

        builder.conditional(hasDeclaredMethod("putFrontCache", "java.lang.String", "java.util.concurrent.Future", "long"),
                new ConditionalClassFileTransformerSetup() {
                    
                    @Override
                    public void setup(ConditionalClassFileTransformerBuilder conditional) {
                        boolean traceKey = config.isMemcachedKeyTrace();
                        MethodTransformerBuilder mb = conditional.editMethods(new FrontCacheMemcachedMethodFilter());
                        mb.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
                    }
                }
        );

        context.addClassFileTransformer(builder.build());
    }

    private void addMemcachedClientEditor(ProfilerPluginContext context, final ArcusPluginConfig config) {

        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.MemcachedClient");
        builder.conditional(hasDeclaredMethod("addOp", "java.lang.String", "net.spy.memcached.ops.Operation"),
                new ConditionalClassFileTransformerSetup() {
                    
                    @Override
                    public void setup(ConditionalClassFileTransformerBuilder conditional) {
                        boolean traceKey = config.isMemcachedKeyTrace();

                        conditional.injectMetadata(METADATA_SERVICE_CODE);
                        conditional.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.AddOpInterceptor");
                        
                        MethodTransformerBuilder mb2 = conditional.editMethods(new MemcachedMethodFilter());
                        mb2.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
                    }
                }
        );


        context.addClassFileTransformer(builder.build());
    }

    private void getFutureEditor(ProfilerPluginContext context, BaseClassFileTransformerBuilder builder) {
        builder.injectMetadata(ArcusConstants.METADATA_OPERATION);
        builder.injectMetadata(ArcusConstants.METADATA_ASYNC_TRACE_ID);
        
        MethodTransformerBuilder setOperationMethodBuilder = builder.editMethod("setOperation", "net.spy.memcached.ops.Operation");
        setOperationMethodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor");
        
        MethodTransformerBuilder methodBuilder = builder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                final String name = method.getName();
                return !(name.equals("cancel") || name.equals("get") || name.equals("set"));
            }
        });
        methodBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor");
    }

    private void addCollectionFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.CollectionFuture");
        getFutureEditor(context, builder);

        context.addClassFileTransformer(builder.build());
    }

    private void addGetFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.GetFuture");
        getFutureEditor(context, builder);

        context.addClassFileTransformer(builder.build());
    }

    private void addOperationFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.OperationFuture");
        getFutureEditor(context, builder);

        context.addClassFileTransformer(builder.build());
    }

    private void getImmediateFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.ImmediateFuture");
        getFutureEditor(context, builder);

        context.addClassFileTransformer(builder.build());
    }

}