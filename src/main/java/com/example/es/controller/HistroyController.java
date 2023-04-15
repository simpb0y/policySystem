package com.example.es.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.es.common.Result;
import com.example.es.dao.HistoryMapper;
import com.example.es.dao.PolicyMapper;
import com.example.es.dao.UserMapper;
import com.example.es.entity.History;
import com.example.es.service.ElasticsearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/history")
public class HistroyController {

    @Autowired
    private HistoryMapper historyMapper;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PolicyMapper policyMapper;

//    @GetMapping("/add")
//    public Result<?> addHistory() throws IOException {
//
//
//        String[] industry = {"农、林、牧、渔业","住宿、餐饮业","居民服务和其他服务业","水利、环境和公共设施管理业","建筑业","教育行业","公共管理和社会组织",
//                "制造业","房地产业","卫生、社会保障和社会福利业","租赁和商务服务业","批发和零售业","交通运输、仓储和邮政业","文化、体育和娱乐业",
//                "电力、燃气及水的生产和供应业","金融业","科学研究、技术服务和地质勘查业","信息传输、计算机服务和软件业","其他"};
//
//        List<String> industrylist = Arrays.asList(industry);
//
//
//
//        //22个领域
//        String[] area = {"商贸流通","气象服务","科技创新","教育文化","资源能源","医疗卫生","安全生产","信用服务",
//                "城建住房","财税金融","市场监督","生态环境","生活服务","社会救助","社保就业","法律服务","地理空间",
//                "公共安全","工业农业","交通运输","机构团体","其他"};
//
//        List<String> arealist = Arrays.asList(area);
//
//
//        String[] province = {"110000","120000","130000","140000","150000","210000","220000","230000","310000","320000",
//                "330000","340000","350000","360000","370000","410000","420000","430000","440000","450000","460000",
//                "500000","510000","520000","530000","540000","610000","620000","630000","640000","650000","710000","810000","820000"};
//
//
//        String[] provinceName = {"北京市", "天津市", "河北省", "山西省", "内蒙古自治区", "辽宁省", "吉林省", "黑龙江省", "上海市", "江苏省",
//                "浙江省", "安徽省", "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省", "广东省", "广西壮族自治区", "海南省", "重庆市",
//                "四川省", "贵州省", "云南省", "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "新疆维吾尔自治区","台湾省","香港特别行政区","澳门特别行政区"};
//
//        List<String> provincelist = Arrays.asList(provinceName);
//
//
//        File titleFile = new File("C:\\Users\\ASUS\\Desktop\\es\\history3.txt");
//
////        FileWriter writer = new FileWriter(titleFile, true);
//        Writer writer = new OutputStreamWriter(new FileOutputStream(titleFile), "GB18030");
//
//
//        for(Integer i=2;i<=1001;i++){
//
//            System.out.println("i = " + i);
//            //输出用户id
//            writer.write(i+" ");
//
//            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("id",i);
//            User user = userMapper.selectOne(queryWrapper);
//            String[] userarea = userMapper.selectOne(queryWrapper).getArea().split(";");
//
//            System.out.println("userarea = " + userarea[1]);
//
//            //输出用户职业
//            int industryindex = industrylist.indexOf(user.getIndustry());
//            writer.write("1" + " " + industryindex + " ");
//
//            int provinceindex = provincelist.indexOf(user.getProvince());
//            writer.write("1" + " " + provinceindex + " ");
//
//            //输出用户领域
//            writer.write(userarea.length + " ");
//            for(int a = 0 ;a < userarea.length;a++){
//                int areaindex = arealist.indexOf(userarea[a]);
//                writer.write(areaindex + " ");
//            }
//
//
//
//
//            String searchword = userMapper.selectOne(queryWrapper).getProvince()+userMapper.selectOne(queryWrapper).getIndustry();
//
//            SearchHits hits = elasticsearchService.addHistory(searchword).getHits();
//
//            System.out.println("hits.getTotalHits() = " + hits.getTotalHits());
//
//            writer.write("100" + " ");
//            for (int j=0;j<100;j++){
//                History history = new History();
//                int y = j;
//
//                Map<String,Object> map = hits.getAt(y).getSourceAsMap();
//
//
//                writer.write(map.get("index").toString() + " ");
//
//
//                history.setPolicyId(map.get("POLICY_ID").toString());
//                history.setUserId(i.toString());
//                historyMapper.insert(history);
//
//            }
//
//            writer.write("\n");
//        }
//
//
//
//
//
//
//        return Result.success();
//
//
//    }

    @GetMapping("/deleteAll")
    public Result<?> deleteAll(){
       return Result.success(historyMapper.delete(null));
    }


    /**
     * 用户历史记录
     * @param id
     * @return
     */
    @GetMapping("/user/{userid}")
    public Result<?> userHistory(@PathVariable String userid) throws IOException {

        QueryWrapper<History> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userid).orderByDesc("timestamp");
        List<History> list = historyMapper.selectList(queryWrapper);
        System.out.println("list = " + list);


        Object[] result = new Object[list.size()];

        for(int i=0;i<list.size();i++){

            SearchResponse searchResponse = elasticsearchService.getUserHistory(list.get(i).getPolicyId());
            result[i]=searchResponse.getHits().getAt(0).getSourceAsMap();
        }


        return Result.success(result);
    }

    /**
     * 频率最高用户
     * @param usernum
     * @return
     */

    @GetMapping("/topuser")
    public Result<?> topuser(@RequestParam Timestamp[] time,
                             @RequestParam Boolean needtime,
                             @RequestParam Integer usernum){
        QueryWrapper<History> queryWrapper = new QueryWrapper<>();
        if(needtime){queryWrapper.between(needtime,"timestamp",time[0],time[1]);}

        queryWrapper.select("user_id ,count(*) as count").groupBy("user_id").orderByDesc("count").last("limit "+usernum.toString());




        return Result.success(historyMapper.selectMaps(queryWrapper));
    }

    @GetMapping("/toppolicy")
    public Result<?> toppolicy(@RequestParam Timestamp[] time,
                               @RequestParam Boolean needtime,
                               @RequestParam Integer policynum) throws IOException {
        QueryWrapper<History> queryWrapper = new QueryWrapper<>();
        if(needtime){queryWrapper.between(needtime,"timestamp",time[0],time[1]);}
        queryWrapper.select("policy_id ,count(*) as count").groupBy("policy_id").orderByDesc("count").last("limit "+policynum.toString());


        return Result.success(historyMapper.selectMaps(queryWrapper));
    }

    @GetMapping("/addindex")
    public Result<?> addindex() throws IOException {

        return Result.success(elasticsearchService.addindex());
    }
}
