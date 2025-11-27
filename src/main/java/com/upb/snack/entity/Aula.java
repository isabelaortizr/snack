package com.upb.snack.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "aulas")
public class Aula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "aula", nullable = false, length = 30)
    private String aula;

    @Column(name = "edificio", length = 50)
    private String edificio;

    @Column(name = "piso")
    private Integer piso;

    @Column(name = "description", length = 150)
    private String description;

    public Aula() {
    }

    public Aula(Long id, String aula, String edificio, Integer piso, String description) {
        this.id = id;
        this.aula = aula;
        this.edificio = edificio;
        this.piso = piso;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public String getEdificio() {
        return edificio;
    }

    public void setEdificio(String edificio) {
        this.edificio = edificio;
    }

    public Integer getPiso() {
        return piso;
    }

    public void setPiso(Integer piso) {
        this.piso = piso;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Aula{" +
                "id=" + id +
                ", aula='" + aula + '\'' +
                ", edificio='" + edificio + '\'' +
                ", piso=" + piso +
                ", description='" + description + '\'' +
                '}';
    }
}

