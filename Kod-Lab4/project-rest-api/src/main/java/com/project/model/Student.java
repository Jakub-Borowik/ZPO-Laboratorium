package com.project.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity //Indeksujemy kolumny, które są najczęściej wykorzystywane do wyszukiwania studentów
@Table(name = "student",
indexes = { @Index(name = "idx_nazwisko", columnList = "nazwisko", unique = false),
 @Index(name = "idx_nr_indeksu", columnList = "nr_indeksu", unique = true),
 @Index(name = "idx_imie", columnList = "imie", unique = false) })

public class Student {

@Id
@GeneratedValue
@Column(name = "student_id")
private Integer studentId;

@NotBlank(message = "Pole imie nie może być puste.")
@Size(min = 3, max = 50, message = "Imie musi zawierać od {min} do {max} znaków.")
@Column(nullable = false, length = 50)
private String imie;

@NotBlank(message = "Pole nazwisko nie może być puste.")
@Size(min = 3, max = 100, message = "Nazwisko musi zawierać od {min} do {max} znaków.")
@Column(nullable = false, length = 100)
private String nazwisko;

@NotNull(message = "Pole nr_indeksu nie może być puste.")
@Column(name = "nr_indeksu", nullable = false, length = 20)
private Integer nrIndeksu;

@NotBlank(message = "Pole email nie może być puste.")
@Column(nullable = false, length = 50)
private String email;

@Column(nullable = false)
private Boolean stacjonarny;

@ManyToMany(mappedBy = "studenci")
@JsonIgnoreProperties({"studenci"})
private Set<Projekt> projekty;
}
