package com.upb.snack.service;

import com.upb.snack.entity.Admin;
import com.upb.snack.repository.AdminRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin getAdmin(Long id) {
        Long safeId = requireId(id);
        return adminRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Admin no encontrado: " + id));
    }

    public Admin createAdmin(Admin admin) {
        Admin safeAdmin = Objects.requireNonNull(admin, "El admin es obligatorio");
        return adminRepository.save(safeAdmin);
    }

    public Admin updateAdmin(Long id, Admin admin) {
        Long safeId = requireId(id);
        Admin safeAdmin = Objects.requireNonNull(admin, "El admin es obligatorio");
        if (!adminRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Admin no encontrado: " + id);
        }
        return adminRepository.save(safeAdmin);
    }

    public void deleteAdmin(Long id) {
        Long safeId = requireId(id);
        if (!adminRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Admin no encontrado: " + id);
        }
        adminRepository.deleteById(safeId);
    }

    private Long requireId(Long id) {
        return Objects.requireNonNull(id, "El id de admin es obligatorio");
    }
}

