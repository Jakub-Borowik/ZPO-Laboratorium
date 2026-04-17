package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;

import com.validation.annotation.Size;
import com.validation.annotation.ValidationFor;

@ValidationFor(Size.class)
public class SizeStrategy implements ValidationStrategy {
    @Override
    public Optional<String> validate(Field field, Object value) {
        if (field.isAnnotationPresent(Size.class) && value instanceof String) {
            Size annotation = field.getAnnotation(Size.class);
            String strValue = (String) value;
            
            if (strValue.length() < annotation.min() || strValue.length() > annotation.max()) {
                // Podmieniamy {min} i {max} z wiadomości na konkretne liczby
                String errorMessage = annotation.message()
                        .replace("{min}", String.valueOf(annotation.min()))
                        .replace("{max}", String.valueOf(annotation.max()));
                return Optional.of(String.format("Pole %s: %s", field.getName(), errorMessage));
            }
        }
        return Optional.empty();
    }
}