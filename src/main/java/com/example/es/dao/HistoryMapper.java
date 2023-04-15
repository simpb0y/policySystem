package com.example.es.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.es.entity.History;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HistoryMapper extends BaseMapper<History> {
    int insert(History record);

    int insertSelective(History record);
}