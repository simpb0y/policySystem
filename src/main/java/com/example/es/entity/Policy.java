package com.example.es.entity;

import lombok.Data;

@Data
public class Policy {
    private String policyIndex;

    private String policyId;

    private Integer viewNum;

    public Policy(String policyIndex, String policyId, Integer viewNum) {
        this.policyIndex = policyIndex;
        this.policyId = policyId;
        this.viewNum = viewNum;
    }

    public Policy() {
        super();
    }

    public String getPolicyIndex() {
        return policyIndex;
    }

    public void setPolicyIndex(String policyIndex) {
        this.policyIndex = policyIndex == null ? null : policyIndex.trim();
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId == null ? null : policyId.trim();
    }

    public Integer getViewNum() {
        return viewNum;
    }

    public void setViewNum(Integer viewNum) {
        this.viewNum = viewNum;
    }
}