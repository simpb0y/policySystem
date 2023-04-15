package com.example.es.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.es.dao.HistoryMapper;
import com.example.es.dao.UserMapper;
import com.example.es.entity.History;
import com.example.es.entity.User;
import jep.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.python.util.PythonInterpreter;
import py4j.GatewayServer;

import com.example.es.common.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.search.SearchHits;


import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

@Service
public class ElasticsearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Autowired
    private HistoryMapper historyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HistoryService historyService;

    private ExecutorService executorService;


    private final Object lock = new Object();

    private static final int GATEWAY_PORT = 25333;
    private static final String GATEWAY_ADDRESS = "localhost";
    public SearchResponse search(String index, QueryBuilder query) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(query);
        searchSourceBuilder.size(100);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse;
    }


    public Map<String, Long> getFieldValueCounts(String indexName, String fieldName, int size) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.aggregation(AggregationBuilders.terms("values").field(fieldName).size(100));
        searchSourceBuilder.aggregation(AggregationBuilders.cardinality("unique_count").field(fieldName));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        Terms values = searchResponse.getAggregations().get("values");
        Cardinality uniqueCount = searchResponse.getAggregations().get("unique_count");

        Map<String, Long> fieldValueCounts = new HashMap<>();
        for (Terms.Bucket bucket : values.getBuckets()) {
            fieldValueCounts.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        fieldValueCounts.put("unique_count", uniqueCount.getValue());

        return fieldValueCounts;
    }

    public SearchResponse highSearch(Integer ConditionNum,
                                String condition[],
                                String searchContent[],
                                String searchType[],
                                String time[],
                                Integer timeNum,
                                Boolean isAdvanced,
                                String content,
                                String type,
                                Integer page,
                                Integer size,
                                     Integer userid
                                )throws IOException{


        if(StpUtil.isLogin()){
            if(userid==0){
                QueryWrapper<User> queryWrapper = new QueryWrapper<User>();

                String user_id = StpUtil.getLoginIdAsString();
                queryWrapper.eq("id",user_id);

                String user_province = userMapper.selectOne(queryWrapper).getProvince();
                content = content + user_province;
            }
            else {
                QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
                queryWrapper.eq("id",userid);

                String user_province = userMapper.selectOne(queryWrapper).getProvince();
                content = content + user_province;

            }

        }



        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.multiMatchQuery(content, "POLICY_TITLE", "POLICY_BODY"));

        QueryBuilder fororquery = QueryBuilders.boolQuery()
                .must(QueryBuilders.multiMatchQuery(content, "POLICY_TITLE", "POLICY_BODY"));



        //携带时间条件
        if(timeNum == 1) {
            query = QueryBuilders.boolQuery()
                    .must(query)
                    .must(QueryBuilders.rangeQuery("PUB_TIME").gte(time[0]).lt(time[1]));


        }


        //高级查询
        if(isAdvanced){
            int i = 0;
            while(i<ConditionNum){
                switch (condition[i]){

                    case "并且":
                        query = QueryBuilders.boolQuery()
                                .must(query)
                                .must(QueryBuilders.termQuery(searchType[i]+".keyword",searchContent[i]));
                        break;
                    case "或者":
                        BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
                        shouldQuery.must(QueryBuilders.termQuery(searchType[i]+".keyword",searchContent[i]));
                        query = QueryBuilders.boolQuery()
                                .must(fororquery)
                                .should(shouldQuery)
                                .should(query);
                        break;
                    case "且非":
                        query = QueryBuilders.boolQuery()
                                .must(query)
                                .mustNot(QueryBuilders.termQuery(searchType[i]+".keyword",searchContent[i]));
                        break;
                }
                i++;
            }

        }
        SearchRequest searchRequest = new SearchRequest("policynew");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.fetchSource(null,"POLICY_BODY");
        searchSourceBuilder.query(query);
        searchSourceBuilder.from(page*size-size);
        searchSourceBuilder.size(size);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        SearchHits searchHits = searchResponse.getHits();

