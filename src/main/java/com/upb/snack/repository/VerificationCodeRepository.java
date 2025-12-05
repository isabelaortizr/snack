package com.upb.snack.repository;

import com.upb.snack.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByCodeAndUsedFalseAndExpiresAtAfter(
            String code,
            LocalDateTime now
    );
}
