package com.hightail.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.gson.Gson;
import com.hightail.metrics.constants.NewRelicConstants;
import com.hightail.metrics.constants.ReporterType;
import com.hightail.metrics.reporter.NewRelicReporter;
import com.hightail.metrics.reporter.NewRelicReporterFactory;
import com.hightail.metrics.rest.*;
import org.junit.Test;

import java.util.*;
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
        properties.put(NewRelicConstants.COMPONENT_NAME, "Billing-Metrics");
        properties.put(NewRelicConstants.APP_ID, "com.hightail-custom-metrics");
        properties.put(NewRelicConstants.DURATION, "10");
        properties.put(NewRelicConstants.METRIC_REGISTRY, registry);
        properties.put(NewRelicConstants.PREFIX, "Test-Metrics");

        NewRelicReporter newRelicHTTPv1Reporter = NewRelicReporterFactory.getNewRelicReporter(ReporterType.HTTPv1, properties);
        newRelicHTTPv1Reporter.start(10, TimeUnit.SECONDS);

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



        @Test
        public void printJson() throws Exception {

            Map<String,  Map<String, Float>> componentMetrics = new HashMap<String,  Map<String, Float>>();

            Map<String, Float> metrics = new HashMap<String, Float>();
            metrics.put("max", 10f);
            metrics.put("min", 2f);
            metrics.put("total", 12f);
            metrics.put("count", 2f);

            componentMetrics.put("NewRelicAgentReporterTest/Metrics/Billing", metrics);

            Agent agent = new Agent("application.metrics.hightail",1,"1.0");
            List<Component> components = new ArrayList<Component>();
            components.add(new Component("componentName", "com.metrics.hightail.dummy", 60, componentMetrics));

            PayLoad payLoad = new PayLoad(agent, components);

            Gson gson = new Gson();
            System.out.println(gson.toJson(payLoad));
        }

    }
