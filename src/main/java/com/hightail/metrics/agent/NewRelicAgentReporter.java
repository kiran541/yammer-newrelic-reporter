package com.hightail.metrics.agent;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.hightail.metrics.reporter.NewRelicReporter;
import com.newrelic.api.agent.NewRelic;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter for Metrics that writes to New Relic as "custom metrics".
 *
 * New Relic wants to keep custom metrics to a total of about 2000, but 2000 custom metrics can easily be reached when
 * every {@link com.codahale.metrics.Timer} can produce 15 New Relic metrics. See https://docs.newrelic.com/docs/features/custom-metric-collection
 * for more.
 *
 * To keep the number of custom metrics under control, provide appropriate implementations of {@link MetricFilter} and
 * {@link MetricAttributeFilter}.
 */
public final class NewRelicAgentReporter extends NewRelicReporter {

    private static final Logger logger = Logger.getLogger(NewRelicAgentReporter.class);


    private final String metricNamePrefix;

    /**
     * @param registry         metric registry to get metrics from
     * @param name             reporter name
     * @param filter           metric filter
     * @param attributeFilter  metric attribute filter
     * @param rateUnit         unit for reporting rates
     * @param durationUnit     unit for reporting durations
     * @param metricNamePrefix metricNamePrefix before the metric name used when naming New Relic metrics. Use "" if no metricNamePrefix is
     *                         needed.
     * @see ScheduledReporter#ScheduledReporter(MetricRegistry, String, MetricFilter, TimeUnit, TimeUnit)
     */
    public NewRelicAgentReporter(MetricRegistry registry, String name, MetricFilter filter,
                                 TimeUnit rateUnit, TimeUnit durationUnit, String metricNamePrefix) {
        super(registry, name, filter, rateUnit, durationUnit);
        this.metricNamePrefix = metricNamePrefix;
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
         * given {@link com.hightail.metrics.rest.NewRelic} client.
         *
         * @param newRelic a {@link com.hightail.metrics.rest.NewRelic} client
         * @return a {@link NewRelicHTTPv1Reporter}
         */
        public NewRelicAgentReporter build() {
            return new NewRelicAgentReporter(
                    registry,
                    "new-relic-agent-reporter",
                    filter,
                    rateUnit,
                    durationUnit, metricNamePrefix);
        }
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {

        try {
            for (Map.Entry<String, Gauge> gaugeEntry : gauges.entrySet()) {
                doGauge(gaugeEntry.getKey(), gaugeEntry.getValue());
            }

            for (Map.Entry<String, Counter> counterEntry : counters.entrySet()) {
                String name = counterEntry.getKey();
                Counter counter = counterEntry.getValue();
                record(name + "/count", counter.getCount());
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
            logger.error("Could not push metrics to NewRelic via Agent: ", ex);
        }
    }

    private void doMetered(String name, Meter meter) {
        record(name + "/count", meter.getCount());
        record(name + "/meanRate/" + getRateUnit(), (float) convertRate(meter.getMeanRate()));
        record(name + "/1MinuteRate/" + getRateUnit(), (float) convertRate(meter.getOneMinuteRate()));
        record(name + "/5MinuteRate/" + getRateUnit(), (float) convertRate(meter.getFiveMinuteRate()));
        record(name + "/15MinuteRate/" + getRateUnit(), (float) convertRate(meter.getFifteenMinuteRate()));
    }

    private void doTimerMetered(Timer timer, String name) {
        record(name + "/count", timer.getCount());
        record(name + "/meanRate/" + getRateUnit(), (float) convertRate(timer.getMeanRate()));
        record(name + "/1MinuteRate/" + getRateUnit(), (float) convertRate(timer.getOneMinuteRate()));
        record(name + "/5MinuteRate/" + getRateUnit(), (float) convertRate(timer.getFiveMinuteRate()));
        record(name + "/15MinuteRate/" + getRateUnit(), (float) convertRate(timer.getFifteenMinuteRate()));
    }

    private void doHistogramSnapshot(String name, Snapshot snapshot, Histogram metric) {
        record(name + "/min" , (float) convertDuration(snapshot.getMin()));
        record(name + "/max" , (float) convertDuration(snapshot.getMax()));
        record(name + "/mean" , (float) convertDuration(snapshot.getMean()));
        record(name + "/stdDev" , (float) convertDuration(snapshot.getStdDev()));
        record(name + "/median" , (float) convertDuration(snapshot.getMedian()));
        record(name + "/75th" , (float) convertDuration(snapshot.get75thPercentile()));
        record(name + "/95th" , (float) convertDuration(snapshot.get95thPercentile()));
        record(name + "/98th" , (float) convertDuration(snapshot.get98thPercentile()));
        record(name + "/99th" , (float) convertDuration(snapshot.get99thPercentile()));
        record(name + "/99.9th" , (float) convertDuration(snapshot.get999thPercentile()));
    }

    private void doTimerSnapshot(Timer timer, String name, Snapshot snapshot) {
        String nameSuffix = "/" + getDurationUnit();

        record(name + "/min" + nameSuffix, (float) convertDuration(snapshot.getMin()));
        record(name + "/max" + nameSuffix, (float) convertDuration(snapshot.getMax()));
        record(name + "/mean" + nameSuffix, (float) convertDuration(snapshot.getMean()));
        record(name + "/stdDev" + nameSuffix, (float) convertDuration(snapshot.getStdDev()));
        record(name + "/median" + nameSuffix, (float) convertDuration(snapshot.getMedian()));
        record(name + "/75th" + nameSuffix, (float) convertDuration(snapshot.get75thPercentile()));
        record(name + "/95th" + nameSuffix, (float) convertDuration(snapshot.get95thPercentile()));
        record(name + "/98th" + nameSuffix, (float) convertDuration(snapshot.get98thPercentile()));
        record(name + "/99th" + nameSuffix, (float) convertDuration(snapshot.get99thPercentile()));
        record(name + "/99.9th" + nameSuffix, (float) convertDuration(snapshot.get999thPercentile()));
    }

    private void doGauge(String name, Gauge gauge) {
        Object gaugeValue = gauge.getValue();

        if (gaugeValue instanceof Number) {
            float n = ((Number) gaugeValue).floatValue();
            if (!Float.isNaN(n) && !Float.isInfinite(n)) {
                record(name+"/gauge", n);
            }
        }
    }

    private void record(String name, float value) {
        logger.info("Reporting metric: "+metricNamePrefix+"/"+name+" : "+value);
        NewRelic.recordMetric(metricNamePrefix+"/" + name, value);
    }
}
