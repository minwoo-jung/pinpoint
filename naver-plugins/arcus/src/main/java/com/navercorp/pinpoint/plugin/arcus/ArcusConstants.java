package com.navercorp.pinpoint.plugin.arcus;

import static com.navercorp.pinpoint.common.AnnotationKeyMatcher.*;
import static com.navercorp.pinpoint.common.HistogramSchema.*;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.ServiceType;



public interface ArcusConstants {
    public static final ServiceType ARCUS = ServiceType.of(8100, "ARCUS", FAST_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_FUTURE_GET = ServiceType.of(8101, "ARCUS_FUTURE_GET", "ARCUS", FAST_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_EHCACHE_FUTURE_GET = ServiceType.of(8102, "ARCUS_EHCACHE_FUTURE_GET", "ARCUS-EHCACHE", FAST_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);

    public static final String ARCUS_SCOPE = "ArcusScope";
    public static final String ATTRIBUTE_CONFIG = "arcusPluginConfig";
    public static final String METADATA_SERVICE_CODE = "serviceCode";
    public static final String MEATDATA_CACHE_NAME = "cacheName";
    public static final String METADATA_CACHE_KEY = "cacheKey";
    public static final String METADATA_OPERATION = "operation";
}
