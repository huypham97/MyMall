package com.huypham.mymall;

import com.google.firebase.Timestamp;

public class RewardModel {

    private String type;
    private String lowerLimit;
    private String upperLimit;
    private String discountOrAmount;
    private String couponBody;
    private Timestamp timestamp;
    private boolean alreadyUsed;
    private String couponId;

    public RewardModel(String couponId, String type, String lowerLimit, String upperLimit, String discountOrAmount, String couponBody, Timestamp timestamp, Boolean alreadyUsed) {
        this.couponId = couponId;
        this.type = type;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.discountOrAmount = discountOrAmount;
        this.couponBody = couponBody;
        this.timestamp = timestamp;
        this.alreadyUsed = alreadyUsed;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(String lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public String getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(String upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getDiscountOrAmount() {
        return discountOrAmount;
    }

    public void setDiscountOrAmount(String discountOrAmount) {
        this.discountOrAmount = discountOrAmount;
    }

    public String getCouponBody() {
        return couponBody;
    }

    public void setCouponBody(String couponBody) {
        this.couponBody = couponBody;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAlreadyUsed() {
        return alreadyUsed;
    }

    public void setAlreadyUsed(boolean alreadyUsed) {
        this.alreadyUsed = alreadyUsed;
    }
}
