package com.example.es.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.es.entity.Policy;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface PolicyMapper extends BaseMapper<Policy> {
    int deleteByPrimaryKey(String policyIndex);

    int insert(Policy record);

    int insertSelective(Policy record);

    Policy selectByPrimaryKey(String policyIndex);

    int updateByPrimaryKeySelective(Policy record);

    int updateByPrimaryKey(Policy record);
}