//        //获取es_ids
//        Object[] indexValues = Arrays.stream(searchHits.getHits())
//                .map(hit -> hit.getSourceAsMap().get("index"))
//                .toArray();
//        List<Object> es_ids = new ArrayList<>();
//        for (int es_id = 0;es_id<=99;es_id++) {
//            es_ids.add(indexValues[es_id]);
//        }
//        Collections.reverse(es_ids);
//
//        //获取es_dis
//        float[] scores = new float[searchHits.getHits().length];
//
//        for (int es_dis = 0;es_dis<=99;es_dis++) {
//            scores[es_dis] = searchHits.getAt(es_dis).getScore();
//        }
//
//        // 计算得分最大值
//        float maxScore = searchHits.getAt(0).getScore();
//        float minScore = searchHits.getAt(99).getScore();
//
//        // 将得分数组进行归一化排序
//
//        for (int j = 0; j < scores.length; j++) {
//            scores[j] =(scores[j]-minScore)/ (float) ((maxScore-minScore)+1.4E-45);
//        }
//
//        String[] scoresFormatted = new String[scores.length];
//        DecimalFormat df = new DecimalFormat("0.00000000");
//        for (int m = 0; m < scores.length; m++) {
//            scoresFormatted[m] = df.format(scores[m]);
//        }
//
//        List<String> es_dis = new ArrayList<>();
//        for (String score : scoresFormatted) {
//            es_dis.add(score);
//        }
//        Collections.reverse(es_dis);
//
//        try  {
//
////            interp.exec("import sys");
////            interp.exec("sys.path.append('D:/venv/venv')");
////
////
////            //引入py接口
////            interp.exec("from searchTitle_API import searchTitleAPI");
////            interp.exec("from searchBody_API import searchBodyAPI");
////            interp.exec("from TA import Fusion");
//
//
//
//            SharedInterpreter interp = new SharedInterpreter();
//
//
//                interp.exec("title_hnsw = searchTitleAPI('HNSW')");
//                interp.exec("body_hnsw = searchBodyAPI('HNSW')");
//
//                PyString pycontent = Py.newStringOrUnicode(content);
//
//                // 调用Python类中的方法
//                interp.exec("title_hnsw.search('" + pycontent + "', 100)");
//
//                interp.exec("body_hnsw.search('" + pycontent + "', 100)");
//
//                interp.exec("title_id = title_hnsw.getIndex().tolist()");
//                interp.exec("title_dis = title_hnsw.getDistance().tolist()");
//                interp.exec("body_id = body_hnsw.getIndex().tolist()");
//                interp.exec("body_dis = body_hnsw.getDistance().tolist()");
//
//
//                List title_id = interp.getValue("title_id", List.class);
//                List title_dis = interp.getValue("title_dis", List.class);
//                List body_id = interp.getValue("body_id", List.class);
//                List body_dis = interp.getValue("body_dis", List.class);
//
//
//                System.out.println("title_id = " + title_id);
//                System.out.println("title_dis = " + title_dis);
//                System.out.println("body_id = " + body_id);
//                System.out.println("body_dis = " + body_dis);
//                System.out.println("es_ids = " + es_ids);
//                System.out.println("es_dis = " + es_dis);
//
//
//                interp.exec("finallist = Fusion(" + title_id + ", " + title_dis + ", " + body_id + ", " + body_dis + ", " + es_ids + ", " + es_dis + ")");
////            List result = (List)interp.invoke("Fusion" ,title_id,title_dis,body_id,body_dis,es_ids,es_dis);
//
//                List finallist = interp.getValue("finallist", List.class);
//
//                System.out.println("result = " + finallist);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return searchResponse;
    }



