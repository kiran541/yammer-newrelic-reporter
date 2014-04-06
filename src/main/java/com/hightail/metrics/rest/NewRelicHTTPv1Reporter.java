package com.hightail.metrics.rest;

import com.codahale.metrics.*;
import com.google.gson.Gson;
import com.hightail.metrics.constants.NewRelicConstants;
import com.hightail.metrics.reporter.NewRelicReporter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * The New Relic Reporter, which reports the data to New Relic via REST calls
 */
public class NewRelicHTTPv1Reporter extends NewRelicReporter {

    private static final Logger logger = Logger.getLogger(NewRelicHTTPv1Reporter.class);

    private NewRelic newRelic;
    private String prefix;
    private Client client;
    private Agent agent;

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
    protected NewRelicHTTPv1Reporter(NewRelic newRelic, MetricRegistry registry, String prefix, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, prefix, filter, rateUnit, durationUnit);
        this.newRelic = newRelic;
        this.prefix = prefix;
        ClientConfig config = new DefaultClientConfig();
        client = Client.create(config);

        try {
            agent = new Agent(InetAddress.getLocalHost().getHostName(), NewRelicConstants.DEFAULT_AGENT_PID, NewRelicConstants.DEFAULT_AGENT_VERSION);
        } catch (UnknownHostException uhex) {
            logger.error("This can be ignored: Agent hostId Error: ", uhex);
            agent = new Agent(NewRelicConstants.DEFAULT_AGENT_HOST, NewRelicConstants.DEFAULT_AGENT_PID, NewRelicConstants.DEFAULT_AGENT_VERSION);
        }

        logger.info("NewRelicHTTPv1Reporter initialized");
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
         * Builds a {@link NewRelicHTTPv1Reporter} with the given properties, sending metrics using the
         * given {@link NewRelic} client.
         *
         * @param newRelic a {@link NewRelic} client
         * @return a {@link NewRelicHTTPv1Reporter}
         */
        public NewRelicHTTPv1Reporter build(NewRelic newRelic) {
            return new NewRelicHTTPv1Reporter(newRelic,
                    registry,
                    prefix,
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
        logger.debug("Reporting timer metrics..");

        final Snapshot snapshot = timer.getSnapshot();

        Map<String, Float> metrics = new HashMap<String, Float>();
        metrics.put("min", (float) convertDuration(snapshot.getMin()));
        metrics.put("max", (float) convertDuration(snapshot.getMax()));
        metrics.put("count", (float) timer.getCount());
        metrics.put("total", (float) timer.getCount());

        Map<String, Map<String, Float>> componentMetrics = new HashMap<String, Map<String, Float>>();
        componentMetrics.put(prefix(name), metrics);
        Component component = new Component(newRelic.getComponentName(), newRelic.getAppId(), newRelic.getDuration(), componentMetrics);

        postToNewRelic(new PayLoad(agent, Arrays.asList(component)));

    }

    private void postToNewRelic(PayLoad payLoad) {

        try {
            Gson gson = new Gson();
            ClientResponse response = client.resource(new URI(newRelic.getEndpointURI())).
                    accept(MediaType.APPLICATION_JSON).
                    type(MediaType.APPLICATION_JSON).
                    header("X-License-Key",newRelic.getLicenseKey()).
                    post(ClientResponse.class, gson.toJson(payLoad));
            int status = response.getStatus();
            if(status != 200) {
                throw new Exception("NewRelic returned status: "+ status);
            }
            logger.info("Posted to newRelic: "+ payLoad.toString());

        } catch (Exception ex) {
            logger.error("Could not post to newRelic: ", ex);
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

    private String prefix(String name) {
        StringBuffer stringBuffer = new StringBuffer();
        if(StringUtils.isNotBlank(prefix)) {
            stringBuffer.append(prefix).append("/");
        }
        stringBuffer.append(name);
        return stringBuffer.toString();
    }


}
