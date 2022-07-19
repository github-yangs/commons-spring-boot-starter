package com.yangjq.commons.swagger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Swagger基础配置 Created by macro on 2020/7/16.
 *
 * @author macro
 */
public abstract class BaseSwaggerConfig {

  /**
   * 获取自定义的Swagger配置（每个模块的子类实现）
   * @return SwaggerProperties
   */
  public abstract SwaggerProperties getSwaggerProperties();

  @Bean
  public Docket createRestApi(Environment env) {
    SwaggerProperties swaggerProperties = getSwaggerProperties();
    Docket docket = new Docket(DocumentationType.SWAGGER_2)
        //配置只在测试或开发环境下开启swagger：通过spring.profiles.active判断当前所处环境
        .enable(env.acceptsProfiles(Profiles.of("dev", "test")))
        //配置文档基本信息
        .apiInfo(apiInfo(swaggerProperties))
        //去掉response中的401，403，404等状态码
        .useDefaultResponseMessages(false)
        //配置怎么扫描接口
        .select()
        //扫描指定路径下的接口
        .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getApiBasePackage()))
        //可以理解为全局配置的@ApiIgnore，用于忽略符合条件的接口；any()：任何路径都满足条件
        .paths(PathSelectors.any())
        .build();
    if (swaggerProperties.isEnableSecurity()) {
      docket
          .securitySchemes(securitySchemes())
          .securityContexts(securityContexts());
    }
    return docket;
  }

  private ApiInfo apiInfo(SwaggerProperties swaggerProperties) {
    return new ApiInfoBuilder()
        .title(swaggerProperties.getTitle())
        .description(swaggerProperties.getDescription())
        .contact(new Contact(swaggerProperties.getContactName(), swaggerProperties.getContactUrl(), swaggerProperties.getContactEmail()))
        .version(swaggerProperties.getVersion())
        .build();
  }

  private List<SecurityScheme> securitySchemes() {
    //设置请求头信息
    List<SecurityScheme> result = new ArrayList<>();
    ApiKey apiKey = new ApiKey("Authorization", "Authorization", "header");
    result.add(apiKey);
    return result;
  }

  private List<SecurityContext> securityContexts() {
    //设置需要登录认证的路径
    List<SecurityContext> result = new ArrayList<>();
    result.add(getContextByPath("/*/.*"));
    return result;
  }

  private SecurityContext getContextByPath(String pathRegex) {
    return SecurityContext.builder()
        .securityReferences(defaultAuth())
        .forPaths(PathSelectors.regex(pathRegex))
        .build();
  }

  private List<SecurityReference> defaultAuth() {
    List<SecurityReference> result = new ArrayList<>();
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    result.add(new SecurityReference("Authorization", authorizationScopes));
    return result;
  }

  @Bean
  public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
    return new BeanPostProcessor() {

      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
          customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
        }
        return bean;
      }

      private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
        List<T> copy = mappings.stream()
            .filter(mapping -> mapping.getPatternParser() == null)
            .collect(Collectors.toList());
        mappings.clear();
        mappings.addAll(copy);
      }

      @SuppressWarnings("unchecked")
      private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
        try {
          Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
          field.setAccessible(true);
          return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      }
    };
  }

}
