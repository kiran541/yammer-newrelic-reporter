package com.hightail.metrics.utils;

import java.util.Map;

/**
 * Created by hightail on 4/3/14.
 */
public class Component {

    private String name;
    private String guid;
    private int duration;
    Map<String, Float> metrics;

    public Component(String name, String guid, int duration, Map<String, Float> metrics) {
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

    public Map<String, Float> getMetrics() {
        return metrics;
    }

    @Override
    public String toString() {
        return "{" +
                "\"name\":\"" + name + '\"' +
                ", \"guid\":\"" + guid + '\"' +
                ", \"duration\":\"" + duration +'\"'+
                ", \"metrics\":" + metrics +
                '}';
    }
}
