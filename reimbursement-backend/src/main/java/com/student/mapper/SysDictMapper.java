package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
    
    // 根据类型查询字典列表
    @Select("SELECT * FROM sys_dict WHERE dict_type = #{dictType} AND status = 1 ORDER BY sort_order")
    List<SysDict> findByType(String dictType);
}