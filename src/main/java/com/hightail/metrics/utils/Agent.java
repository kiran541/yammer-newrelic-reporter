package com.hightail.metrics.utils;

/**
 * Created by hightail on 4/3/14.
 */
public class Agent {

    private String host;
    private int pid;
    private String version;

    public Agent(String host, int pid, String version) {
        this.host = host;
        this.pid = pid;
        this.version = version;
    }

    public String getHost() {
        return host;
    }

    public int getPid() {
        return pid;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "{" +
                "\"host\":\"" + host + '\"' +
                ",\" pid\":\"" + pid + '\"'+
                ", \"version\":\"" + version + '\"' +
                '}';
    }
}
