package com.upb.snack.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
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

}