package com.example.es.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.es.common.Result;
import com.example.es.dao.UserMapper;
import com.example.es.entity.User;
import com.example.es.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@CrossOrigin
@RestController
@SaIgnore
@RequestMapping("/user")
public class UserController {



    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * 登陆
     * @param username
     * @param password
     * @return
     */
    @SaIgnore
    @GetMapping("/login")

    public Result<?> doLogin(@RequestParam String username, @RequestParam String password) {
        return userService.doLogin(username,password);
    }

    /**
     * 登出
     * @return
     */
    @SaIgnore
    @GetMapping("/logout")

    public Result<?> doLogout() {
        return Result.success(userService.doLogout());
    }

    /**
     * 登陆验证（判断是否已登陆）
     * @return
     */
    @SaIgnore
    @GetMapping("/loginVerify")

    public Result<?> loginVerify(){
        return Result.success(StpUtil.isLogin());
    }

    //测试用
    @SaCheckLogin
    @GetMapping("/info")
    public Result<?> info(){
        return Result.success(userMapper.selectList(null));
    }

    /**
     * 新增用户
     * @param user
     * @return
     */
    @PostMapping ("/add")
    public Result<?> save(@RequestBody User user){

        String[] industry = {"农、林、牧、渔业","住宿、餐饮业","居民服务和其他服务业","水利、环境和公共设施管理业","建筑业","教育行业","公共管理和社会组织",
                "制造业","房地产业","卫生、社会保障和社会福利业","租赁和商务服务业","批发和零售业","交通运输、仓储和邮政业","文化、体育和娱乐业",
                "电力、燃气及水的生产和供应业","金融业","科学研究、技术服务和地质勘查业","信息传输、计算机服务和软件业","其他"};

        List<String> industrylist = Arrays.asList(industry);
                //22个领域
        String[] area = {"商贸流通","气象服务","科技创新","教育文化","资源能源","医疗卫生","安全生产","信用服务",
                "城建住房","财税金融","市场监督","生态环境","生活服务","社会救助","社保就业","法律服务","地理空间",
                "公共安全","工业农业","交通运输","机构团体","其他"};

        List<String> arealist = Arrays.asList(area);


        String[] province = {"110000","120000","130000","140000","150000","210000","220000","230000","310000","320000",
                "330000","340000","350000","360000","370000","410000","420000","430000","440000","450000","460000",
                "500000","510000","520000","530000","540000","610000","620000","630000","640000","650000","710000","810000","820000"};


        String[] provinceName = {"北京市", "天津市", "河北省", "山西省", "内蒙古自治区", "辽宁省", "吉林省", "黑龙江省", "上海市", "江苏省",
                "浙江省", "安徽省", "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省", "广东省", "广西壮族自治区", "海南省", "重庆市",
                "四川省", "贵州省", "云南省", "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "新疆维吾尔自治区","台湾省","香港特别行政区","澳门特别行政区"};

        List<String> provincelist = Arrays.asList(provinceName);
        user.setIndustryId(String.valueOf(industrylist.indexOf(user.getIndustry())));
        user.setProvinceId(String.valueOf(provincelist.indexOf(user.getProvince())));
        List area_list = Arrays.asList(user.getArea().split(";"));
        String area_id = String.valueOf(arealist.indexOf(area_list.get(0)));
        for (int i = 1; i < area_list.size(); i++) {
            area_id = area_id  + ";" + arealist.indexOf(area_list.get(i));
        }
        user.setAreaId(area_id);

        return Result.success(userService.save(user));
    }

    /**
     * 身份码
     * @return
     */
    @SaCheckRole("boss")
    @GetMapping("/role")
    public Result<?> role(){return Result.success(StpUtil.getRoleList());}

    /**
     * 更新
     * @param user
     * @return
     */
    @PostMapping("/update")
    public Result<?> update(@RequestBody User user){

        String[] industry = {"农、林、牧、渔业","住宿、餐饮业","居民服务和其他服务业","水利、环境和公共设施管理业","建筑业","教育行业","公共管理和社会组织",
                "制造业","房地产业","卫生、社会保障和社会福利业","租赁和商务服务业","批发和零售业","交通运输、仓储和邮政业","文化、体育和娱乐业",
                "电力、燃气及水的生产和供应业","金融业","科学研究、技术服务和地质勘查业","信息传输、计算机服务和软件业","其他"};

        List<String> industrylist = Arrays.asList(industry);
        //22个领域
        String[] area = {"商贸流通","气象服务","科技创新","教育文化","资源能源","医疗卫生","安全生产","信用服务",
                "城建住房","财税金融","市场监督","生态环境","生活服务","社会救助","社保就业","法律服务","地理空间",
                "公共安全","工业农业","交通运输","机构团体","其他"};

        List<String> arealist = Arrays.asList(area);


        String[] province = {"110000","120000","130000","140000","150000","210000","220000","230000","310000","320000",
                "330000","340000","350000","360000","370000","410000","420000","430000","440000","450000","460000",
                "500000","510000","520000","530000","540000","610000","620000","630000","640000","650000","710000","810000","820000"};


        String[] provinceName = {"北京市", "天津市", "河北省", "山西省", "内蒙古自治区", "辽宁省", "吉林省", "黑龙江省", "上海市", "江苏省",
                "浙江省", "安徽省", "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省", "广东省", "广西壮族自治区", "海南省", "重庆市",
                "四川省", "贵州省", "云南省", "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "新疆维吾尔自治区","台湾省","香港特别行政区","澳门特别行政区"};

        List<String> provincelist = Arrays.asList(provinceName);
        user.setIndustryId(String.valueOf(industrylist.indexOf(user.getIndustry())));
        user.setProvinceId(String.valueOf(provincelist.indexOf(user.getProvince())));
        List area_list = Arrays.asList(user.getArea().split(";"));
        String area_id = String.valueOf(arealist.indexOf(area_list.get(0)));
        for (int i = 1; i < area_list.size(); i++) {
            area_id = area_id  + ";" + arealist.indexOf(area_list.get(i));
        }
        user.setAreaId(area_id);

        return Result.success(userService.saveOrUpdate(user));
    }

    /**
     * 分页查询
     * @param current
     * @param size
     * @return
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam Integer current,@RequestParam Integer size ){
        return Result.success(userMapper.selectPage(new Page<User>(current,size).addOrder(OrderItem.asc("id")),null));
    }

    /**
     * 身份码
     * @param usersName
     * @param usersPass
     * @return
     */
    @SaIgnore
    @GetMapping("/getRole")
    public Result<?> getRole(@RequestParam String usersName,@RequestParam String usersPass){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",usersName).eq("password",usersPass);
        return Result.success(userMapper.selectOne(queryWrapper).getRole());
    }


//    @GetMapping("/addUsers")
//    public Result<?> addUsers(){
//
//        //19个行业
//        String[] industry = {"农、林、牧、渔业","住宿、餐饮业","居民服务和其他服务业","水利、环境和公共设施管理业","建筑业","教育行业","公共管理和社会组织",
//                "制造业","房地产业","卫生、社会保障和社会福利业","租赁和商务服务业","批发和零售业","交通运输、仓储和邮政业","文化、体育和娱乐业",
//                "电力、燃气及水的生产和供应业","金融业","科学研究、技术服务和地质勘查业","信息传输、计算机服务和软件业","其他"};
//        //22个领域
//        String[] area = {"商贸流通","气象服务","科技创新","教育文化","资源能源","医疗卫生","安全生产","信用服务",
//                "城建住房","财税金融","市场监督","生态环境","生活服务","社会救助","社保就业","法律服务","地理空间",
//                "公共安全","工业农业","交通运输","机构团体","其他"};
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
//
//        for (int i = 0;i<1000;i++){
//
//            //区域字符串拼接
//            String areas = null;
//
//            Integer areas_num = new Random().nextInt(9)+1;
//
//            areas = area[new Random().nextInt(22)];
//
//            while(areas_num>0){
//                areas = areas + ";" + area[new Random().nextInt(22)];
//                areas_num--;
//            }
//
//
//            User user = new User();
//            user.setId(i+2);
//            user.setUsername("normaluser"+i);
//            user.setPassword("123456");
//            user.setRole("user");
//            user.setAge(String.valueOf(new Random().nextInt(60)+18));
//            user.setSex(new Random().nextBoolean() ? "男" : "女");
//            user.setIndustry(industry[new Random().nextInt(19)]);
//            user.setArea(
//                    areas
//            );
//            user.setProvince(provinceName[new Random().nextInt(34)]);
//
//            userService.saveOrUpdate(user);
//        }
//        return Result.success();
//    }
//
//
//    @GetMapping("/deleteall")
//    public Result<?> deleteAll(){
//        return Result.success(userMapper.delete(null));
//    }
//
//    @GetMapping("/addmap")
//    public Result<?> addhistory(){
//
//        String[] industry = {"农、林、牧、渔业","住宿、餐饮业","居民服务和其他服务业","水利、环境和公共设施管理业","建筑业","教育行业","公共管理和社会组织",
//                "制造业","房地产业","卫生、社会保障和社会福利业","租赁和商务服务业","批发和零售业","交通运输、仓储和邮政业","文化、体育和娱乐业",
//                "电力、燃气及水的生产和供应业","金融业","科学研究、技术服务和地质勘查业","信息传输、计算机服务和软件业","其他"};
//        List<String> industrylist = Arrays.asList(industry);
//        String[] area = {"商贸流通","气象服务","科技创新","教育文化","资源能源","医疗卫生","安全生产","信用服务",
//                "城建住房","财税金融","市场监督","生态环境","生活服务","社会救助","社保就业","法律服务","地理空间",
//                "公共安全","工业农业","交通运输","机构团体","其他"};
//        List<String> arealist = Arrays.asList(area);
//        String[] provinceName = {"北京市", "天津市", "河北省", "山西省", "内蒙古自治区", "辽宁省", "吉林省", "黑龙江省", "上海市", "江苏省",
//                "浙江省", "安徽省", "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省", "广东省", "广西壮族自治区", "海南省", "重庆市",
//                "四川省", "贵州省", "云南省", "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "新疆维吾尔自治区","台湾省","香港特别行政区","澳门特别行政区"};
//        List<String> provincelist = Arrays.asList(provinceName);
//
//
//
//        for (Integer i = 0; i < industrylist.size(); i++) {
//            UpdateWrapper<User> updateWrapper1 = new UpdateWrapper<>();
//            updateWrapper1.set("industry_id",i.toString()).eq("industry",industrylist.get(i));
//            userMapper.update(null,updateWrapper1);
//        }
//
//
//        for (Integer i = 0; i < provincelist.size(); i++) {
//            UpdateWrapper<User> updateWrapper2 = new UpdateWrapper<>();
//            updateWrapper2.set("province_id",i.toString()).eq("province",provincelist.get(i));
//            userMapper.update(null,updateWrapper2);
//        }
//
//        List<User> userlist = userMapper.selectList(null);
//
//        for (int i = 0; i < userlist.size(); i++) {
//            List area_list = Arrays.asList(userlist.get(i).getArea().split(";"));
//            List area_id_list = new ArrayList();
//            for (int j = 0; j < area_list.size(); j++) {
//                area_id_list.add(arealist.indexOf(area_list.get(j)));
//
//            }
//            String area_id_string = area_id_list.get(0).toString();
//            for (int j = 1; j < area_id_list.size(); j++) {
//                area_id_string = area_id_string + ";" +area_id_list.get(j).toString();
//            }
//            Integer userid = userlist.get(i).getId();
//            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
//            updateWrapper.set("area_id",area_id_string).eq("id",userid);
//            userMapper.update(null,updateWrapper);
//        }
//
//
//
//
//        return Result.success();
//    }



}