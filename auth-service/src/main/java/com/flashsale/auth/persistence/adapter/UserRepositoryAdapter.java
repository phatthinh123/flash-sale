package com.flashsale.auth.persistence.adapter;

import com.flashsale.auth.business.dto.User;
import com.flashsale.auth.business.port.UserRepository;
import com.flashsale.auth.persistence.entity.UserEntity;
import com.flashsale.auth.persistence.jpa.JpaUserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;
  private final JpaMapper jpaMapper;

  @Override
  public User save(User user) {
    UserEntity entity = jpaMapper.toEntity(user);
    UserEntity saved = jpaUserRepository.save(entity);
    return jpaMapper.toDomain(saved);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpaUserRepository.findById(id).map(jpaMapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email).map(jpaMapper::toDomain);
  }

  @Override
  public Optional<User> findByPhone(String phone) {
    return jpaUserRepository.findByPhone(phone).map(jpaMapper::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaUserRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByPhone(String phone) {
    return jpaUserRepository.existsByPhone(phone);
  }

  @Override
  @Transactional
  public void updateVerificationStatus(UUID userId, String targetType, boolean verified) {
    jpaUserRepository
        .findById(userId)
        .ifPresent(
            entity -> {
              if ("EMAIL".equals(targetType)) {
                entity.setEmailVerified(verified);
              } else {
                entity.setPhoneVerified(verified);
              }
              jpaUserRepository.save(entity);
            });
  }

  @Transactional
  @Override
  public void updateBalance(UUID userId, BigDecimal newBalance) {
    jpaUserRepository
        .findById(userId)
        .ifPresent(
            entity -> {
              entity.setBalance(newBalance);
              jpaUserRepository.save(entity);
            });
  }

  @Mapper(componentModel = "spring")
  interface JpaMapper {
    UserEntity toEntity(User user);

    User toDomain(UserEntity userEntity);
  }
}
