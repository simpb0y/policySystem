package com.example.es.entity;

import lombok.Data;

import java.util.Date;

@Data
public class History {
    private String userId;

    private String policyId;

    private Date timestamp;

    private String policyIndex;

    public History(String userId, String policyId, Date timestamp, String policyIndex) {
        this.userId = userId;
        this.policyId = policyId;
        this.timestamp = timestamp;
        this.policyIndex = policyIndex;
    }

    public History() {
        super();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId == null ? null : policyId.trim();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPolicyIndex() {
        return policyIndex;
    }

    public void setPolicyIndex(String policyIndex) {
        this.policyIndex = policyIndex == null ? null : policyIndex.trim();
    }
}