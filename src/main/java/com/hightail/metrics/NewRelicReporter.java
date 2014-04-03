package com.hightail.metrics;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.google.gson.Gson;
import com.hightail.metrics.utils.Agent;
import com.hightail.metrics.utils.Component;
import com.hightail.metrics.utils.NewRelicObj;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by hightail on 4/3/14.
 */
public class NewRelicReporter  extends ScheduledReporter {


    /**
     * Returns a new {@link Builder} for {@link NewRelicReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link NewRelicReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

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
         * Builds a {@link NewRelicReporter} with the given properties, sending metrics using the
         * given {@link com.hightail.metrics.NewRelic} client.
         *
         * @param newRelic a {@link com.hightail.metrics.NewRelic} client
         * @return a {@link com.hightail.metrics.NewRelicReporter}
         */
        public NewRelicReporter build(NewRelic newRelic) {
            return new NewRelicReporter(newRelic,
                    registry,
                    prefix,
                    filter,
                    rateUnit,
                    durationUnit
            );
        }
    }

    private NewRelic newRelic;
    private String prefix;
    private Client client;

    /**
     * Creates a new {@link com.codahale.metrics.ScheduledReporter} instance.
     *
     * @param registry     the {@link com.codahale.metrics.MetricRegistry} containing the metrics this
     *                     reporter will report
     * @param prefix       the reporter's name
     * @param filter       the filter for which metrics to report
     * @param rateUnit
     * @param durationUnit
     */
    protected NewRelicReporter(NewRelic newRelic, MetricRegistry registry, String prefix, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, prefix, filter, rateUnit, durationUnit);
        this.newRelic = newRelic;
        this.prefix = prefix;

        ClientConfig config = new DefaultClientConfig();
        client = Client.create(config);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

        try {

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
        } finally {
        }
    }

    private void reportTimer(String name, Timer timer) {

        final Snapshot snapshot = timer.getSnapshot();

        Map<String, Float> componentMetrics = new HashMap<String, Float>();

        componentMetrics.put(prefix(name, "max"), (float) convertDuration(snapshot.getMax()));
        componentMetrics.put(prefix(name, "mean"), (float) convertDuration(snapshot.getMean()));
        componentMetrics.put(prefix(name, "min"), (float) convertDuration(snapshot.getMin()));
        componentMetrics.put(prefix(name, "stddev"), (float) convertDuration(snapshot.getStdDev()));
        componentMetrics.put(prefix(name, "p50"), (float) convertDuration(snapshot.getMedian()));
        componentMetrics.put(prefix(name, "p75"), (float) convertDuration(snapshot.get75thPercentile()));
        componentMetrics.put(prefix(name, "p95"), (float) convertDuration(snapshot.get95thPercentile()));
        componentMetrics.put(prefix(name, "p98"), (float) convertDuration(snapshot.get98thPercentile()));
        componentMetrics.put(prefix(name, "p99"), (float) convertDuration(snapshot.get99thPercentile()));
        componentMetrics.put(prefix(name, "p999"), (float) convertDuration(snapshot.get999thPercentile()));

        Agent agent = new Agent("application.metrics.hightail",1,"1.0");
        List<Component> components = new ArrayList<Component>();
        components.add(new Component(newRelic.getPluginName(), newRelic.getGuid(), newRelic.getDuration(), componentMetrics));

        postToNewRelic(new NewRelicObj(agent, components));

    }

    private void postToNewRelic(NewRelicObj newRelicObj) {

        System.out.println("Posting to NewRelic: "+ new Gson().toJson(newRelicObj));
        try {
            Gson gson = new Gson();
            ClientResponse response = client.resource(new URI(newRelic.getEndpointURI())).
                    accept(MediaType.APPLICATION_JSON).
                    type(MediaType.APPLICATION_JSON).
                    header("X-License-Key",newRelic.getLicenseKey()).
                    post(ClientResponse.class, gson.toJson(newRelicObj));
            int status = response.getStatus();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void reportMetered(String name, Meter meter) {

    }

    private void reportHistogram(String name, Histogram histogram) {

    }

    private void reportCounter(String name, Counter counter) {

    }

    private void reportGauge(String name, Gauge gauge) {

    }

    private String prefix(String... components) {
        return MetricRegistry.name(prefix, components);
    }


}
