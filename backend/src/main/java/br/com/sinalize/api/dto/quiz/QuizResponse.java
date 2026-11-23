package br.com.sinalize.api.dto.quiz;

import br.com.sinalize.api.dto.usuario.UsuarioResumoResponse;
import br.com.sinalize.api.entity.Quiz;

import java.time.LocalDateTime;
import java.util.List;

public record QuizResponse(
        Long id,
        LocalDateTime criadoEm,
        UsuarioResumoResponse usuario,
        List<QuizPalavraResponse> quizPalavras) {

    public static QuizResponse from(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getCriadoEm(),
                UsuarioResumoResponse.from(quiz.getUsuario()),
                quiz.getItens().stream().map(QuizPalavraResponse::from).toList());
    }
}
