package com.yangjq.commons.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

/**
 * JsonWebToken工具类
 *
 * @author yangjq
 * @since 2022/7/5
 */
public class TokenUtil {

  /**
   * 请求头中JWT信息的HeaderName
   */
  public static final String AUTH_HEADER = "Authorization";

  /**
   * JWT 添加至HTTP HEAD中的前缀
   */
  public static final String TOKEN_PREFIX = "Bearer ";

  /**
   * 失效时间：7天 单位：毫秒
   */
  public static final long TOKEN_DURATION = 7 * 24 * 60 * 60 * 1000;

  private static final String JWT_SECRET = "ERWlLFI49q4WjCYu";

  private static final String jwtId = "tokenId";


  /**
   * 创建JWT
   */
  public static String createToken(Map<String, Object> claims) {
    return createToken(claims, TOKEN_DURATION);
  }

  /**
   * 创建JWT
   *
   * @param claims 私有声明
   * @param time 有效时间 单位：微秒
   */
  public static String createToken(Map<String, Object> claims, Long time) {
    //指定签名算法
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
    Date now = new Date(System.currentTimeMillis());
    Date exp = new Date(System.currentTimeMillis() + time);

    //设置payload
    JwtBuilder builder = Jwts.builder()
        .setClaims(claims) //私有声明，需要设置在标准声明前
        .setId(jwtId) //jwt ID：jwt的唯一标识
        .setIssuedAt(now) //jwt的签发时间
        .setExpiration(exp) //jwt的过期时间
        .signWith(signatureAlgorithm, generalKey()); //设置签名算法和签名密钥

    return builder.compact();
  }

  /**
   * 解析request获取负载
   *
   * @param request 请求对象
   * @return Claims
   */
  public static Claims parseToken(HttpServletRequest request)
      throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException {
    // 获取Token
    String token = request.getHeader(AUTH_HEADER);
    return parseToken(token);
  }

  /**
   * 解析token获取负载
   *
   * @param token token, 带'Bearer '前缀
   * @return Claims
   * @throws ExpiredJwtException token 过期
   * @throws UnsupportedJwtException 不支持的 token
   * @throws MalformedJwtException token 格式不对
   * @throws SignatureException token 签名不对
   */
  public static Claims parseToken(String token)
      throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException {
    return Jwts.parser()
        .setSigningKey(generalKey())
        .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
        .getBody();
  }

  /**
   * 由字符串生成加密key
   */
  private static SecretKey generalKey() {
    byte[] encodeKey = Base64.getDecoder().decode(JWT_SECRET);
    return new SecretKeySpec(encodeKey, 0, encodeKey.length, "AES");
  }

  /**
   * 判断token是否已经失效
   */
  public boolean isTokenExpired(String token) {
    Date expiredDate = parseToken(token).getExpiration();
    return expiredDate.before(new Date());
  }

}
