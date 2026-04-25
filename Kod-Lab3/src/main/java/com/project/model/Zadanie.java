package com.project.model;
import java.time.LocalDateTime;

public class Zadanie {
    private Integer zadanieId;
    private String nazwa;
    private String opis;
    private Integer kolejnosc;
    private LocalDateTime dataCzasUtworzenia;
    private Integer projektId;

    public Zadanie() {}

    public Zadanie(Integer zadanieId, String nazwa, String opis, Integer kolejnosc, LocalDateTime dataCzasUtworzenia, Integer projektId) {
        this.zadanieId = zadanieId;
        this.nazwa = nazwa;
        this.opis = opis;
        this.kolejnosc = kolejnosc;
        this.dataCzasUtworzenia = dataCzasUtworzenia;
        this.projektId = projektId;
    }

    public Integer getZadanieId() { return zadanieId; }
    public void setZadanieId(Integer zadanieId) { this.zadanieId = zadanieId; }
    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }
    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }
    public Integer getKolejnosc() { return kolejnosc; }
    public void setKolejnosc(Integer kolejnosc) { this.kolejnosc = kolejnosc; }
    public LocalDateTime getDataCzasUtworzenia() { return dataCzasUtworzenia; }
    public void setDataCzasUtworzenia(LocalDateTime dataCzasUtworzenia) { this.dataCzasUtworzenia = dataCzasUtworzenia; }
    public Integer getProjektId() { return projektId; }
    public void setProjektId(Integer projektId) { this.projektId = projektId; }
}