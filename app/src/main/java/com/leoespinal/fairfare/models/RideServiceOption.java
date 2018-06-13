package com.leoespinal.fairfare.models;

public class RideServiceOption {
    private String serviceBaseName;
    private String rideProductName;
    private Integer baseRate;
    private int capacity;
    private Integer lowRateEstimate;
    private Integer highRateEstimate;
    private Float surgeMultiplier;
    private String fareId;
    private Integer eta;
    private String estimateRange;

    //General constructor
    public RideServiceOption() {}

    //Uber constructor
    public RideServiceOption(String serviceBaseName, String rideProductName, Integer lowRateEstimate, Integer highRateEstimate, Float surgeMultiplier, String fareId, Integer eta) {
        this.serviceBaseName = serviceBaseName;
        this.rideProductName = rideProductName;
        this.lowRateEstimate = lowRateEstimate;
        this.highRateEstimate = highRateEstimate;
        this.surgeMultiplier = surgeMultiplier;
        this.fareId = fareId;
        this.eta = eta;
    }

    //Lyft constructor
    //Note: lowRateEstimate and highRateEstimate is in cents, need to convert to dollars for UI, eta is in seconds -> convert to mins for UI
    public RideServiceOption(String serviceBaseName, String rideProductName, Integer baseRate, Integer lowRateEstimate, Integer highRateEstimate, Integer eta) {
        this.serviceBaseName = serviceBaseName;
        this.rideProductName = rideProductName;
        this.baseRate = baseRate;
        this.lowRateEstimate = lowRateEstimate;
        this.highRateEstimate = highRateEstimate;
        this.eta = eta;
    }

    public String getServiceBaseName() {
        return serviceBaseName;
    }

    public void setServiceBaseName(String serviceBaseName) {
        this.serviceBaseName = serviceBaseName;
    }

    public String getRideProductName() {
        return rideProductName;
    }

    public void setRideProductName(String rideProductName) {
        this.rideProductName = rideProductName;
    }

    public Integer getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(Integer baseRate) {
        this.baseRate = baseRate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Integer getLowRateEstimate() {
        return lowRateEstimate;
    }

    public void setLowRateEstimate(Integer lowRateEstimate) {
        this.lowRateEstimate = lowRateEstimate;
    }

    public Integer getHighRateEstimate() {
        return highRateEstimate;
    }

    public void setHighRateEstimate(Integer highRateEstimate) {
        this.highRateEstimate = highRateEstimate;
    }

    public Float getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public void setSurgeMultiplier(Float surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }

    public String getFareId() {
        return fareId;
    }

    public void setFareId(String fareId) {
        this.fareId = fareId;
    }

    public Integer getEta() {
        return eta;
    }

    public void setEta(Integer eta) {
        this.eta = eta;
    }

    public String getEstimateRange() {
        return estimateRange;
    }

    public void setEstimateRange(String estimateRange) {
        this.estimateRange = estimateRange;
    }
}
