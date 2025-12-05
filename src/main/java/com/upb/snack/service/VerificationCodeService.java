package com.upb.snack.service;

import com.upb.snack.dto.GenerateCodeResponse;
import com.upb.snack.entity.VerificationCode;
import com.upb.snack.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;

    @Value("${external.default-client-id:SNACK-EXTERNAL}")
    private String defaultClientId;

    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
    }

    public GenerateCodeResponse generateCode(String clientId) {
        String finalClientId = (clientId != null && !clientId.isBlank())
                ? clientId
                : defaultClientId;

        String code = generateRandomCode(6); // 6 caracteres

        VerificationCode vc = new VerificationCode();
        vc.setCode(code);
        vc.setClientId(finalClientId);
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        vc.setUsed(false);

        verificationCodeRepository.save(vc);

        return new GenerateCodeResponse(code, vc.getExpiresAt());
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sin confusos
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }
}
