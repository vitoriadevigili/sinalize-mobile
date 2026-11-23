package br.com.sinalize.api.dto.quiz;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuizRequest(

        @NotEmpty(message = "Informe pelo menos uma categoria.")
        List<@NotNull(message = "A lista de categorias não pode conter valores nulos.") Long> categoriaIds) {
}
