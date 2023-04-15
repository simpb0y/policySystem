package com.example.es.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.es.common.Result;
import com.example.es.entity.User;

public interface UserService extends IService<User> {

    Result<User> doLogin(String username, String password);

    Result<?> doLogout();
}
