package com.yangjq.commons.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author yangjq
 * @Description：
 * @Date：Created in 17:01 2022/1/7
 * @Modified by:
 */
@Component
public final class RedisUtil {

  /**
   * 不能用@Autowired，因为没有RedisTemplate<String, Object>返回的bean
   * 这里我们引入的其实还是RedisTemplate<Object, Object>
   */
  @Resource
  private RedisTemplate<String, Object> redisTemplate;

  /**
   * 普通缓存放入
   * @param key
   * @param val 存入的对象其实会被序列化为JsonObject对象
   * @return
   */
  public boolean set(String key, Object val){
    try {
      redisTemplate.opsForValue().set(key, val);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 普通缓存放入并设置过期时间
   * @param key
   * @param val
   * @param expireTime 时间<0将设置无限期
   * @return
   */
  public boolean set(String key, Object val, long expireTime){
    try {
      if (expireTime>0){
        redisTemplate.opsForValue().set(key, val, expireTime, TimeUnit.SECONDS);
      }else {
        redisTemplate.opsForValue().set(key, val);
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 普通缓存放入：只有key不存在的时候才放入
   * @param key
   * @param val
   * @return
   */
  public boolean setIfAbsent(String key, Object val){
    try {
      redisTemplate.opsForValue().setIfAbsent(key, val);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 普通缓存获取
   * @param key
   * @return
   */
  public String get(String key){
    return (String)redisTemplate.opsForValue().get(key);
  }

  /**
   * 普通缓存获取:返回类型会被强转
   * @param key
   * @param clazz
   * @param <T>
   * @return
   */
  public <T> T get(String key, Class<T> clazz){
    try {
      JSONObject jsonObject = (JSONObject) redisTemplate.opsForValue().get(key);
      return JSON.toJavaObject(jsonObject, clazz);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 是否有key
   * @param key
   * @return
   */
  public boolean hasKey(String key){
    return redisTemplate.hasKey(key);
  }

  /**
   * 删除缓存
   * @param key
   */
  public void del(String... key){
    redisTemplate.delete(Arrays.asList(key));
  }




}
