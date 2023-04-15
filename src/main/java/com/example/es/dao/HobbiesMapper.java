package com.example.es.dao;

import com.example.es.entity.Hobbies;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface HobbiesMapper {
    int deleteByPrimaryKey(String id);

    int insert(Hobbies record);

    int insertSelective(Hobbies record);

    Hobbies selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Hobbies record);

    int updateByPrimaryKey(Hobbies record);
}