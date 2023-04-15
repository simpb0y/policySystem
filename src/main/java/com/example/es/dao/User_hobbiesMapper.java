package com.example.es.dao;

import com.example.es.entity.User_hobbies;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface User_hobbiesMapper {
    int insert(User_hobbies record);

    int insertSelective(User_hobbies record);
}