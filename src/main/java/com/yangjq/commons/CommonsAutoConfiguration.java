package com.yangjq.commons;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

/**
 * commons模块自动配置类
 *
 * ConfigurationProperties 注解不包含@Component注解，因此不会被spring管理。需要
 * 在@EnableConfigurationProperties注解中配置才能被spring管理
 *
 * @author yangjq
 * @since 2022/7/14
 */
@Configuration
@ComponentScan("com.yangjq.commons")
@EnableConfigurationProperties(CommonsProperties.class)
public class CommonsAutoConfiguration {

  /**
   * 配置Validator
   */
  @Bean
  public Validator validator() {
    ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
        .configure()
        // 快速失败模式
        .failFast(true)
        .buildValidatorFactory();
    return validatorFactory.getValidator();
  }

  /**
   * 配置fastJson
   */
  @Bean
  public HttpMessageConverters fastJsonHttpMessageConverters() {
    // 1.定义一个converters转换消息的对象
    FastJsonHttpMessageConverter fastJsonConverter = new FastJsonHttpMessageConverter();
    // 2.配置fastJson
    FastJsonConfig fastJsonConfig = new FastJsonConfig();
    fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
    fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteMapNullValue); //结果格式化
    fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteNullListAsEmpty); //List字段如果为null,输出为[],而非null
    fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteNullStringAsEmpty); //字符类型字段如果为null,输出为”“,而非null
    fastJsonConverter.setFastJsonConfig(fastJsonConfig);
    //处理中文乱码
    List<MediaType> fastJsonMediaTypes = new ArrayList<>();
    fastJsonMediaTypes.add(MediaType.APPLICATION_JSON);
    fastJsonConverter.setSupportedMediaTypes(fastJsonMediaTypes);
    return new HttpMessageConverters(fastJsonConverter);

  }

}
