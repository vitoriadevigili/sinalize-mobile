package br.com.sinalize.api.dto.palavra;

import br.com.sinalize.api.validation.NaoVazioSePresente;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PalavraAtualizacaoRequest(

        @NaoVazioSePresente(message = "O campo palavra não pode estar em branco.")
        @Size(max = 255, message = "O campo palavra deve conter no máximo 255 caracteres.")
        String palavra,

        @Size(min = 1, message = "A palavra deve estar associada a pelo menos uma categoria.")
        List<@NotNull(message = "A lista de categorias não pode conter valores nulos.") Long> categoriaIds) {
}
