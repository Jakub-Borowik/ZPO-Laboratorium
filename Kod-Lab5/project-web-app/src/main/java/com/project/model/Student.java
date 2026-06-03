package com.project.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student {

private Integer studentId;

@NotBlank(message = "Pole imie nie może być puste.")
@Size(min = 3, max = 50, message = "Imie musi zawierać od {min} do {max} znaków.")
private String imie;

@NotBlank(message = "Pole nazwisko nie może być puste.")
@Size(min = 3, max = 100, message = "Nazwisko musi zawierać od {min} do {max} znaków.")
private String nazwisko;

@NotNull(message = "Pole nr_indeksu nie może być puste.")
private Integer nrIndeksu;

@NotBlank(message = "Pole email nie może być puste.")
private String email;

private Boolean stacjonarny;

@JsonIgnoreProperties({"studenci"})
@EqualsAndHashCode.Exclude
@ToString.Exclude
private Set<Projekt> projekty;
}
