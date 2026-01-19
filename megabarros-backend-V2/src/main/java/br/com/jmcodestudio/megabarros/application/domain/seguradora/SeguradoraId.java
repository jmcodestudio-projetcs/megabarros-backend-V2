package br.com.jmcodestudio.megabarros.application.domain.seguradora;

public record SeguradoraId(Integer value) {
    public static SeguradoraId of(Integer value) {
        return value == null ? null : new SeguradoraId(value);
    }
}
