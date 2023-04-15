package com.example.es.controller;

import com.example.es.component.UploadService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class UploadController {
    @Autowired
    private UploadService uploadService;

    @Autowired
    private RestHighLevelClient client;

    @PostMapping("/upload")
    public String uploadJsonFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam("indexName") String indexName,
                                 @RequestParam("typeName") String typeName) throws IOException {
        if (file.isEmpty()) {
            return "File is empty";
        }

        String fileName = file.getOriginalFilename();
        File tempFile = new File(fileName);
        file.transferTo(tempFile);

        uploadService.uploadJsonFile(tempFile, indexName, typeName);

        tempFile.delete();

        return "File uploaded successfully";
    }

    @PostMapping("/uploadtsv")
    public String uploadTsv(@RequestParam("file") MultipartFile file) throws Exception {
        // 解析 TSV 文件并将数据插入到 Elasticsearch 中
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(),"utf-8"));
        String line = null;
        int count = 0;
//
        while ((line = reader.readLine()) != null){

            count++;
            System.out.println("count = " + count);
            String[] fields = line.split("\t");
            System.out.println("fields.length = " + fields.length);

            IndexRequest indexRequest = new IndexRequest("policy");
            indexRequest.source(
                    "POLICY_ID",fields[0],
                    "POLICY_TITLE", fields[1],
                    "POLICY_GRADE",fields[2],
                    "PUB_AGENCY_FULLNAME",fields[3],
                    "PUB_TIME",fields[4],
                    "POLICY_TYPE",fields[5],
                    "POLICY_BODY",fields[6],
                    "PROVINCE",fields[7],
                    "POLICY_SOURCE",fields[8],
                    "UPDATE_DATE",fields[9],
                    "index",fields[10]);
            client.index(indexRequest, RequestOptions.DEFAULT);
        }
        reader.close();

        return "success";
    }


    @PostMapping("/index/{indexName}")
    public ResponseEntity<?> indexJsonFile(@PathVariable String indexName, @RequestParam("file") MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                IndexRequest indexRequest = new IndexRequest(indexName);
                indexRequest.source(line, XContentType.JSON);
                client.index(indexRequest,RequestOptions.DEFAULT);
            }
        } catch (IOException e) {
            // 处理异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }


}
