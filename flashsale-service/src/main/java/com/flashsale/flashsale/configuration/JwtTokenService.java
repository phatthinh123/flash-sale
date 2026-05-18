package com.flashsale.flashsale.configuration;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenService {

  private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

  private final SecretKey key;
  private final StringRedisTemplate redisTemplate;

  public JwtTokenService(JwtProperties jwtProperties, StringRedisTemplate redisTemplate) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
    this.redisTemplate = redisTemplate;
  }

  public String extractUserId(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
  }

  public boolean validateToken(String token) {
    try {
      if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
        return false;
      }
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }
}
