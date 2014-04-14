package com.hightail.metrics.rest;

/**
 * Created by hightail on 4/3/14.
 */
public class NewRelic {

    private String endpointURI = "https://platform-api.newrelic.com/platform/v1/definedMetrics";
    private String licenseKey;
    private String componentName;
    private String appId;

    /**
     * The New Relic instance that has the custom properties defined
     *
     * @param endpointURI The metric data is sent as an HTTP POST of JSON data using this URI
     * @param licenseKey Your new Relic License Key (Note: This is not the API key)
     * @param componentName Your Component Name
     * @param appId This is the Plugin Name that wil appear in the left panel
     * @param duration The duration over which the data is sent to this plugin
     *
     */
    public NewRelic(String endpointURI, String licenseKey,
                    String componentName, String appId) {

        this.endpointURI = endpointURI;
        this.licenseKey = licenseKey;
        this.componentName = componentName;
        this.appId = appId;

    }

    public String getEndpointURI() {
        return endpointURI;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getAppId() {
        return appId;
    }

}
