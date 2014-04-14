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
 * Created by hightail on 4/6/14.
 */
public class NewRelicAgentReporterTest {

    @Test
    public void testNewRelicReporter() throws Exception {

        MetricRegistry registry = new MetricRegistry();

        Properties properties = new Properties();
        properties.put(NewRelicConstants.METRIC_REGISTRY,registry);
        properties.put(NewRelicConstants.PREFIX,"CustomAgentMetrics");

        NewRelicReporter reporter = NewRelicReporterFactory.getNewRelicReporter(ReporterType.AGENT, properties);
        reporter.start(10, TimeUnit.SECONDS);

        MockFileScanner.scanFiles(registry);
    }

}

