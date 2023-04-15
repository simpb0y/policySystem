package com.example.es.common;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "policy")
public class policy {
    @Id
    private String POLICY_ID;
    private String POLICY_TITLE;
    private String POLICY_GRADE;
    private String PUB_AGENCY_FULLNAME;
    private String PUB_TIME;
    private String POLICY_TYPE;
    private String POLICY_BODY;
    private String POLICY_SOURCE;
    private String UPDATE_DATE;
    private String index;



    // getters and setters
}
