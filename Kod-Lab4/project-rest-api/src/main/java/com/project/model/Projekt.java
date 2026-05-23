package com.project.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="projekt", indexes = { @Index(name = "idx_nazwa_projektu", columnList = "nazwa"),
    @Index (name = "idx_data_oddania", columnList = "dataOddania") })

@EntityListeners(AuditingEntityListener.class)
public class Projekt {

@Id
@GeneratedValue
@Column(name="projekt_id") //tylko jeżeli nazwa kolumny w bazie danych ma być inna od nazwy zmiennej
private Integer projektId;

@NotBlank(message = "Pole nazwa nie może być puste.")
@Size(min = 3, max = 50, message = "Nazwa musi zawierać od {min} do {max} znaków.")
@Column(nullable = false, length = 50)
private String nazwa;

@Column(length = 1000)
private String opis;

@CreatedDate
@Column(name = "data_utworzenia", nullable = false, updatable = false)
private LocalDateTime dataCzasUtworzenia;

@Column(name = "data_oddania")
private LocalDate dataOddania;

@OneToMany(mappedBy = "projekt", cascade = CascadeType.REMOVE)
@JsonIgnoreProperties({"projekt"})
private List<Zadanie> zadania;

@ManyToMany
@JoinTable(name = "projekt_student", // Budowa tabeli łączącej
joinColumns = {@JoinColumn(name="projekt_id")}, 
inverseJoinColumns = {@JoinColumn(name = "student_id")})
@JsonIgnoreProperties({"projekty"})
private Set<Student> studenci; // Set a nie List bo nie pozwala na duplikaty, dzięki czemu student nie doda się do projektu podwójnie.
}