package com.example.es.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.es.common.Result;
import com.example.es.dao.UserMapper;
import com.example.es.entity.User;
import com.example.es.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired(required = false)
    private UserMapper userMapper;



    @Override
    public Result<User> doLogin(String username, String password) {



        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("username",username);


        if(userMapper.selectOne(queryWrapper).getPassword().equals(password)) {

            User user = userMapper.selectOne(queryWrapper);
            Integer uid = user.getId();

            StpUtil.login(uid);
            return Result.success(user);
        }
        return Result.error("401","登陆失败");
    }

    @Override
    public Result<?> doLogout() {
        StpUtil.logout();
        return Result.success();
    }
}
