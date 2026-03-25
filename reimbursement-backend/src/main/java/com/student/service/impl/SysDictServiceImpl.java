package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.SysDict;
import com.student.dto.SysDictDto;
import com.student.mapper.SysDictMapper;
import com.student.service.SysDictService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Override
    public List<SysDict> getDictByType(String dictType) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dictType)
               .eq(SysDict::getStatus, 1)
               .orderByAsc(SysDict::getSortOrder);
        return this.list(wrapper);
    }

    @Override
    public List<String> getAllDictTypes() {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysDict::getDictType)
               .groupBy(SysDict::getDictType);
        return this.list(wrapper).stream()
                .map(SysDict::getDictType)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getDictMap(String dictType) {
        List<SysDict> dictList = getDictByType(dictType);
        Map<String, String> map = new HashMap<>();
        for (SysDict dict : dictList) {
            map.put(dict.getDictCode(), dict.getDictValue());
        }
        return map;
    }

    @Override
    @Transactional
    public boolean addDict(SysDictDto dictDto) {
        // 检查编码是否已存在
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dictDto.getDictType())
               .eq(SysDict::getDictCode, dictDto.getDictCode());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("该字典编码已存在");
        }
        
        SysDict dict = new SysDict();
        BeanUtils.copyProperties(dictDto, dict);
        dict.setCreateTime(LocalDateTime.now());
        return this.save(dict);
    }

    @Override
    @Transactional
    public boolean updateDict(SysDictDto dictDto) {
        if (dictDto.getId() == null) {
            throw new RuntimeException("ID不能为空");
        }
        
        // 检查编码是否已存在（排除自己）
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dictDto.getDictType())
               .eq(SysDict::getDictCode, dictDto.getDictCode())
               .ne(SysDict::getId, dictDto.getId());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("该字典编码已存在");
        }
        
        SysDict dict = new SysDict();
        BeanUtils.copyProperties(dictDto, dict);
        return this.updateById(dict);
    }

    @Override
    @Transactional
    public boolean deleteDict(Long id) {
        // 物理删除（简单起见先物理删除，实际项目可能用逻辑删除）
        return this.removeById(id);
    }
}