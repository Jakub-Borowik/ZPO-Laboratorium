package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;

import com.validation.annotation.Email;
import com.validation.annotation.ValidationFor;

@ValidationFor(Email.class)
public class EmailStrategy implements ValidationStrategy {
    @Override
    public Optional<String> validate(Field field, Object value) {
        if (field.isAnnotationPresent(Email.class) && value instanceof String) {
            Email annotation = field.getAnnotation(Email.class);
            String email = (String) value;
            
            if (!email.contains("@")) {
                return Optional.of(String.format("Pole %s: %s", field.getName(), annotation.message()));
            }
        }
        return Optional.empty();
    }
}