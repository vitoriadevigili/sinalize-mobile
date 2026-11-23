package br.com.sinalize.api.dto.categoria;

import br.com.sinalize.api.validation.NaoVazioSePresente;
import jakarta.validation.constraints.Size;

public record CategoriaAtualizacaoRequest(

        @NaoVazioSePresente(message = "O campo nome não pode estar em branco.")
        @Size(max = 255, message = "O campo nome deve conter no máximo 255 caracteres.")
        String nome,

        @NaoVazioSePresente(message = "A descrição não pode estar em branco. branco.")
        @Size(max = 400, message = "A descrição deve ter no máximo 400 caracteres.")
        String descricao) {
}
