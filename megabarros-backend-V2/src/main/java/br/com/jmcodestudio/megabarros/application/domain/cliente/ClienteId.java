package br.com.jmcodestudio.megabarros.application.domain.cliente;

public record ClienteId(Integer value) {
    public static ClienteId of(Integer value) {
        return value == null ? null : new ClienteId(value);
    }
}
