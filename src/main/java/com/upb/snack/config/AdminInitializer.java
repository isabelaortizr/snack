package com.upb.snack.config;

import com.upb.snack.entity.User;
import com.upb.snack.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Long adminId = 123123L;
        String rawPassword = "root123!";

        userRepository.findById(adminId).ifPresentOrElse(
                existing -> {
                    // Si existe, me aseguro de que sea ADMIN
                    boolean changed = false;

                    if (existing.getRol() == null ||
                            !"ADMIN".equalsIgnoreCase(existing.getRol())) {
                        existing.setRol("ADMIN");
                        changed = true;
                    }

                    // opcional: resetear contraseña si quieres
                    if (existing.getPassword() == null) {
                        existing.setPassword(passwordEncoder.encode(rawPassword));
                        changed = true;
                    }

                    if (changed) {
                        userRepository.save(existing);
                        System.out.println("[AdminInitializer] Usuario existente actualizado a ADMIN con id: " + adminId);
                    } else {
                        System.out.println("[AdminInitializer] Usuario admin ya estaba configurado con id: " + adminId);
                    }
                },
                () -> {
                    // Si NO existe, lo creo desde cero
                    User admin = new User();
                    admin.setId(adminId);
                    admin.setNombre("Administrador Snack");
                    admin.setPassword(passwordEncoder.encode(rawPassword));
                    admin.setRol("ADMIN");
                    userRepository.save(admin);
                    System.out.println("[AdminInitializer] Usuario admin creado en 'users' con id: " + adminId);
                }
        );
    }
}
