package com.yangjq.commons.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常抽象类
 *
 * spring 对于 RuntimeException 异常才会进行事务回滚
 *
 * message属性在父类中
 *
 * @author yangjq
 * @since Created in 15:15 2021/12/13
 */
@Getter
@Setter
public class BizException extends RuntimeException{

  private Integer code;

  public BizException(BizExceptionEnum exceptionEnum) {
    super(exceptionEnum.getMessage());
    this.code = exceptionEnum.getCode();
  }
}
