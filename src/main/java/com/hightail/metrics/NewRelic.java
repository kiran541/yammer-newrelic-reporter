package com.hightail.metrics;

/**
 * Created by hightail on 4/3/14.
 */
public class NewRelic {

    private String endpointURI = "https://platform-api.newrelic.com/platform/v1/metrics";
    private String licenseKey;
    private String pluginName;
    private String guid;
    private int duration;

    public NewRelic(String endpointURI, String licenseKey, String pluginName, String guid, int duration) {
        this.endpointURI = endpointURI;
        this.licenseKey = licenseKey;
        this.pluginName = pluginName;
        this.guid = guid;
        this.duration = duration;
    }

    public String getEndpointURI() {
        return endpointURI;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getGuid() {
        return guid;
    }

    public int getDuration() {
        return duration;
    }


}
