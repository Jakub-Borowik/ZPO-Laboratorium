package com.project.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Projekt {

    private Integer projektId;

    @NotBlank(message = "Pole nazwa nie może być puste.")
    @Size(min = 3, max = 50, message = "Nazwa musi zawierać od {min} do {max} znaków.")
    private String nazwa;

    private String opis;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime dataCzasUtworzenia;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataOddania;

    @JsonIgnoreProperties({"projekt"})
    private List<Zadanie> zadania;

    @JsonIgnoreProperties({"projekty"})
    private Set<Student> studenci;
}