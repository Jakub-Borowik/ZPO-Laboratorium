package com.validation.strategy;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.validation.annotation.ValidationFor;

public class ValidationStrategyFactory {

    private static final Map<Class<? extends Annotation>, ValidationStrategy> strategies = new HashMap<>();

    static {
        // Rejestrujemy wszystkie nasze strategie przez @ValidationFor
        register(EmailStrategy.class);
        register(NotEmptyStrategy.class);
        register(NotNullStrategy.class);
        register(NrIndeksuStrategy.class);
        register(SizeStrategy.class);
    }

    private ValidationStrategyFactory() {
        
    }

    private static void register(Class<? extends ValidationStrategy> strategyClass) {
        ValidationFor binding = strategyClass.getAnnotation(ValidationFor.class);
        if (binding == null) {
            throw new IllegalStateException("Brak @ValidationFor na klasie: " + strategyClass.getName());
        }
        try {
            ValidationStrategy strategy = strategyClass.getDeclaredConstructor().newInstance();
            strategies.put(binding.value(), strategy);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Nie mozna utworzyc strategii: " + strategyClass.getName(), e);
        }
    }

    public static ValidationStrategy getStrategy(Annotation annotation) {
        return strategies.get(annotation.annotationType());
    }
}