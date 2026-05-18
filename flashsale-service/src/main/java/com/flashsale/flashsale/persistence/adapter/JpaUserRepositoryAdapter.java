package com.flashsale.flashsale.persistence.adapter;

import com.flashsale.flashsale.business.dto.User;
import com.flashsale.flashsale.business.port.UserRepository;
import com.flashsale.flashsale.persistence.entity.UserEntity;
import com.flashsale.flashsale.persistence.jpa.JpaUserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;
  private final JpaMapper jpaMapper;

  @Override
  public Optional<User> findById(UUID userId) {
    return jpaUserRepository.findById(userId).map(jpaMapper::toDomain);
  }

  @Override
  @Transactional
  public boolean debitBalance(UUID userId, BigDecimal amount) {
    return jpaUserRepository
        .findForUpdateById(userId)
        .filter(entity -> entity.getBalance().compareTo(amount) >= 0)
        .map(
            entity -> {
              entity.setBalance(entity.getBalance().subtract(amount));
              jpaUserRepository.save(entity);
              return true;
            })
        .orElse(false);
  }

  @Mapper(componentModel = "spring")
  interface JpaMapper {
    User toDomain(UserEntity entity);
  }
}
