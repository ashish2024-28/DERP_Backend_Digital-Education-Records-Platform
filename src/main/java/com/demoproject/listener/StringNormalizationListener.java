package com.demoproject.listener;

import com.demoproject.annotation.LowerCase;
import com.demoproject.annotation.TitleCase;
import com.demoproject.annotation.UpperCase;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.lang.reflect.Field;
import java.util.Locale;

public class StringNormalizationListener {

    @PrePersist
    @PreUpdate
    public void normalize(Object entity) {

        if (entity == null) {
            return;
        }

        Class<?> clazz = entity.getClass();

        for (Field field : clazz.getDeclaredFields()) {

            // We only process String fields
            if (field.getType() != String.class) {
                continue;
            }

            try {
                field.setAccessible(true);

                String value = (String) field.get(entity);

                if (value == null || value.isBlank()) {
                    continue;
                }

                // @UpperCase
                if (field.isAnnotationPresent(UpperCase.class)) {
                    field.set(entity, value.toUpperCase(Locale.ROOT));
                }

                // @LowerCase
                else if (field.isAnnotationPresent(LowerCase.class)) {
                    field.set(entity, value.toLowerCase(Locale.ROOT));
                }

                // @TitleCase
                else if (field.isAnnotationPresent(TitleCase.class)) {
                    field.set(entity, toTitleCase(value));
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                    "Unable to normalize field: " + field.getName(), e
                );
            }
        }
    }

    private String toTitleCase(String value) {

        String[] words = value.trim().toLowerCase(Locale.ROOT).split("\\s+");

        StringBuilder result = new StringBuilder();

        for (String word : words) {

            if (word.isEmpty()) {
                continue;
            }

            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(Character.toUpperCase(word.charAt(0)));

            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }

        return result.toString();
    }
}