package com.upb.snack.service;

import com.upb.snack.entity.User;
import com.upb.snack.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        long safeId = requireId(id);
        return userRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
    }

    public User createUser(User user) {
        User safeUser = Objects.requireNonNull(user, "El usuario es obligatorio");
        String rawPassword = Objects.requireNonNull(safeUser.getPassword(), "La contraseña es obligatoria");
        safeUser.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(safeUser);
    }

    public User updateUser(Long id, User user) {
        long safeId = requireId(id);
        User safeUser = Objects.requireNonNull(user, "El usuario es obligatorio");
        User existingUser = userRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));

        existingUser.setNombre(safeUser.getNombre());
        if (safeUser.getPassword() != null && !safeUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(safeUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        long safeId = requireId(id);
        if (!userRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Usuario no encontrado: " + id);
        }
        userRepository.deleteById(safeId);
    }

    public User authenticate(Long id, String password) {
        long safeId = requireId(id);
        String rawPassword = Objects.requireNonNull(password, "La contraseña es obligatoria");
        User user = userRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        return user;
    }

    private long requireId(Long id) {
        return Objects.requireNonNull(id, "El id del usuario es obligatorio");
    }
}

