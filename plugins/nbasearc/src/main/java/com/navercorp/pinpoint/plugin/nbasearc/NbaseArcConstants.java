package com.navercorp.pinpoint.plugin.nbasearc;

import static com.navercorp.pinpoint.common.HistogramSchema.FAST_SCHEMA;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.INCLUDE_DESTINATION_ID;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.TERMINAL;

import com.navercorp.pinpoint.common.ServiceType;

public interface NbaseArcConstants {
    public static final ServiceType NBASE_ARC = ServiceType.of(8250, "NBASE_ARC", FAST_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final String METADATA_END_POINT = "endPoint";
    public static final String METADATA_DESTINATION_ID = "destinationId";
}