//    public ElasticsearchService (){
////        executorService = Executors.newFixedThreadPool(10);
//        SharedInterpreter interp = new SharedInterpreter();
//
//            interp.exec("import sys");
//            interp.exec("sys.path.append('D:/venv/venv')");
//            //引入py接口
//            interp.exec("from searchTitle_API import searchTitleAPI");
//            interp.exec("from searchBody_API import searchBodyAPI");
//            interp.exec("from TA import Fusion");
//
//            Object searchTitle = interp.getValue("searchTitleAPI");
//
//        SharedInterpreter interp2 = new SharedInterpreter();
//
//            interp2.set("searchTitleAPI",searchTitle);
//            interp.exec("title_hnsw = searchTitleAPI('HNSW')");
//
//
//
//    }

    public SearchResponse highSearchold(Integer ConditionNum,
                                     String condition[],
                                     String searchContent[],
                                     String searchType[],
                                     String time[],
                                     Integer timeNum,
                                     Boolean isAdvanced,
                                     String content,
                                     String type,
                                     Integer page,
                                     Integer size
    )throws IOException{

        if(StpUtil.isLogin()){
            QueryWrapper<User> queryWrapper = new QueryWrapper<User>();

            String user_id = StpUtil.getLoginIdAsString();
            queryWrapper.eq("id",user_id);

            String user_province = userMapper.selectOne(queryWrapper).getProvince();
            content = content + user_province;
        }



        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.multiMatchQuery(content, "POLICY_TITLE", "POLICY_BODY"));

        //携带时间条件
        if(timeNum == 1) {
            query = QueryBuilders.boolQuery()
                    .must(query)
                    .must(QueryBuilders.rangeQuery("PUB_TIME").gte(time[0]).lt(time[1]));


        }


        //高级查询
        if(isAdvanced){
            int i = 0;
            while(i<ConditionNum){
                switch (condition[i]){

                    case "并且":
                        query = QueryBuilders.boolQuery()
                                .must(query)
                                .must(QueryBuilders.termQuery(searchType[i]+".keyword",searchContent[i]));
                        break;
                    case "或者":
                        query = QueryBuilders.boolQuery()
                                .must(query)
                                .should(QueryBuilders.termQuery(searchType[i]+".keyword",searchContent[i]));
                        break;
                    case "且非":
                        query = QueryBuilders.boolQuery()
                                .must(query)
                                .mustNot(QueryBuilders.termQuery(searchType[i]+".keyword",searchContent[i]));
                        break;
                }
                i++;
            }

        }
        SearchRequest searchRequest = new SearchRequest("policy");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(query);
        searchSourceBuilder.from(page*size-size);
        searchSourceBuilder.size(size);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        SearchHits searchHits = searchResponse.getHits();



        return searchResponse;
    }





    public Map<String,Integer> countByYear() throws IOException {
        Map<String,Integer> result = new HashMap<>();

        for (Integer year=2018;year<=2022;year++) {

            QueryBuilder queryBuilder = QueryBuilders.rangeQuery("PUB_TIME").gte(year.toString() + "/1/1").lte(year.toString() + "/12/31");

//            DateHistogramAggregationBuilder aggregationBuilder = AggregationBuilders.dateHistogram("monthly_aggs")
//                    .field("PUB_TIME")
//                    .calendarInterval(DateHistogramInterval.MONTH);

            SearchRequest searchRequest = new SearchRequest("policynew");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//            searchSourceBuilder.aggregation(aggregationBuilder);
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.trackTotalHits(true);

            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

//            Histogram agg = searchResponse.getAggregations().get("monthly_aggs");
//            for (Histogram.Bucket entry : agg.getBuckets()) {
//                String key = entry.getKeyAsString(); // key as month
//                long docCount = entry.getDocCount(); // Doc count
//                System.out.println(key + " has " + docCount + " docs");
//            }

            result.put(year.toString(), (int) searchResponse.getHits().getTotalHits().value);

        }
        return result;
    }


    public Map<String,Long> countByMonth() throws IOException {
        Map<String,Long> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest("policynew");

        for(Integer year=2018;year<=2022;year++){
            QueryBuilder queryBuilder = QueryBuilders.rangeQuery("PUB_TIME").gte(year.toString() + "/1/1").lte(year.toString() + "/12/31");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.size(0); // 不返回实际搜索结果

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");

            DateHistogramAggregationBuilder aggregation = AggregationBuilders.dateHistogram("by_month")
                    .field("PUB_TIME")
                    .calendarInterval(DateHistogramInterval.MONTH)
                    .format("yyyy/M/d")
                    .minDocCount(1);

            sourceBuilder.aggregation(aggregation);

            searchRequest.source(sourceBuilder);


            SearchResponse searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);

            Histogram histogram = searchResponse.getAggregations().get("by_month");

            for (Bucket bucket : histogram.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();
                // 将日期转换为月份
                LocalDate date = LocalDate.parse(keyAsString, formatter);
                Integer month = date.getMonthValue();

                // 将记录数添加到对应的月份中
                result.put(year.toString()+"/"+month.toString(),bucket.getDocCount());
//                result.merge(month, bucket.getDocCount(), Long::sum);
            }
        }



        return (result);
    }

    public SearchResponse sortByYear() throws IOException {


        // 创建一个搜索请求
        SearchRequest searchRequest = new SearchRequest("policy");

// 创建一个查询，该查询将匹配所有文档
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();

// 创建一个排序，该排序将按照日期属性降序排序
        SortBuilder sortBuilder = SortBuilders.fieldSort("UPDATE_DATE.keyword").order(SortOrder.DESC);

// 将查询和排序添加到搜索请求中
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.sort(sortBuilder);
        searchSourceBuilder.size(5);
        searchRequest.source(searchSourceBuilder);

// 执行搜索请求并获取响应
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

// 处理搜索响应


        return searchResponse;
    }


    public Result<?> deletePolicy(String doc_id) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("policy", doc_id);

