package com.yangjq.commons.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author: zhouss
 * @description: redis配置
 * @create: 2022-01-03 15:41
 **/
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

  private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

  @Autowired
  private JedisConnectionFactory jedisConnectionFactory;

  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    return (target, method, params) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(target.getClass().getName());
      sb.append(":");
      sb.append(method.getName());
      for (Object obj : params) {
        sb.append(":" + String.valueOf(obj));
      }
      String rsToUse = String.valueOf(sb);
      logger.info("自动生成Redis Key -> [{}]", rsToUse);
      return rsToUse;
    };
  }


  @Bean
  @Override
  public CacheManager cacheManager() {
    logger.info("Initializing Redis RedisCacheManager Successful");
    RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
        .RedisCacheManagerBuilder
        .fromConnectionFactory(jedisConnectionFactory);
    return builder.build();
  }


  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      JedisConnectionFactory jedisConnectionFactory) {
    Assert.notNull(jedisConnectionFactory, "JedisConnectionFactory must not be null");
    Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(
        Object.class);
    ObjectMapper om = new ObjectMapper();
    om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    jackson2JsonRedisSerializer.setObjectMapper(om);
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
    redisTemplate.setConnectionFactory(jedisConnectionFactory);
    RedisSerializer stringSerializer = new StringRedisSerializer();
    redisTemplate.setKeySerializer(stringSerializer);
    redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
    redisTemplate.setHashKeySerializer(stringSerializer);
    redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }


  @Bean
  @Override
  public CacheErrorHandler errorHandler() {
    // 异常处理，当Redis发生异常时，打印日志，但是程序正常走
    logger.info("Initializing Redis CacheErrorHandler Successful");
    CacheErrorHandler cacheErrorHandler = new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
        logger.error("Redis occur handleCacheGetError：key -> [{}]", key, e);
      }

      @Override
      public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
        logger.error("Redis occur handleCachePutError：key -> [{}]；value -> [{}]", key, value, e);
      }

      @Override
      public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
        logger.error("Redis occur handleCacheEvictError：key -> [{}]", key, e);
      }

      @Override
      public void handleCacheClearError(RuntimeException e, Cache cache) {
        logger.error("Redis occur handleCacheClearError：", e);
      }
    };
    return cacheErrorHandler;
  }

  @ConfigurationProperties
  class DataJedisProperties {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.database}")
    private int database;
    @Value("${spring.redis.timeout}")
    private int timeout;
    @Value("${spring.redis.jedis.pool.maxIdle}")
    private int maxIdle;
    @Value("${spring.redis.jedis.pool.maxWaitMillis}")
    private long maxWaitMillis;

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
      logger.info(
          "Initializing Redis JedisConnectionFactory Successful，host -> [{}]；port -> [{}]]；database->[{}]; timeout->[{}]; maxIdle->[{}], maxWait->[{}]",
          host, port, database, timeout, maxIdle, maxWaitMillis);
      JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
      jedisPoolConfig.setMaxIdle(maxIdle);
      jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
      JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder()
          .usePooling().poolConfig(jedisPoolConfig).and().readTimeout(Duration.ofMillis(timeout))
          .build();
      RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
      redisStandaloneConfiguration.setHostName(host);
      redisStandaloneConfiguration.setPort(port);
      redisStandaloneConfiguration.setDatabase(database);
      redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
      return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }
  }
}

