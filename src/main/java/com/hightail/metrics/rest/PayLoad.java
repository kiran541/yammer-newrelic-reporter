package com.hightail.metrics.rest;

import com.hightail.metrics.rest.Agent;
import com.hightail.metrics.rest.Component;

import java.util.List;

/**
 * Created by hightail on 4/3/14.
 */
public class PayLoad {

    private Agent agent;
    private List<Component> components;

    public PayLoad(Agent agent, List<Component> components) {
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
        return "PayLoad{" +
                "agent=" + agent +
                ", components=" + components +
                '}';
    }
}
