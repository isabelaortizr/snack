package com.upb.snack.service;

import com.upb.snack.entity.Aula;
import com.upb.snack.repository.AulaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AulaService {

    private final AulaRepository aulaRepository;

    public AulaService(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }

    public List<Aula> getAllAulas() {
        return aulaRepository.findAll();
    }

    public Aula getAula(Long id) {
        Long safeId = requireId(id);
        return aulaRepository.findById(safeId)
                .orElseThrow(() -> new EntityNotFoundException("Aula no encontrada: " + id));
    }

    public Aula createAula(Aula aula) {
        Aula safeAula = Objects.requireNonNull(aula, "El aula es obligatoria");
        return aulaRepository.save(safeAula);
    }

    public Aula updateAula(Long id, Aula aula) {
        Long safeId = requireId(id);
        Aula safeAula = Objects.requireNonNull(aula, "El aula es obligatoria");
        if (!aulaRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Aula no encontrada: " + id);
        }
        return aulaRepository.save(safeAula);
    }

    public void deleteAula(Long id) {
        Long safeId = requireId(id);
        if (!aulaRepository.existsById(safeId)) {
            throw new EntityNotFoundException("Aula no encontrada: " + id);
        }
        aulaRepository.deleteById(safeId);
    }

    private Long requireId(Long id) {
        return Objects.requireNonNull(id, "El id de aula es obligatorio");
    }
}

