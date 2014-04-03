package com.hightail.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.gson.Gson;
import com.hightail.metrics.utils.Agent;
import com.hightail.metrics.utils.Component;
import com.hightail.metrics.utils.NewRelicObj;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by hightail on 4/3/14.
 */
public class NewRelicReporterTest {

    @Test
    public void testNewRelicReporter() throws Exception {

        MetricRegistry registry = new MetricRegistry();
        NewRelic newRelic = new NewRelic("http://platform-api.newrelic.com/platform/v1/metrics",
                "d59fa2b038b0496d00018c6ad63187350bb76a3c",
                "test-metrics",
                "com.test.metrics.hightail",
                60);

        NewRelicReporter newRelicReporter = NewRelicReporter.forRegistry(registry).build(newRelic);
        newRelicReporter.start(10, TimeUnit.SECONDS);

        doSomething(registry);
    }

    private void doSomething(MetricRegistry registry) {

        while(true) {

            Timer timer = registry.timer("Test/sample.code.to.timer");
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
            Map<String, Float> componentMetrics = new HashMap<String, Float>();

            componentMetrics.put("max", 0.1f);
            componentMetrics.put("mean", 0.5f);
            componentMetrics.put("min", 0.01f);
            componentMetrics.put("stddev", 0.25f);
            componentMetrics.put("p50", 0.5f);
            componentMetrics.put("p75", 0.75f);
            componentMetrics.put("p95", 0.95f);
            componentMetrics.put("p98", 0.98f);
            componentMetrics.put("p99", 0.99f);
            componentMetrics.put("p999", 0.999f);
            Agent agent = new Agent("application.metrics.hightail",1,"1.0");
            List<Component> components = new ArrayList<Component>();
            components.add(new Component("componentName", "com.metrics.hightail.dummy", 60, componentMetrics));

            NewRelicObj newRelicObj = new NewRelicObj(agent, components);

            Gson gson = new Gson();
            System.out.println(gson.toJson(newRelicObj));
        }

    }
