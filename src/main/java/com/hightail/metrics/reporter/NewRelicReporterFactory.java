package com.hightail.metrics.reporter;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.hightail.metrics.agent.NewRelicAgentReporter;
import com.hightail.metrics.constants.NewRelicConstants;
import com.hightail.metrics.constants.ReporterType;
import com.hightail.metrics.exceptions.CannotCreateInstanceException;
import com.hightail.metrics.rest.NewRelic;
import com.hightail.metrics.rest.NewRelicHTTPv1Reporter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 *
 * This is the factory class for getting appropriate NewRelic Reporter Instance.
 *
 * The Madatory properties are:
 *   for Agent:
 *      - MetricRegistry
 *
 *   for Httpv1:
 *      - MetricsRegistry
 *      - NewRelic License Key
 *      - Component Id (The list of components inside a plugin)
 *      - App Id : com.companyname (companyname will appear as the plugin name on the left panel in the dashboard)
 *
 * The Default properties, if not provided explicitly are:
 *      - rate Unit     : {@link java.util.concurrent.TimeUnit.SECONDS}
 *      - duration Unit : {@link java.util.concurrent.TimeUnit.MILLISECONDS}
 *      - metric filter : {@link com.codahale.metrics.MetricFilter.ALL}
 *      - prefix        : null
 *
 *
 * Created by hightail on 4/6/14.
 *
 *
 */
public class NewRelicReporterFactory {

    private static final Logger logger = Logger.getLogger(NewRelicReporterFactory.class);

    public static NewRelicReporter getNewRelicReporter(ReporterType reporterType, Properties properties)
            throws CannotCreateInstanceException {

        NewRelicReporter newRelicReporter = null;

        switch(reporterType) {
            case AGENT: {
                newRelicReporter = buildNewRelicAgentInstance(properties);
                break;
            }
            case HTTPv1: {
                newRelicReporter = buildNewRelicHttpV1Instance(properties);
                break;
            }
        }

        return newRelicReporter;

    }

    private static NewRelicReporter buildNewRelicHttpV1Instance(Properties properties) throws CannotCreateInstanceException {

        List<String> errorMsgs = new ArrayList<String>();

        if(!properties.containsKey(NewRelicConstants.METRIC_REGISTRY) ||  properties.get(NewRelicConstants.METRIC_REGISTRY) == null) {
            errorMsgs.add(NewRelicConstants.METRIC_REGISTRY+" is not provided");
        }

        if(!properties.containsKey(NewRelicConstants.LICENSE_KEY) || StringUtils.isBlank(properties.getProperty(NewRelicConstants.LICENSE_KEY))) {
            errorMsgs.add(NewRelicConstants.LICENSE_KEY+" is not provided");
        }

        if(!properties.containsKey(NewRelicConstants.COMPONENT_NAME) || StringUtils.isBlank(properties.getProperty(NewRelicConstants.COMPONENT_NAME))) {
            errorMsgs.add(NewRelicConstants.COMPONENT_NAME+" is not provided");
        }

        if(!properties.containsKey(NewRelicConstants.APP_ID) || StringUtils.isBlank(properties.getProperty(NewRelicConstants.APP_ID))) {
            errorMsgs.add(NewRelicConstants.APP_ID+" is not provided");
        }

        if(!errorMsgs.isEmpty()) {
            logger.error("Cannot instantiate New Relic Reporter because mandatory attributes are not provided: "+ errorMsgs.toString());
            throw new CannotCreateInstanceException(errorMsgs.toString());
        }

        MetricRegistry registry = (MetricRegistry) properties.get(NewRelicConstants.METRIC_REGISTRY);
        String licenseKey = properties.getProperty(NewRelicConstants.LICENSE_KEY);
        String componentName = properties.getProperty(NewRelicConstants.COMPONENT_NAME);
        String appId = properties.getProperty(NewRelicConstants.APP_ID);

        String prefix = (properties.containsKey(NewRelicConstants.PREFIX)) ? properties.getProperty(NewRelicConstants.PREFIX) :
                NewRelicConstants.DEFAULT_PREFIX;
        TimeUnit rateUnit = (properties.containsKey(NewRelicConstants.RATE_UNIT)) ? (TimeUnit) properties.get(NewRelicConstants.RATE_UNIT) :
                NewRelicConstants.DEFAULT_RATE_UNIT ;
        TimeUnit durationUnit = (properties.containsKey(NewRelicConstants.DURATION_UNIT)) ? (TimeUnit) properties.get(NewRelicConstants.DURATION_UNIT) :
                NewRelicConstants.DEFAULT_DURATION_UNIT;
        MetricFilter filter = (properties.containsKey(NewRelicConstants.METRIC_FILTER))? (MetricFilter) properties.get(NewRelicConstants.METRIC_FILTER):
                NewRelicConstants.DEFAULT_METRIC_FILTER;

        NewRelic newRelic = new NewRelic(NewRelicConstants.DEFAULT_URL,licenseKey,
                componentName,
                appId);

        return NewRelicHTTPv1Reporter
                .forRegistry(registry)
                .prefixedWith(prefix)
                .convertRatesTo(rateUnit)
                .convertDurationsTo(durationUnit)
                .filter(filter)
                .build(newRelic);
    }

    private static NewRelicReporter buildNewRelicAgentInstance(Properties properties) throws CannotCreateInstanceException {


        if(!properties.containsKey(NewRelicConstants.METRIC_REGISTRY) ||  properties.get(NewRelicConstants.METRIC_REGISTRY) == null) {
                throw new CannotCreateInstanceException(NewRelicConstants.METRIC_REGISTRY+" is not provided");
        }

        MetricRegistry registry = (MetricRegistry) properties.get(NewRelicConstants.METRIC_REGISTRY);
        String prefix = (properties.containsKey(NewRelicConstants.PREFIX)) ? properties.getProperty(NewRelicConstants.PREFIX) :
                NewRelicConstants.DEFAULT_PREFIX;
        TimeUnit rateUnit = (properties.containsKey(NewRelicConstants.RATE_UNIT)) ? (TimeUnit) properties.get(NewRelicConstants.RATE_UNIT) :
                NewRelicConstants.DEFAULT_RATE_UNIT ;
        TimeUnit durationUnit = (properties.containsKey(NewRelicConstants.DURATION_UNIT)) ? (TimeUnit) properties.get(NewRelicConstants.DURATION_UNIT) :
                NewRelicConstants.DEFAULT_DURATION_UNIT;
        MetricFilter filter = (properties.containsKey(NewRelicConstants.METRIC_FILTER))? (MetricFilter) properties.get(NewRelicConstants.METRIC_FILTER):
                NewRelicConstants.DEFAULT_METRIC_FILTER;

        return NewRelicAgentReporter
                .forRegistry(registry)
                .prefixedWith(prefix)
                .convertRatesTo(rateUnit)
                .convertDurationsTo(durationUnit)
                .filter(filter)
                .build();

    }
}
