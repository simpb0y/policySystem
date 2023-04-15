package com.example.es.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.es.dao.HistoryMapper;
import com.example.es.common.Result;
import com.example.es.dao.UserMapper;
import com.example.es.entity.History;
import com.example.es.entity.User;
import com.example.es.service.ElasticsearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.*;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;

@RestController
public class EsSearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserMapper userMapper;


    @Autowired
    private HistoryMapper historyMapper;


    Map<Integer,List> tmap = new TreeMap<>();
//    /**
//     * 查询
//     * @param keyword
//     * @return
//     */
//    @GetMapping("/search")
//    public ResponseEntity<?> Essearch(@RequestParam String keyword){
//
//        QueryBuilder query = QueryBuilders.multiMatchQuery(keyword,"POLICY_TITLE","POLICY_BODY" );
//        try {
//            SearchResponse searchResponse = elasticsearchService.search("policy", query);
//            // 处理查询结果
//            Long count = searchResponse.getHits().getTotalHits().value;
//            System.out.println("count = " + count);
//            return ResponseEntity.ok(searchResponse);
//        } catch (IOException e) {
//            // 处理异常
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }


//    @GetMapping("/search")
//    public Result<?> advancedSearch(@RequestParam Integer ConditionNum,
//                                    @RequestParam String condition[],
//                                    @RequestParam String searchContent[],
//                                    @RequestParam String searchType[],
//                                    @RequestParam Date time[],
//                                    @RequestParam Integer timeNum,
//                                    @RequestParam Boolean isAdvanced,
//                                    @RequestParam String content,
//                                    @RequestParam String type,
//                                    @RequestParam Integer page,
//                                    @RequestParam Integer size
//
//                                    ) throws IOException {
//
//
//
//        //普通查询
//        QueryBuilder query = QueryBuilders.boolQuery()
//                .must(QueryBuilders.multiMatchQuery(content, "POLICY_TITLE", "POLICY_BODY"));
//
//        //携带时间条件
//        if(timeNum == 1) {
//            query = QueryBuilders.boolQuery()
//                    .must(query)
//                    .must(QueryBuilders.rangeQuery("PUB_TIME").gte(time[0]).lt(time[1]));
//
//        }
//
//        //高级查询
//        if(isAdvanced){
//            int i = 0;
//            while(i<ConditionNum){
//                switch (condition[i]){
//
//                    case "并且":
//                        query = QueryBuilders.boolQuery()
//                                .must(query)
//                                .must(QueryBuilders.termQuery(searchType[i]+".keyword",searchContent[i]));
//                        break;
//                    case "或者":
//                        query = QueryBuilders.boolQuery()
//                                .must(query)
//                                .should(QueryBuilders.termQuery(searchType[i]+".keyword",searchContent[i]));
//                        break;
//                    case "且非":
//                        query = QueryBuilders.boolQuery()
//                                .must(query)
//                                .mustNot(QueryBuilders.termQuery(searchType[i]+".keyword",searchContent[i]));
//                        break;
//                }
//                i++;
//            }
//
//        }
//
//        SearchResponse searchResponse = elasticsearchService.search("policy", query);
//
//
//
//
//
//        return Result.success(searchResponse.getHits());
//
//
//    }

    @GetMapping("/search")
    public Result<?> highSearch(@RequestParam Integer ConditionNum,
                                    @RequestParam String condition[],
                                    @RequestParam String searchContent[],
                                    @RequestParam String searchType[],
                                    @RequestParam String time[],
                                    @RequestParam Integer timeNum,
                                    @RequestParam Boolean isAdvanced,
                                    @RequestParam String content,
                                    @RequestParam String type,
                                    @RequestParam Integer page,
                                    @RequestParam Integer size,
                                @RequestParam Integer userid
) throws IOException {

        return Result.success(elasticsearchService.highSearch(ConditionNum,condition,searchContent,searchType,time,timeNum,isAdvanced,content,type,page,size,userid).getHits());
    }

    @GetMapping("/searchold")
    public Result<?> highSearchold(@RequestParam Integer ConditionNum,
                                @RequestParam String condition[],
                                @RequestParam String searchContent[],
                                @RequestParam String searchType[],
                                @RequestParam String time[],
                                @RequestParam Integer timeNum,
                                @RequestParam Boolean isAdvanced,
                                @RequestParam String content,
                                @RequestParam String type,
                                @RequestParam Integer page,
                                @RequestParam Integer size

    ) throws IOException {

        return Result.success(elasticsearchService.highSearchold(ConditionNum,condition,searchContent,searchType,time,timeNum,isAdvanced,content,type,page,size).getHits());
    }



    @PostConstruct
    @GetMapping("/addtmap")
    public Result<?> getHistoryMap(){





        List<History> histories = historyMapper.selectList(null);
        List<User> user_list = userMapper.selectList(null);

        for (int i = 0; i < user_list.size(); i++) {
            QueryWrapper<History> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",user_list.get(i).getId());
            List<History> user_history = historyMapper.selectList(queryWrapper);
            List user_history_list = new ArrayList();
            for (int j = 0; j < user_history.size(); j++) {
                user_history_list.add(user_history.get(j).getPolicyIndex());
            }

            this.tmap.put(user_list.get(i).getId(),user_history_list);

        }




        System.out.println("OKK");

        return Result.success(tmap);





    }

    @GetMapping("/detail/{POLICY_ID}")
    public Result<?> getPolicyDetail(@PathVariable String POLICY_ID){
        QueryBuilder query = QueryBuilders.termQuery("POLICY_ID",POLICY_ID);
        try {
            SearchResponse searchResponse = elasticsearchService.search("policy", query);
            // 处理查询结果

            Map detail = searchResponse.getHits().getAt(0).getSourceAsMap();

            String id = null;
            if(StpUtil.isLogin()){
                id = StpUtil.getLoginIdAsString();
                System.out.println("id = " + id);
            }



            History history = new History();
            history.setUserId(id);
            history.setPolicyId(POLICY_ID);
            history.setPolicyIndex(detail.get("index").toString());
            history.setTimestamp(new Timestamp(System.currentTimeMillis()));
            historyMapper.insert(history);

            return Result.success(detail);
        } catch (IOException e) {
            // 处理异常
            return Result.error("404","Not Found");
        }
    }


    @GetMapping("/yearcount")
    public Result<?> yearCount() throws IOException {
        return Result.success(elasticsearchService.countByYear());
    }

    @GetMapping("/monthcount")
    public Result<?> monthCount() throws IOException {
        return Result.success(elasticsearchService.countByMonth());
    }



    @GetMapping("/province")
    public Result<?> allprovinceCount() throws IOException {

        return Result.success(elasticsearchService.getFieldValueCounts("policy","PROVINCE.keyword",0));
    }



    @GetMapping("/news")
    public Result<?> sortByDate() throws IOException {
        System.out.println("elasticsearchService.sortByYear().getHits().getTotalHits() = " + elasticsearchService.sortByYear().getHits().getTotalHits());

        SearchHits hits = elasticsearchService.sortByYear().getHits();
        Object[] result = {0,0,0,0,0};
        for(int i=0;i<5;i++){
            result[i] = hits.getAt(i).getSourceAsMap();

        }


        return Result.success(result);


    }

    @GetMapping("/gradenews")
    public Result<?> sortByGradeAndDate(@RequestParam String grade) throws IOException {
        SearchHits hits = elasticsearchService.sortByGradeAndDate(grade).getHits();
        Object[] result = {0,0,0,0,0,0};

        System.out.println("elasticsearchService.sortByGradeAndDate(grade).getHits().getTotalHits() = " + elasticsearchService.sortByGradeAndDate(grade).getHits().getTotalHits());
        for(int i=0;i<6;i++){
            result[i] = hits.getAt(i).getSourceAsMap();

        }
        return Result.success(result);

    }


    /**
     * 获得向量图
     * @param keyword
     * @return
     */





    /**
     *读id写标题
     */

