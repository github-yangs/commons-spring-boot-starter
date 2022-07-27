package com.yangjq.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * 通用返回对象
 *
 * @author yangjq
 */
@Getter
@Setter //Feign调用时需要Setter方法设置返回参数
public class Result<T> {

  /**
   * 状态码
   */
  private long code;
  /**
   * 提示信息
   */
  private String message;
  /**
   * 数据封装
   *
   * 如果为null则不序列化
   */
  @JsonInclude(Include.NON_NULL)
  private T data;

  /**
   * fastJson需要用到默认构造器，因此保留一个private的
   */
  private Result(){}

  private Result(long code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  /**
   * 请求成功，无数据返回
   */
  public static Result<List> success() {
    return success(Collections.EMPTY_LIST);
  }

  /**
   * 请求成功并返回数据
   *
   * @param data 获取的数据
   */
  public static <T> Result<T> success(T data) {
    return success("success", data);
  }

  /**
   * 请求成功，返回成功信息
   * @param message 返回信息
   */
  public static Result<List> success(String message) {
    return success(message, Collections.EMPTY_LIST);
  }

  /**
   * 请求成功，返回数据和信息
   * @param message 返回信息
   * @param data 返回数据
   */
  public static <T> Result<T> success(String message, T data) {
    return new Result<>(HttpStatus.OK.value(), message, data);
  }

  /**
   * package-private: 返回只允许成功，失败就抛出异常，然后被全局异常处理类捕获处理
   *
   * 请求失败，返回编号和信息
   */
  public static Result<List> fail(Integer code, String message) {
    return new Result<>(code, message, Collections.EMPTY_LIST);
  }

}
