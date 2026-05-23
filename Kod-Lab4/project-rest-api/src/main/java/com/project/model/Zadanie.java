package com.project.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="zadanie", indexes = { @Index (name = "idx_nazwa_zadania", columnList = "nazwa"),
    @Index (name = "idx_data_dodania", columnList = "dataCzasDodania") })

@EntityListeners(AuditingEntityListener.class)
public class Zadanie {

@Id
@GeneratedValue
@Column(name="zadanie_id")
private Integer zadanieId;

@ManyToOne
@JoinColumn(name ="projekt_id", nullable = false)
@JsonIgnoreProperties({"zadania"})
private Projekt projekt;

@Column(nullable = false, length = 50)
private String nazwa;

@Column
private Integer kolejnosc;

@Column(length = 1000)
private String opis;

@CreatedDate
@Column(name = "data_dodania", nullable = false, updatable = false)
private LocalDateTime dataCzasDodania;
}
