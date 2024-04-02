package com.example.calendrierceri.model;

import java.io.Serializable;

public class User implements Serializable {

    public String getUserPreferenceTheme() {
        return userPreferenceTheme;
    }

    public void setUserPreferenceTheme(String userPreferenceTheme) {
        this.userPreferenceTheme = userPreferenceTheme;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getEdtFormationId() {
        return edtFormationId;
    }

    public void setEdtFormationId(int edtFormationId) {
        this.edtFormationId = edtFormationId;
    }

    public int getEdtPersonnelId() {
        return edtPersonnelId;
    }

    public void setEdtPersonnelId(int edtPersonnelId) {
        this.edtPersonnelId = edtPersonnelId;
    }

    public int getEdtProfId() {
        return edtProfId;
    }

    public void setEdtProfId(int edtProfId) {
        this.edtProfId = edtProfId;
    }

    private int userId;
    private String nom;
    private String prenom;
    private String motDePasse;
    private String role;
    private int edtFormationId;
    private int edtPersonnelId;
    private int edtProfId;
    private String userPreferenceTheme;

}
