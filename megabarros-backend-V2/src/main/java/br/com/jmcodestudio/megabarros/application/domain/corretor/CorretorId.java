package br.com.jmcodestudio.megabarros.application.domain.corretor;

/**
 * CorretorId representa o identificador único de um Corretor.
 * @param value
 */
public record CorretorId(Integer value) {
    public static CorretorId of(Integer value) {
        return value == null ? null : new CorretorId(value);
    }
}
