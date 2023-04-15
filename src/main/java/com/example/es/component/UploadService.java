package com.example.es.component;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class UploadService {
    @Autowired
    private RestHighLevelClient client;

    public void uploadJsonFile(File file, String indexName, String typeName) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            IndexRequest request = new IndexRequest(indexName, typeName, String.valueOf(i+1));
            request.source(lines[i], XContentType.JSON);
            IndexResponse response =client.index(request, RequestOptions.DEFAULT);
            System.out.println("Inserted document " + (i+1) + " to Elasticsearch, result: " + response.getResult());
        }
    }
}
