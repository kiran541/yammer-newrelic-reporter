package com.hightail.metrics.constants;

import com.codahale.metrics.MetricFilter;

import java.util.concurrent.TimeUnit;

/**
 * Created by hightail on 4/3/14.
 */
public class NewRelicConstants {

    //Default values
    public static final String DEFAULT_URL = "http://platform-api.newrelic.com/platform/v1/metrics";
    public static final int DEFAULT_AGENT_PID = 1;
    public static final String DEFAULT_AGENT_VERSION = "1.0.0";
    public static final String DEFAULT_AGENT_HOST = "localhost";
    public static final TimeUnit DEFAULT_RATE_UNIT = TimeUnit.SECONDS;
    public static final TimeUnit DEFAULT_DURATION_UNIT = TimeUnit.MILLISECONDS;
    public static final MetricFilter DEFAULT_METRIC_FILTER = MetricFilter.ALL;
    public static final String DEFAULT_PREFIX = null;

    //These can be used for defining Property Keys
    public static final String LICENSE_KEY = "LICENSE_KEY";
    public static final String COMPONENT_NAME = "COMPONENT_NAME";
    public static final String APP_ID = "APP_ID";
    public static final String DURATION = "DURATION";
    public static final String METRIC_REGISTRY = "METRIC_REGISTRY";
    public static final String PREFIX = "PREFIX";
    public static final String RATE_UNIT = "RATE_UNIT";
    public static final String DURATION_UNIT = "DURATION_UNIT";
    public static final String METRIC_FILTER = "METRIC_FILTER";

}
