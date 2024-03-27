package com.example.calendrierceri.model;

public class Event {
    private int eventId;
    private String dtstart;
    private String dtend;
    private String matiere;
    private String enseignant;
    private String td;
    private String salle;
    private String type;
    private Integer edtId;

    public Event(int eventId, String dtstart, String dtend, String matiere, String enseignant, String td, String salle, String type, Integer edtId) {
        this.eventId = eventId;
        this.dtstart = dtstart;
        this.dtend = dtend;
        this.matiere = matiere;
        this.enseignant = enseignant;
        this.td = td;
        this.salle = salle;
        this.type = type;
        this.edtId = edtId;
    }

    public Event() {
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getDtstart() {
        return dtstart;
    }

    public void setDtstart(String dtstart) {
        this.dtstart = dtstart;
    }

    public String getDtend() {
        return dtend;
    }

    public void setDtend(String dtend) {
        this.dtend = dtend;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public String getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(String enseignant) {
        this.enseignant = enseignant;
    }

    public String getTd() {
        return td;
    }

    public void setTd(String td) {
        this.td = td;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getEdtId() {
        return edtId;
    }

    public void setEdtId(Integer edtId) {
        this.edtId = edtId;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", dtstart='" + dtstart + '\'' +
                ", dtend='" + dtend + '\'' +
                ", matiere='" + matiere + '\'' +
                ", enseignant='" + enseignant + '\'' +
                ", td='" + td + '\'' +
                ", salle='" + salle + '\'' +
                ", type='" + type + '\'' +
                ", edtId=" + edtId +
                '}';
    }
}
