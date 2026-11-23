package br.com.sinalize.api.dto.usuario;

import br.com.sinalize.api.entity.Usuario;

public record UsuarioResumoResponse(Long id, String nome) {

    public static UsuarioResumoResponse from(Usuario usuario) {
        return usuario == null ? null : new UsuarioResumoResponse(usuario.getId(), usuario.getNome());
    }
}
