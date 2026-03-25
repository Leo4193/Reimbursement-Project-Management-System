package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.SysDict;
import com.student.dto.SysDictDto;
import java.util.List;
import java.util.Map;

public interface SysDictService extends IService<SysDict> {
    
    // 根据类型查询字典列表
    List<SysDict> getDictByType(String dictType);
    
    // 查询所有字典类型（用于前端下拉框）
    List<String> getAllDictTypes();
    
    // 将字典转换为Map格式（方便前端使用）
    Map<String, String> getDictMap(String dictType);
    
    // 新增字典
    boolean addDict(SysDictDto dictDto);
    
    // 修改字典
    boolean updateDict(SysDictDto dictDto);
    
    // 删除字典（逻辑删除或物理删除）
    boolean deleteDict(Long id);
}