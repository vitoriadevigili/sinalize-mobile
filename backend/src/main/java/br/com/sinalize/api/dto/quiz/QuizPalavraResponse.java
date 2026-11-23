package br.com.sinalize.api.dto.quiz;

import br.com.sinalize.api.dto.palavra.PalavraResumoResponse;
import br.com.sinalize.api.entity.QuizPalavra;

import java.time.LocalDateTime;

public record QuizPalavraResponse(
        Long id,
        Boolean acertou,
        LocalDateTime respondidoEm,
        PalavraResumoResponse palavra) {

    public static QuizPalavraResponse from(QuizPalavra item) {
        return new QuizPalavraResponse(
                item.getId(),
                item.getAcertou(),
                item.getRespondidoEm(),
                PalavraResumoResponse.from(item.getPalavra()));
    }
}
