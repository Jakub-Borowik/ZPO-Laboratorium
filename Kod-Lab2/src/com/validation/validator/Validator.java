package com.validation.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.validation.exception.ValidationException;
import com.validation.strategy.ValidationStrategy;
import com.validation.strategy.ValidationStrategyFactory;

public class Validator {

    private Validator() {
    }

    public static void validate(Object object) throws ValidationException {
        final List<String> errors = new ArrayList<>();
        final Class<?> clazz = object.getClass();
        
        // iterowanie po wszystkich polach danej klasy
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // umożliwia dostęp również do pól prywatnych danej klasy
            try {
                Object value = field.get(object);
                
                // iterowanie po wszystkich adnotacjach danego pola
                for (Annotation annotation : field.getAnnotations()) {
                    ValidationStrategy strategy = ValidationStrategyFactory.getStrategy(annotation);
                    if (strategy != null) {
                        Optional<String> validationError = strategy.validate(field, value);
                        if (validationError.isPresent()) {
                            errors.add(validationError.get());
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                errors.add("Błąd dostępu do pola: " + field.getName());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("\n", errors));
        }
    }
}