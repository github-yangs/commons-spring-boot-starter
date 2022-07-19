package com.yangjq.commons.exception;

/**
 * 业务异常枚举类基类
 *
 * @author yangjq
 * @since Created in 15:19 2021/12/13
 */
public interface BizExceptionEnum {

  /**
   * 获取异常编码
   * @return code
   */
  Integer getCode();

  /**
   * 获取异常信息
   * @return message
   */
  String getMessage();

}
