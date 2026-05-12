package com.flashsale.auth.communication;

import com.flashsale.auth.business.port.CommunicationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MockCommunication implements CommunicationPort {

    @Override
    public void sendOTP(String target, String otpCode, String targetType) {
        log.info("========================================");
        log.info("  OTP VERIFICATION CODE");
        log.info("  Target: {} ({})", target, targetType);
        log.info("  Code: {}", otpCode);
        log.info("  Expires in: 5 minutes");
        log.info("========================================");
    }
}
