package com.hightail.metrics;

import com.codahale.metrics.MetricRegistry;
import com.hightail.metrics.constants.NewRelicConstants;
import com.hightail.metrics.constants.ReporterType;
import com.hightail.metrics.reporter.NewRelicReporter;
import com.hightail.metrics.reporter.NewRelicReporterFactory;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by hightail on 4/3/14.
 */
public class NewRelicHTTPv1ReporterTest {

    @Test
    public void testNewRelicReporter() throws Exception {
        MetricRegistry registry = new MetricRegistry();

        Properties properties = new Properties();
        properties.put(NewRelicConstants.LICENSE_KEY, "d59fa2b038b0496d00018c6ad63187350bb76a3c");
        properties.put(NewRelicConstants.COMPONENT_NAME, "Kaspersky-Metrics");
        properties.put(NewRelicConstants.APP_ID, "com.hightail-custom-metrics");
        properties.put(NewRelicConstants.METRIC_REGISTRY, registry);
        properties.put(NewRelicConstants.PREFIX, "CustomHTTPMetrics/billing-service01.itl.hightail.com");

        NewRelicReporter newRelicHTTPv1Reporter = NewRelicReporterFactory.getNewRelicReporter(ReporterType.HTTPv1, properties);
        newRelicHTTPv1Reporter.start(10, TimeUnit.SECONDS);

        MockFileScanner.scanFiles(registry);
    }

    @Test
    public void testNewRelicReporter2() throws Exception {
        MetricRegistry registry = new MetricRegistry();

        Properties properties = new Properties();
        properties.put(NewRelicConstants.LICENSE_KEY, "d59fa2b038b0496d00018c6ad63187350bb76a3c");
        properties.put(NewRelicConstants.COMPONENT_NAME, "Kaspersky-Metrics");
        properties.put(NewRelicConstants.APP_ID, "com.hightail-custom-metrics");
        properties.put(NewRelicConstants.METRIC_REGISTRY, registry);
        properties.put(NewRelicConstants.PREFIX, "CustomHTTPMetrics/billing-service02.itl.hightail.com");

        NewRelicReporter newRelicHTTPv1Reporter = NewRelicReporterFactory.getNewRelicReporter(ReporterType.HTTPv1, properties);
        newRelicHTTPv1Reporter.start(10, TimeUnit.SECONDS);

        MockFileScanner.scanFiles(registry);
    }
}
