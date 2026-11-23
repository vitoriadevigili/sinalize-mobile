package br.com.sinalize.api.dto.quiz;

import jakarta.validation.constraints.NotNull;

public record RespostaRequest(

        @NotNull(message = "Informe se o jogador acertou.")
        Boolean acertou) {
}
