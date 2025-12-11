package com.upb.snack.repository;

import com.upb.snack.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * Busca un código que:
     *  - coincida exactamente (ya normalizado en el service),
     *  - no esté usado,
     *  - no esté vencido (expiresAt > now).
     */
    Optional<VerificationCode> findByCodeAndUsedFalseAndExpiresAtAfter(
            String code,
            LocalDateTime now
    );
}
