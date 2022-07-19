package com.yangjq.commons;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * commons模块配置类
 *
 * @author yangjq
 * @since 2022/7/14
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "commons")
public class CommonsProperties {



}
