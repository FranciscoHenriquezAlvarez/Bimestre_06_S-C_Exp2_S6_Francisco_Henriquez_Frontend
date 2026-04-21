package com.duoc.frontend.view;

import org.springframework.stereotype.Component;

@Component("adoptionDisplay")
public class AdoptionDisplayHelper {

    // Se agrego este helper para traducir solo textos visibles sin cambiar valores internos del backend.
    public String species(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "dog", "perro" -> "Perro";
            case "cat", "gato" -> "Gato";
            default -> fallback(value);
        };
    }

    // Se centraliza el genero visible para evitar mostrar Male o Female en las vistas de adopcion.
    public String gender(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "male", "macho" -> "Macho";
            case "female", "hembra" -> "Hembra";
            default -> fallback(value);
        };
    }

    public String status(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "available", "disponible" -> "Disponible";
            case "unavailable", "no disponible", "no_disponible" -> "No disponible";
            case "adopted", "adoptado", "adoptada" -> "Adoptado";
            default -> fallback(value);
        };
    }

    public String statusBadgeClass(String value) {
        String normalized = normalize(value);
        return switch (normalized) {
            case "available", "disponible" -> " badge-available";
            case "adopted", "adoptado", "adoptada" -> " badge-adopted";
            default -> " badge-unavailable";
        };
    }

    public boolean isAvailable(String value) {
        return "available".equals(normalize(value)) || "disponible".equals(normalize(value));
    }

    public boolean isSpecies(String value, String expected) {
        return normalize(value).equals(normalize(expected));
    }

    public boolean isGender(String value, String expected) {
        return normalize(value).equals(normalize(expected));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replace("_", " ").toLowerCase();
    }

    private String fallback(String value) {
        return value == null || value.isBlank() ? "Sin informacion" : value;
    }
}
