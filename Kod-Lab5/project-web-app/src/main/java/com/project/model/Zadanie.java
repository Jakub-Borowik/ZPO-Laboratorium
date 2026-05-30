package com.project.model;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Zadanie {

private Integer zadanieId;

@JsonIgnoreProperties({"zadania"})
private Projekt projekt;

private String nazwa;

private Integer kolejnosc;

private String opis;

@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
private LocalDateTime dataCzasDodania;
}
