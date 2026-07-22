package com.flowpay.routing.domain.model;

import java.util.Optional;

public enum TeamType {
    CARTOES("Cartões"),
    EMPRESTIMOS("Empréstimos"),
    OUTROS_ASSUNTOS("Outros Assuntos");

    private final String displayName;

    TeamType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<TeamType> fromString(String value) {
        if (value == null) {
            return Optional.empty();
        }
        for (TeamType type : values()) {
            if (type.name().equalsIgnoreCase(value) || type.getDisplayName().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
