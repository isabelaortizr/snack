package com.upb.snack.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol = "USER"; // USER o ADMIN

    public User() {
    }

    public User(Long id, String nombre, String password) {
        this(id, nombre, password, "USER");
    }

    public User(Long id, String nombre, String password, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol != null ? rol : "USER";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = (rol == null || rol.isBlank()) ? "USER" : rol;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", rol='" + rol + '\'' +
                '}';
    }
}
