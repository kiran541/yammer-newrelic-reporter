package com.hightail.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.hightail.metrics.constants.NewRelicConstants;
import com.hightail.metrics.constants.ReporterType;
import com.hightail.metrics.reporter.NewRelicReporter;
import com.hightail.metrics.reporter.NewRelicReporterFactory;
import org.junit.Test;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by hightail on 4/6/14.
 */
public class NewRelicAgentReporterTest {

    @Test
    public void test() throws Exception {

        MetricRegistry registry = new MetricRegistry();

        Properties properties = new Properties();
        properties.put(NewRelicConstants.METRIC_REGISTRY,registry);

        NewRelicReporter reporter = NewRelicReporterFactory.getNewRelicReporter(ReporterType.AGENT, properties);
        reporter.start(10, TimeUnit.SECONDS);

        doSomething(registry);
    }

    private void doSomething(MetricRegistry registry) {

        while(true) {

            Timer timer = registry.timer("sample.code.to.timer");
            Timer.Context context = timer.time();

            Random random = new Random();
            int sleepTime = random.nextInt(100);
            try {
                Thread.sleep(1+sleepTime);
            }
            catch (Exception ex) {

            } finally {
                context.stop();
            }
        }
    }

}

