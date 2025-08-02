package com.thomasmore.blc.labflow.entity;

import jakarta.persistence.*;
import org.springframework.lang.Nullable;


@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // voor auto-increment in SQLite
    private Long id;

    private String wachtwoord;

    @Nullable
    private String refreshToken;

    private String email;

    @Nullable
    private String voorNaam;

    @Nullable
    private String achterNaam;

    // foreign key naar de rol tabel
    @Nullable
    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    // lege constructor
    public User() {
    }

    // constructor aanmaken user met naam
    public User(String voorNaam, String achterNaam) {
        this.voorNaam = voorNaam;
        this.achterNaam = achterNaam;
    }

    // constructor met alle argumenten
    public User(String wachtwoord, @Nullable String refresh_token, String email, @Nullable String voorNaam,@Nullable String achterNaam, Rol rol) {
        this.wachtwoord = wachtwoord;
        this.refreshToken = refresh_token;
        this.email = email;
        this.voorNaam = voorNaam;
        this.achterNaam = achterNaam;
        this.rol = rol;
    }

    // getters en setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWachtwoord() {
        return wachtwoord;
    }

    public void setWachtwoord(String wachtwoord) {
        this.wachtwoord = wachtwoord;
    }

    public void setRefreshToken(@Nullable String refresh_token) {
        this.refreshToken = refresh_token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVoorNaam() {
        return voorNaam;
    }

    public void setVoorNaam(String voorNaam) {
        this.voorNaam = voorNaam;
    }

    public String getAchterNaam() {
        return achterNaam;
    }

    @Nullable
    public Rol getRol() {
        return rol;
    }

    public void setRol(@Nullable Rol rol) {
        this.rol = rol;
    }

    public void setAchterNaam(String achterNaam) {
        this.achterNaam = achterNaam;
    }

    public String getFullName() {
        return voorNaam + " " + achterNaam;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
