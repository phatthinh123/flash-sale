package com.flashsale.auth.web.port;

public interface TokenProvider {
  String generateToken(String userId);

  String extractUserId(String token);

  boolean validateToken(String token);

  void blacklistToken(String token);

  boolean isBlacklisted(String token);
}