// 执行删除请求并获取响应
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);

// 处理删除响应
        String index = deleteResponse.getIndex();
        String id = deleteResponse.getId();
        long version = deleteResponse.getVersion();
        ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();

        return Result.success();
    }


    public SearchResponse sortByGradeAndDate(String grade) throws IOException {
        // 创建一个搜索请求
        SearchRequest searchRequest = new SearchRequest("policy");

// 创建一个查询，该查询将匹配所有文档
        QueryBuilder queryBuilder = QueryBuilders.termQuery("POLICY_GRADE.keyword",grade);

// 创建一个排序，该排序将按照日期属性降序排序
        SortBuilder sortBuilder = SortBuilders.fieldSort("PUB_TIME.keyword").order(SortOrder.DESC);

// 将查询和排序添加到搜索请求中
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.sort(sortBuilder);
        searchSourceBuilder.size(10);
        searchRequest.source(searchSourceBuilder);

// 执行搜索请求并获取响应
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

// 处理搜索响应


        return searchResponse;


    }


    public SearchResponse addHistory(String searchword) throws IOException {
        SearchRequest searchRequest = new SearchRequest("policy");

// 创建一个查询构造器，该查询构造器将随机选择文档
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(searchword,"POLICY_TITLE","POLICY_BODY","PROVINCE","PUB_AGENCY_FULLNAME","POLICY_SOURCE");

// 将查询构造器添加到搜索请求中
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(100);
        searchSourceBuilder.from(0);
        searchRequest.source(searchSourceBuilder);

// 执行搜索请求并获取响应
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

// 处理搜索响应
        SearchHits hits = searchResponse.getHits();
//        for (SearchHit hit : hits) {
//            // 处理随机选择的文档
//        }

        return searchResponse;
    }








    public SearchResponse getUserHistory(String id) throws IOException {
        QueryBuilder queryBuilder = QueryBuilders.termQuery("POLICY_ID.keyword",id);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        SearchRequest searchRequest = new SearchRequest("policy");
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse =restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);

        return searchResponse;

    }





    public String runTitleSearch() {



        GatewayServer gatewayServer = new GatewayServer();
        gatewayServer.start();


        System.out.println("gatewayServer.getConnectTimeout() = " + gatewayServer.getConnectTimeout());

        System.out.println("gatewayServer.getPythonAddress() = " + gatewayServer.getPythonAddress());


        return gatewayServer.getPythonAddress().toString();
    }

    public Object[] recommend(Integer policynum,Integer userid ,Map<Integer,List> tmap) throws IOException {


        SharedInterpreter interp = new SharedInterpreter();
        Object[] result = new Object[policynum];
        try {

            interp.exec("import sys");
            interp.exec("import os");
//            interp.exec("path1=os.path.abspath('.')");
//            interp.exec("path2=os.path.abspath('..')");
//            String path1 = interp.getValue("path1",String.class);
//            String path2 = interp.getValue("path2",String.class);
//        System.out.println("path1 = " + path1);
//        System.out.println("path2 = " + path2);
//        interp.exec("sys.path.append('D:/venv/venv')");
            interp.exec("sys.path.append('.')");
//            interp.exec("sys.path.append('./src/main/java/com/example/es/py')");
            //引入py接口
            interp.exec("from UserCF import UserCF");

            interp.exec("rates = [0.25, 0.25, 0.25, 0.25]");
            interp.exec("userCF = UserCF(rates)");
            interp.exec("userCF.train()");
            List<User> list = userMapper.selectList(null);

            if(StpUtil.getLoginIdAsInt()>150){
                QueryWrapper<User>  queryWrapper = new QueryWrapper();
                queryWrapper.eq("id",StpUtil.getLoginId());
                User user = userMapper.selectOne(queryWrapper);
                list.add(0,user);
            }

            for (int i = 0; i < 150; i++) {


                Map<String, List<String>> map = new HashMap<>();

                //获取用户职业
                String user_industry = list.get(i).getIndustryId();
                List user_indeustry_list = new ArrayList();
                user_indeustry_list.add(user_industry);

                //获取用户省份
                String user_province = list.get(i).getProvinceId();
                List user_province_list = new ArrayList();
                user_province_list.add(user_province);

                //获取用户感兴趣领域
                List user_area_list = Arrays.asList(list.get(i).getAreaId().split(";"));


                Integer user_id = list.get(i).getId();
                //获取用户历史记录
//            Integer user_id = list.get(i).getId();
//            QueryWrapper<History>  queryWrapper = new QueryWrapper();
//            queryWrapper.eq("user_id",user_id);
//
//
//            List<History> user_history = historyMapper.selectList(queryWrapper);
//            List user_history_list = new ArrayList();
//            for (int j = 0; j < user_history.size(); j++) {
//                user_history_list.add(user_history.get(j).getPolicyIndex());
//            }

                List user_history_list = new ArrayList();
                if(tmap.containsKey(user_id)){
                    user_history_list = tmap.get(user_id);
                }




                map.put("职业", user_indeustry_list);
                map.put("省份", user_province_list);
                map.put("感兴趣的领域", user_area_list);
                map.put("浏览记录", user_history_list);
//            map.put("浏览记录",)
//            System.out.println("i = " + i);
//            System.out.println("map = " + map);
                interp.invoke("userCF.add_user", user_id, map);


            }
            interp.exec("userCF.train()");

            if (userid == 0) {
                interp.exec("recommend = userCF.recommend(" + StpUtil.getLoginIdAsString() + ", " + policynum + ", 80)");
            } else {
                interp.exec("recommend = userCF.recommend(" + userid + ", " + policynum + ", 80)");
            }


            List recommend = interp.getValue("recommend", List.class);

            System.out.println("recommend = " + recommend);


            for (int i = 0; i < recommend.size(); i++) {
                QueryBuilder queryBuilder = QueryBuilders.termQuery("index.keyword", recommend.get(i));
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.size(1);
                searchSourceBuilder.query(queryBuilder);
                SearchRequest searchRequest = new SearchRequest("policy");
                searchRequest.source(searchSourceBuilder);
                SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                result[i] = searchResponse.getHits().getAt(0).getSourceAsMap();
            }

        }
        finally {

            interp.close();


        }


        return result;


    }


    public Result<?>  addindex() throws IOException {

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("policy_id").groupBy("policy_id");
        List<History> policy_list = historyMapper.selectList(queryWrapper);
        System.out.println("policy_list.size() = " + policy_list.size());
        for (int i = 0;i<policy_list.size();i++){

            String policy_id = policy_list.get(i).getPolicyId();

            QueryBuilder queryBuilder = QueryBuilders.termQuery("POLICY_ID.keyword",policy_id);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(queryBuilder);
            searchSourceBuilder.size(1);
            SearchRequest searchRequest = new SearchRequest("policy");
            searchRequest.source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);

            String policy_index = searchResponse.getHits().getAt(0).getSourceAsMap().get("index").toString();

            
            System.out.println("i = " + i);
            UpdateWrapper<History> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("policy_index",policy_index).eq("policy_id",policy_id);
            historyMapper.update(null,updateWrapper);
        }
        return Result.success();
    }



}