//    @GetMapping("/getTitle")
//
//    public String getTitle(){
//
//        File idFile = new File("C:\\Users\\ASUS\\Desktop\\es\\id.txt");
//        File titleFile = new File("C:\\Users\\ASUS\\Desktop\\es\\es_data.txt");
//
//        try (Scanner scanner = new Scanner(idFile);
////             FileWriter writer = new FileWriter(titleFile)
//             Writer writer = new OutputStreamWriter(new FileOutputStream(titleFile), "GB18030");
//        )
//        {
//            while (scanner.hasNextLine()) {
//                String id = scanner.nextLine();
//                System.out.println("id = " + id);
//
//                QueryBuilder query = QueryBuilders.termQuery("index",id);
//                SearchResponse searchResponse = elasticsearchService.search("policy", query);
//
//                SearchHits hits = searchResponse.getHits();
////                System.out.println("hits.getTotalHits() = " + hits.getTotalHits());
////
////                System.out.println("hits = " + hits);
//
//                if(hits.getTotalHits().value==0L){
//                    writer.write("NULL" + "\n");
//                }
//
//                else {
//                    SearchHit firstHit = hits.getAt(0);
//                    String title = firstHit.getSourceAsMap().get("POLICY_TITLE").toString();
//
//                    //以下为根据标题查询
//
//                    QueryBuilder titleQuery = QueryBuilders.multiMatchQuery(title,"POLICY_TITLE","POLICY_BODY");
//                    SearchResponse titleResponse =  elasticsearchService.search("policy", titleQuery);
//                    SearchHits titleHits = titleResponse.getHits();
//
//                    Object[] indexValues = Arrays.stream(titleHits.getHits())
//                            .map(hit -> hit.getSourceAsMap().get("index"))
//                            .toArray();
//
//
//                    //获取相关度得分数组
//                    float[] scores = new float[titleHits.getHits().length];
//
//                    int i = 0;
//                    for (SearchHit hit : titleHits.getHits()) {
//                        scores[i] = hit.getScore();
//                        i++;
//                    }
//
//
//                    // 计算得分最大值
//                    float maxScore = titleResponse.getHits().getAt(0).getScore();
//                    float minScore = titleResponse.getHits().getAt(99).getScore();
//
//                    // 将得分数组进行归一化排序
//
//                    for (int j = 0; j < scores.length; j++) {
//                        scores[j] =(scores[j]-minScore)/ (maxScore-minScore);
//                    }
//
//                    String[] scoresFormatted = new String[scores.length];
//                    DecimalFormat df = new DecimalFormat("0.00000000");
//                    for (int m = 0; m < scores.length; m++) {
//                        scoresFormatted[m] = df.format(scores[m]);
//                    }
//                    writer.write(id + "\n");
//
//                    //倒序
//                    List<String> list = new ArrayList<>();
//                    for (String score : scoresFormatted) {
//                        list.add(score);
//                    }
//                    Collections.reverse(list);
//                    for (int a = 0; a < scoresFormatted.length; a++) {
//                        scoresFormatted[a] = list.get(a);
//                    }
//
//
//
//                    List<Object> list2 = new ArrayList<>();
//                    for (Object indexV : indexValues) {
//                        list2.add(indexV);
//                    }
//                    Collections.reverse(list2);
//                    for (int b = 0; b < indexValues.length; b++) {
//                        indexValues[b] = list2.get(b);
//                    }
//
//
//
//                    for(int p = 0;p<=99;p++){
//                        writer.write(indexValues[p].toString() + " ");
//                        if(p==99) writer.write("\n");
//
//                    }
//
//
//
//                    for (int q = 0;q<=99;q++){
//                        writer.write(scoresFormatted[q] + " ");
//                        if(q==99) writer.write("\n");
//                    }
//
//
//                }
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        return "success";
//    }




    @GetMapping("/delete")
    public Result<?> deletePolicy(@RequestParam String id) throws IOException {

        return Result.success(elasticsearchService.deletePolicy(id));
    }










    @GetMapping("/recommend")
    public Result<?> policyRecommend(@RequestParam Integer policynum,@RequestParam Integer userid
    ) throws IOException {


        Object[] result = new Object[policynum];
        if(!StpUtil.isLogin()){
            QueryWrapper<History> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("policy_id").groupBy("policy_id").orderByDesc("COUNT(policy_id)").last("LIMIT " + policynum);
            List<History> list = historyMapper.selectList(queryWrapper);

            for (int i = 0; i < policynum; i++) {
                SearchResponse searchResponse = elasticsearchService.getUserHistory(list.get(i).getPolicyId());
                result[i] = searchResponse.getHits().getAt(0).getSourceAsMap();
            }
            return Result.success(result);
        }

        else {
            result = elasticsearchService.recommend(policynum,userid,this.tmap);
            return Result.success(result);
        }
    }







}

