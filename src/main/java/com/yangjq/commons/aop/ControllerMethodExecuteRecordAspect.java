package com.yangjq.commons.aop;

import io.swagger.annotations.ApiOperation;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 记录方法运行的情况
 *
 * 切面也需要被Spring Bean管理才能起作用
 *
 * @author yangjq
 * @since 2022/7/15
 */
@Aspect
@Component
@Slf4j
public class ControllerMethodExecuteRecordAspect {

  private final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

  /**
   * 匹配 com包及其子包下以Controller结尾的类的所有公共方法
   * 参考资料：https://blog.csdn.net/java_green_hand0909/article/details/90238242
   */
  @Around("execution(public * com.jiayi..*Controller.*(..))")
  public Object around(ProceedingJoinPoint joinPoint) throws  Throwable {

    threadLocal.set(System.nanoTime());
    Object result = joinPoint.proceed();
    //下面的代码不应该影响正常的执行
    try {
      //获取请求时间
      long methodTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - threadLocal.get());
      //获取请求方法
      String methodName = joinPoint.getSignature().toShortString();
      //获取请求参数
      StringBuilder sb = new StringBuilder();
      Object[] args = joinPoint.getArgs();
      //joinPoint.getArgs()允许传null参数？？？
      for (int i=0; i<args.length; i++) {
        if (args[i] != null){
          sb.append("参数"+i+": ").append(args[i].toString()).append(" ");
        }else {
          sb.append("参数"+i+": ").append("null").append(" ");
        }
      }
      String parameters = sb.toString();
      //获取方法描述
      String methodDesc;
      Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
      if (method.isAnnotationPresent(ApiOperation.class)){
        methodDesc = method.getAnnotation(ApiOperation.class).value();
      }else {
        methodDesc = "";
      }

      if (methodTime > 1000){
        //方法运行时间大于1s则需要警告
        log.warn("方法运行记录---> 方法名:{}; 方法描述:{}; 参数列表->{}; 花费时间:{}ms; 请求IP:{}", methodName, methodDesc, parameters, methodTime, getRequestIp());
      }else {
        log.info("方法运行记录---> 方法名:{}; 方法描述:{}; 参数列表->{}; 花费时间:{}ms; 请求IP:{}", methodName, methodDesc, parameters, methodTime, getRequestIp());
      }
    } catch (Exception e){
      log.error("Controller记录运行异常: "+e.getMessage());
      e.printStackTrace();
    } finally {
      threadLocal.remove();
    }
    return result;
  }

  /**
   * 请求IP如果配置了代理服务器则会无效
   * @return
   */
  private String getRequestIp(){
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    HttpServletRequest request = requestAttributes.getRequest();
    return request.getRemoteAddr();
  }
}
