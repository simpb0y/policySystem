package com.example.es;


import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class EsApplicationTests {




//    private RestHighLevelClient client;
//
//    @Test
//    void contextLoads() {
//        System.out.println("client = " + client);
//    }
//
//    @Test
//    void testCreateClient() throws IOException {
//        HttpHost host = HttpHost.create("http://127.0.0.1:9200");
//        RestClientBuilder builder = RestClient.builder(host);
//        client = new RestHighLevelClient(builder);
//        System.out.println("client = " + client);
//        client.close();
//    }
//
//    @Test
//    void testCreateIndex() throws IOException {
//        HttpHost host = HttpHost.create("http://127.0.0.1:9200");
//        RestClientBuilder builder = RestClient.builder(host);
//        client = new RestHighLevelClient(builder);
//
//        CreateIndexRequest request = new CreateIndexRequest("tickets");
//        client.indices().create(request, RequestOptions.DEFAULT);
//
//        client.close();
//    }
//
//    @Test
//    void testGet() throws IOException {
//        GetRequest request = new GetRequest("books", null);
//        GetResponse response = client.get(request, RequestOptions.DEFAULT);
//        String json = response.getSourceAsString();
//        System.out.println(json);
//    }
}
