package br.com.jmcodestudio.megabarros.application.domain.apolice;

public record ApoliceId(Integer value) {
    public static ApoliceId of(Integer value) {
        return value == null ? null : new ApoliceId(value);
    }
}
