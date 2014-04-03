package com.hightail.metrics.utils;

import java.util.List;

/**
 * Created by hightail on 4/3/14.
 */
public class NewRelicObj {

    private Agent agent;
    private List<Component> components;

    public NewRelicObj(Agent agent, List<Component> components) {
        this.agent = agent;
        this.components = components;
    }

    public Agent getAgent() {
        return agent;
    }

    public List<Component> getComponents() {
        return components;
    }

    @Override
    public String toString() {
        return "{" +
                "\"agent\":" + agent +
                ", \"components\":" + components +
                '}';
    }
}
