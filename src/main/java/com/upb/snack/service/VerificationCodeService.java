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

    /**
     * ClientId por defecto cuando no se manda uno explícito en la API externa.
     */
    @Value("${external.default-client-id:SNACK-EXTERNAL}")
    private String defaultClientId;

    /**
     * Minutos de vigencia del código (TTL).
     * Puedes cambiar este valor en application.properties:
     *   verification-code.ttl-minutes=10
     */
    @Value("${verification-code.ttl-minutes:10}")
    private int ttlMinutes;

    /**
     * Longitud del código generado.
     *   verification-code.length=6
     */
    @Value("${verification-code.length:6}")
    private int codeLength;

    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
    }

    /**
     * Genera un nuevo código de verificación (para descuento) asociado a un clientId.
     * El código se guarda en BD con expiresAt y used=false.
     */
    public GenerateCodeResponse generateCode(String clientId) {
        String finalClientId = (clientId != null && !clientId.isBlank())
                ? clientId
                : defaultClientId;

        String code = generateRandomCode(codeLength); // p.ej. 6 caracteres

        VerificationCode vc = new VerificationCode();
        vc.setCode(code);
        vc.setClientId(finalClientId);
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        vc.setUsed(false);

        verificationCodeRepository.save(vc);

        return new GenerateCodeResponse(code, vc.getExpiresAt());
    }

    /**
     * Verifica si un código es válido:
     *  - existe
     *  - no está usado
     *  - no está vencido
     *
     * Devuelve true/false para que el servicio de órdenes decida si aplicar el descuento.
     */
    public boolean isValid(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        String normalized = code.trim().toUpperCase();

        return verificationCodeRepository
                .findByCodeAndUsedFalseAndExpiresAtAfter(
                        normalized,
                        LocalDateTime.now()
                )
                .isPresent();
    }

    /**
     * Marca un código como usado.
     * Se vuelve a buscar con las mismas condiciones de validez para evitar
     * marcar códigos inexistentes o ya vencidos.
     *
     * Si el código no es válido, lanza IllegalArgumentException.
     */
    public void markCodeAsUsed(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }

        String normalized = code.trim().toUpperCase();

        VerificationCode vc = verificationCodeRepository
                .findByCodeAndUsedFalseAndExpiresAtAfter(
                        normalized,
                        LocalDateTime.now()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Código de descuento inválido o expirado.")
                );

        vc.setUsed(true);
        verificationCodeRepository.save(vc);
    }

    /**
     * Utilidad interna para generar un código aleatorio en mayúsculas,
     * sin caracteres confusos (O, 0, I, 1, etc.).
     */
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
