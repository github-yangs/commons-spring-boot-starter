package com.yangjq.commons;

import com.yangjq.commons.exception.BizException;
import java.util.List;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局控制器异常处理
 *
 * @author yangjq
 * @since 2022/7/14
 */
@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionAdvice {

  /**
   * 业务异常拦截
   * @param e 业务异常
   */
  @ExceptionHandler(BizException.class)
  public Result bizException(BizException e){
    log.error(e.getMessage());
    return Result.fail(e.getCode(), e.getMessage());
  }

  /**
   * 处理 @RequestParam @PathVariable 等注解标注的参数缺失异常
   */
  @ExceptionHandler(ServletRequestBindingException.class)
  public Result servletRequestBindingException(ServletRequestBindingException e) {
    log.error(e.getMessage());
    return Result.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
  }

  /**
   * 直接在controller的方法里校验参数 抛出的异常
   * e.g. <p>public Result f2(@NotBlank String name, @NotBlank String age)</p>
   * 需要在类上加@Validated注解
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public Result constraintViolationException(ConstraintViolationException e) {
    log.error(e.getMessage());
    return Result.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
  }

  /**
   * 表单请求参数异常
   * e.g. public Result f1(@Validated Query query)
   * 注解@Valid也行，不需要在类上加@Validated注解
   */
  @ExceptionHandler(BindException.class)
  public Result bindException(BindException e) {
    String errorMessage = getErrorMessage(e.getAllErrors());
    log.error(errorMessage);
    return Result.fail(HttpStatus.BAD_REQUEST.value(), errorMessage);
  }

  /**
   * JSON请求参数异常
   * e.g. public Result f1(@Validated @RequestBody Query query)
   * 注解@Valid也行，不需要在类上加@Validated注解
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result methodArgumentNotValidException(MethodArgumentNotValidException e) {
    String errorMessage = getErrorMessage(e.getAllErrors());
    log.error(errorMessage);
    return Result.fail(HttpStatus.BAD_REQUEST.value(), errorMessage);
  }

  private String getErrorMessage(List<ObjectError> errors){
    StringBuilder sb = new StringBuilder();
    for (ObjectError error : errors) {
      sb.append(((FieldError) error).getField()).append(error.getDefaultMessage()).append("; ");
    }
    return sb.toString();
  }

  /**
   * 空指针异常拦截
   */
  @ExceptionHandler(NullPointerException.class)
  public Result nullPointerException(NullPointerException e) {
    log.error("空指针异常");
    e.printStackTrace();
    return Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误");
  }

  /**
   * 未知异常拦截
   */
  @ExceptionHandler(Throwable.class)
  public Result throwable(Throwable e) {
    e.printStackTrace();
    log.error(e.getMessage());
    return Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
  }

}
