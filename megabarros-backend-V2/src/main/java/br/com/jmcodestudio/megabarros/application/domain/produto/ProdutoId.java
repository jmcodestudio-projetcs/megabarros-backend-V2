package br.com.jmcodestudio.megabarros.application.domain.produto;

public record ProdutoId(Integer value) {
    public static ProdutoId of(Integer value) {
        return value == null ? null : new ProdutoId(value);
    }
}
