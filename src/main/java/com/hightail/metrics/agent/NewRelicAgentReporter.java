package com.hightail.metrics.agent;

import com.codahale.metrics.*;
import com.hightail.metrics.reporter.NewRelicReporter;
import com.newrelic.api.agent.NewRelic;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * This is the New Relic Agent Reporter.
 * To report your metrics to New Relic, you have to attach java agent in the classpath
 *
 * Created by hightail on 4/6/14.
 */
public class NewRelicAgentReporter extends NewRelicReporter {

    /**
     * Returns a new {@link Builder} for {@link NewRelicAgentReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link NewRelicAgentReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link NewRelicAgentReporter} instances. Defaults to not using a prefix, using the
     * default clock, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

         /**
         * Prefix all metric names with the given string.
         *
         * @param prefix the prefix for all metric names
         * @return {@code this}
         */
        public Builder prefixedWith(String prefix) {
            this.prefix = prefix;
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
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link NewRelicAgentReporter} with the given properties, sending metrics using the
         *
         * @return a {@link NewRelicAgentReporter}
         */
        public NewRelicAgentReporter build() {
            return new NewRelicAgentReporter(registry,
                    prefix,
                    filter,
                    rateUnit,
                    durationUnit);
        }
    }

    private final String prefix;


    /**
     * Creates a new {@link com.codahale.metrics.ScheduledReporter} instance.
     *
     * @param registry     the {@link com.codahale.metrics.MetricRegistry} containing the metrics this
     *                     reporter will report
     * @param prefix         the reporter's name
     * @param filter       the filter for which metrics to report
     * @param rateUnit
     * @param durationUnit
     */
    protected NewRelicAgentReporter(MetricRegistry registry, String prefix, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, "newrelic-reporter", filter, rateUnit, durationUnit);
        this.prefix = prefix;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

        try {

            System.out.print("Inside Report...");

            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                reportGauge(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                reportCounter(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                reportHistogram(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                reportMetered(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                reportTimer(entry.getKey(), entry.getValue());
            }
        } catch (Exception ex) {

            ex.printStackTrace();
            System.out.println("Exception occured while pishing metrics to newrelic: "+ ex.getMessage());
            //LOGGER.warn("Unable to report to NewRelic", e);
        } finally {
        }
    }

    private void reportTimer(String name, Timer timer) {

        final Snapshot snapshot = timer.getSnapshot();

        sendToNewRelic(prefix(name, "max"), convertDuration(snapshot.getMax()));
        sendToNewRelic(prefix(name, "mean"), convertDuration(snapshot.getMean()));
        sendToNewRelic(prefix(name, "min"), convertDuration(snapshot.getMin()));
        sendToNewRelic(prefix(name, "stddev"), convertDuration(snapshot.getStdDev()));
        sendToNewRelic(prefix(name, "p50"), convertDuration(snapshot.getMedian()));
        sendToNewRelic(prefix(name, "p75"), convertDuration(snapshot.get75thPercentile()));
        sendToNewRelic(prefix(name, "p95"), convertDuration(snapshot.get95thPercentile()));
        sendToNewRelic(prefix(name, "p98"), convertDuration(snapshot.get98thPercentile()));
        sendToNewRelic(prefix(name, "p99"), convertDuration(snapshot.get99thPercentile()));
        sendToNewRelic(prefix(name, "p999"), convertDuration(snapshot.get999thPercentile()));

    }

    private void sendToNewRelic(String name, double value) {
        sendToNewRelic(name, (float) value);
    }

    private void sendToNewRelic(String name, float value) {

        System.out.println("Recording metric to NewRelic: " + name + ": "
                + value);
        NewRelic.recordMetric(name, value);
    }

    private void reportMetered(String name, Meter meter) {
        sendToNewRelic(prefix(name, "count"), convertDuration(meter.getCount()));
        sendToNewRelic(prefix(name, "m1_rate"), convertDuration(meter.getOneMinuteRate()));
        sendToNewRelic(prefix(name, "m5_rate"), convertDuration(meter.getFiveMinuteRate()));
        sendToNewRelic(prefix(name, "m15_rate"), convertDuration(meter.getFifteenMinuteRate()));
        sendToNewRelic(prefix(name, "mean_rate"), convertDuration(meter.getMeanRate()));
    }

    private void reportHistogram(String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();

        sendToNewRelic(prefix(name, "max"), convertDuration(snapshot.getMax()));
        sendToNewRelic(prefix(name, "mean"), convertDuration(snapshot.getMean()));
        sendToNewRelic(prefix(name, "min"), convertDuration(snapshot.getMin()));
        sendToNewRelic(prefix(name, "stddev"), convertDuration(snapshot.getStdDev()));
        sendToNewRelic(prefix(name, "p50"), convertDuration(snapshot.getMedian()));
        sendToNewRelic(prefix(name, "p75"), convertDuration(snapshot.get75thPercentile()));
        sendToNewRelic(prefix(name, "p95"), convertDuration(snapshot.get95thPercentile()));
        sendToNewRelic(prefix(name, "p98"), convertDuration(snapshot.get98thPercentile()));
        sendToNewRelic(prefix(name, "p99"), convertDuration(snapshot.get99thPercentile()));
        sendToNewRelic(prefix(name, "p999"), convertDuration(snapshot.get999thPercentile()));
    }

    private void reportCounter(String name, Counter counter) {
        sendToNewRelic(prefix(name, "count"), convertDuration(counter.getCount()));

    }

    private void reportGauge(String name, Gauge gauge) {
        //NewRelic.recordMetric(prefix(name), gauge.getValue());
    }

    private String prefix(String... components) {
        return MetricRegistry.name(prefix, components);
    }

    private String format(long n) {
        return Long.toString(n);
    }
}
