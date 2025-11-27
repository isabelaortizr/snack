package com.upb.snack.config;

import com.upb.snack.entity.Aula;
import com.upb.snack.repository.AulaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AulaDataInitializer implements CommandLineRunner {

    private final AulaRepository aulaRepository;

    public AulaDataInitializer(AulaRepository aulaRepository) {
        this.aulaRepository = aulaRepository;
    }

    @Override
    public void run(String... args) {
        if (aulaRepository.count() > 0) {
            return; // ya hay datos, no sembrar
        }

        List<Aula> aulas = new ArrayList<>();

        // ----- 1er piso – Edificio 1 -----
        add(aulas, "A1", "Edificio 1", 1, "Aula A1");
        add(aulas, "A2", "Edificio 1", 1, "Aula A2");
        add(aulas, "A3", "Edificio 1", 1, "Aula A3");
        add(aulas, "Lab de Comunicación", "Edificio 1", 1, "Laboratorio de Comunicación");

        // ----- 2do piso – Edificio 1 -----
        add(aulas, "Lab de Negocios",  "Edificio 1", 2, "Laboratorio de Negocios");
        add(aulas, "Lab de Finanzas",  "Edificio 1", 2, "Laboratorio de Finanzas");
        add(aulas, "B1", "Edificio 1", 2, "Aula B1");
        add(aulas, "B2", "Edificio 1", 2, "Aula B2");
        add(aulas, "B3", "Edificio 1", 2, "Aula B3");

        // ----- 2do piso – Edificio 2 -----
        add(aulas, "Lab Bioingeniería", "Edificio 2", 2, "Laboratorio de Bioingeniería");
        add(aulas, "Lab de Sistemas",   "Edificio 2", 2, "Laboratorio de Sistemas");
        add(aulas, "D1", "Edificio 2", 2, "Aula D1");
        add(aulas, "D2", "Edificio 2", 2, "Aula D2");
        add(aulas, "D3", "Edificio 2", 2, "Aula D3");

        // ----- 3er piso – Edificio 1 -----
        add(aulas, "C1", "Edificio 1", 3, "Aula C1");
        add(aulas, "C2", "Edificio 1", 3, "Aula C2");
        add(aulas, "C3", "Edificio 1", 3, "Aula C3");

        // ----- 3er piso – Edificio 2 -----
        add(aulas, "E1", "Edificio 2", 3, "Aula E1");
        add(aulas, "E2", "Edificio 2", 3, "Aula E2");
        add(aulas, "E3", "Edificio 2", 3, "Aula E3");
        add(aulas, "E4", "Edificio 2", 3, "Aula E4");
        add(aulas, "E5", "Edificio 2", 3, "Aula E5");
        add(aulas, "E6", "Edificio 2", 3, "Aula E6");
        add(aulas, "E7", "Edificio 2", 3, "Aula E7");

        aulaRepository.saveAll(aulas);
    }

    private static void add(List<Aula> out, String nombreAula, String edificio, int piso, String desc) {
        Aula a = new Aula();
        a.setAula(nombreAula);
        a.setEdificio(edificio);
        a.setPiso(piso);
        a.setDescription(desc);
        out.add(a);
    }
}
