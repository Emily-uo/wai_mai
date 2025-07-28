package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DishService {

    /**
     * 新增菜品 和 对应的口味数据
     * @param dishDTO
     */
    public void  saveWithFlavor(DishDTO dishDTO); //新增不需要返回数据

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品和对应的口味数据
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
