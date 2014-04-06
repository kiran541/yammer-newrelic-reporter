package com.hightail.metrics.rest;

import java.util.Map;

/**
 * Created by hightail on 4/3/14.
 */
public class Component {

    private String name;
    private String guid;
    private int duration;
    Map<String, Map<String, Float>> metrics;

    public Component(String name, String guid, int duration, Map<String, Map<String, Float>> metrics) {
        this.name = name;
        this.guid = guid;
        this.duration = duration;
        this.metrics = metrics;
    }

    public String getName() {
        return name;
    }

    public String getGuid() {
        return guid;
    }

    public int getDuration() {
        return duration;
    }

    public Map<String, Map<String, Float>> getMetrics() {
        return metrics;
    }

    @Override
    public String toString() {
        return "Component{" +
                "name='" + name + '\'' +
                ", guid='" + guid + '\'' +
                ", duration=" + duration +
                ", metrics=" + metrics +
                '}';
    }
}
