package com.flashsale.auth.persistence.adapter;

import com.flashsale.auth.business.dto.OtpToken;
import com.flashsale.auth.business.port.OtpRepository;
import com.flashsale.auth.persistence.entity.OtpTokenEntity;
import com.flashsale.auth.persistence.jpa.JpaOtpRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class OtpRepositoryAdapter implements OtpRepository {

  private final JpaOtpRepository jpaOtpRepository;
  private final JpaMapper jpaMapper;

  @Override
  @Transactional
  public OtpToken save(OtpToken otpToken) {
    OtpTokenEntity entity = jpaMapper.toEntity(otpToken);
    OtpTokenEntity saved = jpaOtpRepository.save(entity);
    return jpaMapper.toDomain(saved);
  }

  @Override
  public Optional<OtpToken> findLatestUnusedByUserId(UUID userId) {
    return jpaOtpRepository
        .findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(userId)
        .map(jpaMapper::toDomain);
  }

  @Override
  @Transactional
  public void markAsUsed(UUID otpTokenId) {
    jpaOtpRepository
        .findById(otpTokenId)
        .ifPresent(
            entity -> {
              entity.setUsed(true);
              jpaOtpRepository.save(entity);
            });
  }

  @Mapper(componentModel = "spring")
  interface JpaMapper {
    OtpTokenEntity toEntity(OtpToken otpToken);

    OtpToken toDomain(OtpTokenEntity otpToken);
  }
}
