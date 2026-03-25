package com.student.controller;

import com.student.entity.SysDict;
import com.student.dto.SysDictDto;
import com.student.service.SysDictService;
import com.student.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dict")
@CrossOrigin(origins = "http://localhost:3000")
public class SysDictController {

    @Autowired
    private SysDictService sysDictService;

    /**
     * 根据类型查询字典列表
     */
    @GetMapping("/list/{type}")
    public Result<List<SysDict>> getDictByType(@PathVariable String type) {
        List<SysDict> dictList = sysDictService.getDictByType(type);
        return Result.success(dictList);
    }

    /**
     * 获取所有字典类型
     */
    @GetMapping("/types")
    public Result<List<String>> getAllDictTypes() {
        List<String> types = sysDictService.getAllDictTypes();
        return Result.success(types);
    }

    /**
     * 获取字典Map（用于前端下拉框）
     */
    @GetMapping("/map/{type}")
    public Result<Map<String, String>> getDictMap(@PathVariable String type) {
        Map<String, String> dictMap = sysDictService.getDictMap(type);
        return Result.success(dictMap);
    }

    /**
     * 分页查询所有字典
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> pageDict(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String dictType) {
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysDict> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysDict> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        if (dictType != null && !dictType.isEmpty()) {
            wrapper.eq(SysDict::getDictType, dictType);
        }
        wrapper.orderByAsc(SysDict::getDictType, SysDict::getSortOrder);
        
        sysDictService.page(page, wrapper);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        
        return Result.success(result);
    }

    /**
     * 查询单个字典
     */
    @GetMapping("/{id}")
    public Result<SysDict> getDictById(@PathVariable Long id) {
        SysDict dict = sysDictService.getById(id);
        return Result.success(dict);
    }

    /**
     * 新增字典
     */
    @PostMapping("/add")
    public Result<String> addDict(@Valid @RequestBody SysDictDto dictDto) {
        boolean success = sysDictService.addDict(dictDto);
        if (success) {
            return Result.success("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }

    /**
     * 修改字典
     */
    @PutMapping("/update")
    public Result<String> updateDict(@Valid @RequestBody SysDictDto dictDto) {
        boolean success = sysDictService.updateDict(dictDto);
        if (success) {
            return Result.success("修改成功");
        } else {
            return Result.error("修改失败");
        }
    }

    /**
     * 删除字典
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteDict(@PathVariable Long id) {
        boolean success = sysDictService.deleteDict(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}