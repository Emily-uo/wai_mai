package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理逻辑
 */
//切面=切入点+通知

@Aspect  //AOP
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点(对哪些类的哪些方法上加入Autofill注解的 实施拦截)
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知中进行公共字段的赋值
     */
    @Before(value = "autoFillPointCut()&&@annotation(autoFill)")  //指定切入点
    public void autoFill(JoinPoint joinPoint, AutoFill autoFill){
        log.info("开始进行公共字段自动填充...");

        //获取到当前被拦截的方法上的数据库操作类型
       OperationType operationType = autoFill.value();

        //获取到当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }

        Object entity=args[0];
        if(entity==null){
            return;
        }

        //准备赋值的数据(时间，登录用户id)
        LocalDateTime now=LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();

        try {
            if (operationType == OperationType.INSERT) {
                setField(entity, AutoFillConstant.SET_CREATE_TIME, now, LocalDateTime.class);
                setField(entity, AutoFillConstant.SET_CREATE_USER, currentId, Long.class);
                setField(entity, AutoFillConstant.SET_UPDATE_TIME, now, LocalDateTime.class);
                setField(entity, AutoFillConstant.SET_UPDATE_USER, currentId, Long.class);
            } else if (operationType == OperationType.UPDATE) {
                setField(entity, AutoFillConstant.SET_UPDATE_TIME, now, LocalDateTime.class);
                setField(entity, AutoFillConstant.SET_UPDATE_USER, currentId, Long.class);
            }
        } catch (Exception e) {
            log.error("自动填充公共字段出错", e);
        }
    }

    /**
     * 通用反射赋值方法
     */
    private void setField(Object entity, String methodName, Object value, Class<?> paramType) throws Exception {
        Method method = entity.getClass().getDeclaredMethod(methodName, paramType); //获取反射方法对象
        method.setAccessible(true);   //设置私有方法可访问
        method.invoke(entity, value);  //执行方法（调用setter）
    }

}

