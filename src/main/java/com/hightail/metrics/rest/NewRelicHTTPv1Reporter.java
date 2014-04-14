package com.hightail.metrics.rest;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.hightail.metrics.constants.NewRelicConstants;
import com.hightail.metrics.reporter.NewRelicReporter;
import com.newrelic.metrics.publish.binding.ComponentData;
import com.newrelic.metrics.publish.binding.Context;
import com.newrelic.metrics.publish.binding.Request;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The New Relic Reporter, which reports the data to New Relic via REST calls
 *
 */
public class NewRelicHTTPv1Reporter extends NewRelicReporter {

    private static final Logger logger = Logger.getLogger(NewRelicHTTPv1Reporter.class);

    private NewRelic newRelic;
    private String metricNamePrefix;
    private String hostname;

    /**
     * Creates a new {@link com.codahale.metrics.ScheduledReporter} instance.
     *
     * @param registry     the {@link com.codahale.metrics.MetricRegistry} containing the definedMetrics this
     *                     reporter will report
     * @param metricNamePrefix       the reporter's name
     * @param filter       the filter for which definedMetrics to report
     * @param rateUnit
     * @param durationUnit
     */
    protected NewRelicHTTPv1Reporter(NewRelic newRelic, MetricRegistry registry, String metricNamePrefix,
                                     MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, "new-relic-http-reporter", filter, rateUnit, durationUnit);
        this.newRelic = newRelic;
        this.metricNamePrefix = metricNamePrefix;

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhex) {
            logger.warn("This can be ignored: Agent hostId Error: ", uhex);
            hostname = NewRelicConstants.DEFAULT_AGENT_HOST;
        }

        logger.info("NewRelicHTTPv1Reporter initialized..");
    }


    /**
     * Returns a new {@link Builder} for {@link NewRelicHTTPv1Reporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link NewRelicHTTPv1Reporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class Builder {
        private final MetricRegistry registry;
        private String metricNamePrefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.metricNamePrefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        /**
         * Prefix all metric names with the given string.
         *
         * @param prefix the metricNamePrefix for all metric names
         * @return {@code this}
         */
        public Builder prefixedWith(String prefix) {
            this.metricNamePrefix = prefix;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report definedMetrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link NewRelicHTTPv1Reporter} with the given properties, sending definedMetrics using the
         * given {@link NewRelic} client.
         *
         * @param newRelic a {@link NewRelic} client
         * @return a {@link NewRelicHTTPv1Reporter}
         */
        public NewRelicHTTPv1Reporter build(NewRelic newRelic) {
            return new NewRelicHTTPv1Reporter(newRelic,
                    registry,
                    metricNamePrefix,
                    filter,
                    rateUnit,
                    durationUnit
            );
        }
    }


    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

        try {

            for (Map.Entry<String, Gauge> gaugeEntry : gauges.entrySet()) {
                doGauge(gaugeEntry.getKey(), gaugeEntry.getValue());
            }

            for (Map.Entry<String, Counter> counterEntry : counters.entrySet()) {
                String name = counterEntry.getKey();
                Counter counter = counterEntry.getValue();
                Map<String, Object> componentMetrics = new HashMap<String, Object>();

                componentMetrics.put(prefix(name) + "/count", counter.getCount());
                postToNewRelic(componentMetrics);
            }

            for (Map.Entry<String, Histogram> histogramEntry : histograms.entrySet()) {
                String name = histogramEntry.getKey();
                Snapshot snapshot = histogramEntry.getValue().getSnapshot();

                Histogram metric = histogramEntry.getValue();
                doHistogramSnapshot(name, snapshot, metric);
            }

            for (Map.Entry<String, Meter> meterEntry : meters.entrySet()) {
                String name = meterEntry.getKey();
                Meter meter = meterEntry.getValue();
                doMetered(name, meter);
            }

            for (Map.Entry<String, Timer> timerEntry : timers.entrySet()) {
                Timer timer = timerEntry.getValue();
                String name = timerEntry.getKey();
                Snapshot snapshot = timer.getSnapshot();

                doTimerMetered(timer, name);
                doTimerSnapshot(timer, name, snapshot);
            }
        }catch(Exception ex) {
            logger.error("Could not push metrics to NewRelic via HTTP : ", ex);
        }
    }

    private void doMetered(String name, Meter meter) {
        Map<String, Object> componentMetrics = new HashMap<String, Object>();

        componentMetrics.put(prefix(name) + "/count", meter.getCount());
        componentMetrics.put(prefix(name) + "/meanRate/" + getRateUnit(), (float) convertRate(meter.getMeanRate()));
        componentMetrics.put(prefix(name) + "/1MinuteRate/" + getRateUnit(), (float) convertRate(meter.getOneMinuteRate()));
        componentMetrics.put(prefix(name) + "/5MinuteRate/" + getRateUnit(), (float) convertRate(meter.getFiveMinuteRate()));
        componentMetrics.put(prefix(name) + "/15MinuteRate/" + getRateUnit(), (float) convertRate(meter.getFifteenMinuteRate()));
        postToNewRelic(componentMetrics);

    }

    private void doTimerMetered(Timer timer, String name) {
        Map<String, Object> componentMetrics = new HashMap<String, Object>();

        componentMetrics.put(prefix(name) + "/count", timer.getCount());
        componentMetrics.put(prefix(name) + "/meanRate/" + getRateUnit(), (float) convertRate(timer.getMeanRate()));
        componentMetrics.put(prefix(name) + "/1MinuteRate/" + getRateUnit(), (float) convertRate(timer.getOneMinuteRate()));
        componentMetrics.put(prefix(name) + "/5MinuteRate/" + getRateUnit(), (float) convertRate(timer.getFiveMinuteRate()));
        componentMetrics.put(prefix(name) + "/15MinuteRate/" + getRateUnit(), (float) convertRate(timer.getFifteenMinuteRate()));
        postToNewRelic(componentMetrics);

    }

    private void doHistogramSnapshot(String name, Snapshot snapshot, Histogram histogram) {
        Map<String, Object> componentMetrics = new HashMap<String, Object>();

        componentMetrics.put(prefix(name) + "/min" , (float) convertDuration(snapshot.getMin()));
        componentMetrics.put(prefix(name) + "/max" , (float) convertDuration(snapshot.getMax()));
        componentMetrics.put(prefix(name) + "/mean" , (float) convertDuration(snapshot.getMean()));
        componentMetrics.put(prefix(name) + "/stdDev" , (float) convertDuration(snapshot.getStdDev()));
        componentMetrics.put(prefix(name) + "/median" , (float) convertDuration(snapshot.getMedian()));
        componentMetrics.put(prefix(name) + "/75th" , (float) convertDuration(snapshot.get75thPercentile()));
        componentMetrics.put(prefix(name) + "/95th" , (float) convertDuration(snapshot.get95thPercentile()));
        componentMetrics.put(prefix(name) + "/98th" , (float) convertDuration(snapshot.get98thPercentile()));
        componentMetrics.put(prefix(name) + "/99th" , (float) convertDuration(snapshot.get99thPercentile()));
        componentMetrics.put(prefix(name) + "/99.9th" , (float) convertDuration(snapshot.get999thPercentile()));
        postToNewRelic(componentMetrics);

    }

    private void doTimerSnapshot(Timer timer, String name, Snapshot snapshot) {
        Map<String, Object> componentMetrics = new HashMap<String, Object>();

        String nameSuffix = "/" + getDurationUnit();

        componentMetrics.put(prefix(name) + "/min" + nameSuffix, (float) convertDuration(snapshot.getMin()));
        componentMetrics.put(prefix(name) + "/max" + nameSuffix, (float) convertDuration(snapshot.getMax()));
        componentMetrics.put(prefix(name) + "/mean" + nameSuffix, (float) convertDuration(snapshot.getMean()));
        componentMetrics.put(prefix(name) + "/stdDev" + nameSuffix, (float) convertDuration(snapshot.getStdDev()));
        componentMetrics.put(prefix(name) + "/median" + nameSuffix, (float) convertDuration(snapshot.getMedian()));
        componentMetrics.put(prefix(name) + "/75th" + nameSuffix, (float) convertDuration(snapshot.get75thPercentile()));
        componentMetrics.put(prefix(name) + "/95th" + nameSuffix, (float) convertDuration(snapshot.get95thPercentile()));
        componentMetrics.put(prefix(name) + "/98th" + nameSuffix, (float) convertDuration(snapshot.get98thPercentile()));
        componentMetrics.put(prefix(name) + "/99th" + nameSuffix, (float) convertDuration(snapshot.get99thPercentile()));
        componentMetrics.put(prefix(name) + "/99.9th" + nameSuffix, (float) convertDuration(snapshot.get999thPercentile()));
        postToNewRelic(componentMetrics);

    }

    private void doGauge(String name, Gauge gauge) {
        Object gaugeValue = gauge.getValue();
        Map<String, Object> componentMetrics = new HashMap<String, Object>();

        if (gaugeValue instanceof Number) {
            float n = ((Number) gaugeValue).floatValue();
            if (!Float.isNaN(n) && !Float.isInfinite(n)) {
                componentMetrics.put(prefix(name)+"/gauge", n);
                postToNewRelic(componentMetrics);
            }
        }
    }

    private void postToNewRelic(Map<String, Object> componentMetrics) {

        Context context = new Context();
        context.licenseKey = newRelic.getLicenseKey();
        context.agentData.host = hostname;
        context.agentData.pid = NewRelicConstants.DEFAULT_AGENT_PID;
        context.agentData.version = NewRelicConstants.DEFAULT_AGENT_VERSION;

        ComponentData componentData = context.createComponent();
        componentData.guid = newRelic.getAppId();
        componentData.name = newRelic.getComponentName();

        Request request = new Request(context);


        for(Map.Entry<String, Object> metric: componentMetrics.entrySet()) {
            if(metric.getValue() instanceof Number) {
                request.addMetric(componentData, metric.getKey(), (Number) metric.getValue());
            }
        }

        request.deliver();
    }

    private String prefix(String name) {
        StringBuffer stringBuffer = new StringBuffer();
        if(StringUtils.isNotBlank(metricNamePrefix)) {
            stringBuffer.append(metricNamePrefix).append("/");
        }
        stringBuffer.append(name);
        return stringBuffer.toString();
    }

}
