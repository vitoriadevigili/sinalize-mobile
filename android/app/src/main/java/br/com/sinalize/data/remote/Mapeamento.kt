package br.com.sinalize.data.remote

import br.com.sinalize.data.model.Categoria
import br.com.sinalize.data.model.CategoriaResumo
import br.com.sinalize.data.model.Palavra
import br.com.sinalize.data.model.PalavraResumo
import br.com.sinalize.data.model.Quiz
import br.com.sinalize.data.model.QuizPalavra
import br.com.sinalize.data.model.UsuarioResumo

/* Conversão do JSON da API (DTOs) para os modelos que as telas usam. */

fun UsuarioResumoDto.paraModelo() = UsuarioResumo(id = id, nome = nome)

fun CategoriaDto.paraModelo() = Categoria(
    id = id,
    nome = nome,
    descricao = descricao,
    criadoPor = criadoPor?.paraModelo(),
    criadoEm = criadoEm,
)

fun CategoriaResumoDto.paraModelo() = CategoriaResumo(id = id, nome = nome)

fun PalavraResumoDto.paraModelo() = PalavraResumo(id = id, palavra = palavra)

fun PalavraDto.paraModelo() = Palavra(
    id = id,
    palavra = palavra,
    categorias = categorias.map { it.paraModelo() },
    criadoPor = criadoPor?.paraModelo(),
    criadoEm = criadoEm,
)

fun QuizPalavraDto.paraModelo() = QuizPalavra(id = id, palavra = palavra.paraModelo(), acertou = acertou)

fun QuizDto.paraModelo() = Quiz(id = id, quizPalavras = quizPalavras.map { it.paraModelo() })
