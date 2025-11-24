package com.upb.snack.controller;

import com.upb.snack.entity.Aula;
import com.upb.snack.service.AulaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/aulas")
public class AulaController {

    private final AulaService aulaService;

    public AulaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    @GetMapping
    public List<Aula> getAllAulas() {
        return aulaService.getAllAulas();
    }

    @GetMapping("/{id}")
    public Aula getAula(@PathVariable Long id) {
        return aulaService.getAula(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Aula createAula(@RequestBody Aula aula) {
        return aulaService.createAula(aula);
    }

    @PutMapping("/{id}")
    public Aula updateAula(@PathVariable Long id, @RequestBody Aula aula) {
        return aulaService.updateAula(id, aula);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAula(@PathVariable Long id) {
        aulaService.deleteAula(id);
    }
}

