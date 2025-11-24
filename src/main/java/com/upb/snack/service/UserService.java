package com.upb.snack.service;

import com.upb.snack.entity.User;
import com.upb.snack.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        Long safeId = requireId(id);
        return userRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
    }

    public User createUser(User user) {
        User safeUser = Objects.requireNonNull(user, "El usuario es obligatorio");
        return userRepository.save(safeUser);
    }

    public User updateUser(Long id, User user) {
        Long safeId = requireId(id);
        User safeUser = Objects.requireNonNull(user, "El usuario es obligatorio");
        if (!userRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Usuario no encontrado: " + id);
        }
        return userRepository.save(safeUser);
    }

    public void deleteUser(Long id) {
        Long safeId = requireId(id);
        if (!userRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Usuario no encontrado: " + id);
        }
        userRepository.deleteById(safeId);
    }

    private Long requireId(Long id) {
        return Objects.requireNonNull(id, "El id del usuario es obligatorio");
    }
}

