package br.com.sinalize.data.fake

import br.com.sinalize.data.model.Categoria
import br.com.sinalize.data.model.CategoriaResumo
import br.com.sinalize.data.model.Palavra
import br.com.sinalize.data.model.UsuarioResumo

object DadosIlustrativos {

    val administrador = UsuarioResumo(id = 1, nome = "Administrador")

    val categorias: List<Categoria> = listOf(
        Categoria(1, "Saudações", "Cumprimentos e expressões de cortesia do dia a dia.", administrador),
        Categoria(2, "Família", "Membros da família e parentes.", administrador),
        Categoria(3, "Cores", "Nomes das cores mais comuns.", administrador),
        Categoria(4, "Animais", "Animais domésticos e selvagens.", administrador),
        Categoria(5, "Alimentos", "Comidas e bebidas do cotidiano.", administrador),
        Categoria(6, "Lugares", "Locais e ambientes frequentados no dia a dia.", administrador),
    )

    private val palavrasPorCategoria: List<Pair<String, List<Long>>> = listOf(
        "Oi" to listOf(1L), "Tchau" to listOf(1L), "Obrigado" to listOf(1L),
        "Bom dia" to listOf(1L), "Boa noite" to listOf(1L), "Desculpa" to listOf(1L),
        "Por favor" to listOf(1L),

        "Mãe" to listOf(2L), "Pai" to listOf(2L), "Irmão" to listOf(2L), "Irmã" to listOf(2L),
        "Avô" to listOf(2L), "Avó" to listOf(2L), "Filho" to listOf(2L), "Tio" to listOf(2L),

        "Azul" to listOf(3L), "Vermelho" to listOf(3L), "Verde" to listOf(3L), "Amarelo" to listOf(3L),
        "Branco" to listOf(3L), "Preto" to listOf(3L), "Rosa" to listOf(3L),
        "Laranja" to listOf(3L, 5L),

        "Cachorro" to listOf(4L), "Gato" to listOf(4L), "Cavalo" to listOf(4L), "Peixe" to listOf(4L),
        "Pássaro" to listOf(4L), "Vaca" to listOf(4L), "Macaco" to listOf(4L),

        "Arroz" to listOf(5L), "Feijão" to listOf(5L), "Pão" to listOf(5L), "Leite" to listOf(5L),
        "Café" to listOf(5L), "Maçã" to listOf(5L), "Banana" to listOf(5L), "Água" to listOf(5L),

        "Casa" to listOf(6L), "Escola" to listOf(6L), "Hospital" to listOf(6L), "Igreja" to listOf(6L),
        "Praia" to listOf(6L), "Cidade" to listOf(6L),
    )

    val palavras: List<Palavra> = palavrasPorCategoria.mapIndexed { indice, (texto, ids) ->
        Palavra(
            id = indice + 1L,
            palavra = texto,
            categorias = ids.map { id -> categorias.first { it.id == id }.resumo() },
            criadoPor = administrador,
        )
    }
}

fun Categoria.resumo() = CategoriaResumo(id = id, nome = nome, descricao = descricao)
