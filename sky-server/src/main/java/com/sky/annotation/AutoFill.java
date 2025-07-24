package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 */
@Target(ElementType.METHOD) //指定注解能在哪里使用（此处只能加在方法上面）
@Retention(RetentionPolicy.RUNTIME) //可以理解为保留时间(生命周期)
public @interface AutoFill {
    //数据库操作类型：update insert（前面分析公共字段在哪里设置得出）
    OperationType value();

}
