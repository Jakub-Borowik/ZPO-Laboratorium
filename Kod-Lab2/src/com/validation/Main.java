package com.validation;

import com.validation.exception.ValidationException;
import com.validation.validator.Validator;

public class Main {
    public static void main(String[] args) {
        try {
            // Stworzenie studenta
            Student student = new Student();
            
            // minimum 3 znaki
            student.setImie("JB"); 
            
            // ponad 50 znaków
            student.setNazwisko("BorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowikBorowik");
            
            // != 8 znaków
            student.setNrIndeksu("123983");
            
            // brak @
            student.setEmail("Jakub.Borowik#pbs.edu.pl");

            Validator.validate(student);
            
            System.out.println("Dane są poprawne.");

        } catch (ValidationException e) {
            System.out.println("Złąpane błędy:");
            System.out.println(e.getMessage());
        }
    }
}