package com.hightail.metrics.reporter;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;

import java.util.concurrent.TimeUnit;

/**
 * Created by hightail on 4/6/14.
 */
public abstract class NewRelicReporter extends ScheduledReporter{

    /**
     * Creates a new {@link com.codahale.metrics.ScheduledReporter} instance.
     *
     * @param registry     the {@link com.codahale.metrics.MetricRegistry} containing the metrics this
     *                     reporter will report
     * @param name         the reporter's name
     * @param filter       the filter for which metrics to report
     * @param rateUnit
     * @param durationUnit
     */
    protected NewRelicReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, name, filter, rateUnit, durationUnit);
    }
}
