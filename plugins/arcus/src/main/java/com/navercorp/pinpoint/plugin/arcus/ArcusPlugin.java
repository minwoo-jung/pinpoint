package com.navercorp.pinpoint.plugin.arcus;

import static com.navercorp.pinpoint.bootstrap.plugin.editor.ClassConditions.*;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.editor.BaseClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConditionalClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConditionalClassEditorSetup;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.plugin.arcus.filter.ArcusMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.FrontCacheMemcachedMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.MemcachedMethodFilter;

public class ArcusPlugin implements ProfilerPlugin, ArcusConstants {

    @Override
    public void setup(ProfilerPluginSetupContext context) {
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

    private void addArcusClientEditor(ProfilerPluginSetupContext context, final ArcusPluginConfig config) {

        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.ArcusClient");

        builder.conditional(hasMethod("addOp", "net.spy.memcached.ops.Operation", "java.lang.String", "net.spy.memcached.ops.Operation"),
                new ConditionalClassEditorSetup() {
            
                    @Override
                    public void setup(ConditionalClassEditorBuilder conditional) {
                        boolean traceKey = config.isArcusKeyTrace();

                        conditional.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor");
        
                        MethodEditorBuilder mb = conditional.editMethods(new ArcusMethodFilter());
                        mb.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
                    }
                }
        );

        context.addClassEditor(builder.build());
    }

    private void addCacheManagerEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.CacheManager");
        builder.injectMetadata(METADATA_SERVICE_CODE);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor");

        context.addClassEditor(builder.build());
    }

    private void addBaseOperationImplEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.protocol.BaseOperationImpl");
        builder.injectMetadata(METADATA_SERVICE_CODE);

        context.addClassEditor(builder.build());
    }

    private void addFrontCacheGetFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.plugin.FrontCacheGetFuture");
        builder.injectMetadata(MEATDATA_CACHE_NAME);
        builder.injectMetadata(METADATA_CACHE_KEY);

        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor");

        MethodEditorBuilder mb2 = builder.editMethod("get", "long", "java.util.concurrent.TimeUnit");
        mb2.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");

        MethodEditorBuilder mb3 = builder.editMethod("get");
        mb3.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");

        context.addClassEditor(builder.build());
    }

    private void addFrontCacheMemcachedClientEditor(ProfilerPluginSetupContext context, final ArcusPluginConfig config) {
        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.plugin.FrontCacheMemcachedClient");

        builder.conditional(hasDeclaredMethod("putFrontCache", "java.lang.String", "java.util.concurrent.Future", "long"),
                new ConditionalClassEditorSetup() {
                    
                    @Override
                    public void setup(ConditionalClassEditorBuilder conditional) {
                        boolean traceKey = config.isMemcachedKeyTrace();
                        MethodEditorBuilder mb = conditional.editMethods(new FrontCacheMemcachedMethodFilter());
                        mb.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
                    }
                }
        );

        context.addClassEditor(builder.build());
    }

    private void addMemcachedClientEditor(ProfilerPluginSetupContext context, final ArcusPluginConfig config) {

        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.MemcachedClient");
        builder.conditional(hasDeclaredMethod("addOp", "java.lang.String", "net.spy.memcached.ops.Operation"),
                new ConditionalClassEditorSetup() {
                    
                    @Override
                    public void setup(ConditionalClassEditorBuilder conditional) {
                        boolean traceKey = config.isMemcachedKeyTrace();

                        conditional.injectMetadata(METADATA_SERVICE_CODE);
                        conditional.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.AddOpInterceptor");
                        
                        MethodEditorBuilder mb2 = conditional.editMethods(new MemcachedMethodFilter());
                        mb2.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
                    }
                }
        );


        context.addClassEditor(builder.build());
    }

    private void getFutureEditor(ProfilerPluginSetupContext context, BaseClassEditorBuilder builder) {
        builder.injectMetadata(ArcusConstants.METADATA_OPERATION);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor");
    }

    private void addCollectionFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.internal.CollectionFuture");
        getFutureEditor(context, builder);

        context.addClassEditor(builder.build());
    }

    private void addGetFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.internal.GetFuture");
        getFutureEditor(context, builder);

        context.addClassEditor(builder.build());
    }

    private void addOperationFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.internal.OperationFuture");
        getFutureEditor(context, builder);

        context.addClassEditor(builder.build());
    }

    private void getImmediateFutureEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.getClassEditorBuilder("net.spy.memcached.internal.ImmediateFuture");
        getFutureEditor(context, builder);

        context.addClassEditor(builder.build());
    }

}