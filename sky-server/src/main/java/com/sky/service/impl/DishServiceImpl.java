package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
/**
 * 菜品管理业务实现类
 * 负责菜品和口味的新增操作
 */
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;


    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();  //直接new出来时空的，要对dish相关属性对象赋值
        BeanUtils.copyProperties(dishDTO,dish);

        //向菜品表插入1条数据
        dishMapper.insert(dish); //不用传dishDTO（里面包含了口味数据）

        Long dishId = dish.getId(); //获取菜品id （获取insert语句生成的主键值）

        //向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors(); //拿到口味的集合
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(flavor->{
                flavor.setDishId(dishId); //设置口味对应的菜品id
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);  //批量插入


        }

    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除菜品和对应的口味数据
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除--是否存在起售中的菜品
        for (Long id : ids) {
           Dish dish=dishMapper.getById(id);
           if(dish.getStatus()== StatusConstant.ENABLE){
               //如果菜品状态为1，说明是起售中的菜品，不能删除
               throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
           }
        }

        //判断当前菜品是否能够删除--是否被套餐关联
        List<Long> SetmealIds = setmealDishMapper.getDishIdsBySetmealIds(ids);
        if(SetmealIds!=null&&SetmealIds.size()>0){
            //如果菜品被套餐关联，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //批量删除表中菜品数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }


    }


}
