package com.flashsale.auth.business.port;

public interface CommunicationPort {
  void sendOTP(String target, String otpCode, String targetType);
}
