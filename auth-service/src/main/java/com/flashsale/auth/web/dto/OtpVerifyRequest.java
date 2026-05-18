package com.flashsale.auth.web.dto;

import java.util.UUID;

public record OtpVerifyRequest(UUID userId, String otpCode) {}
