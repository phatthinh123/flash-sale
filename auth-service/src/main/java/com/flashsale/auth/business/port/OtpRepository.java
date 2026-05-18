package com.flashsale.auth.business.port;

import com.flashsale.auth.business.dto.OtpToken;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository {
  OtpToken save(OtpToken otpToken);

  Optional<OtpToken> findLatestUnusedByUserId(UUID userId);

  void markAsUsed(UUID otpTokenId);
}
