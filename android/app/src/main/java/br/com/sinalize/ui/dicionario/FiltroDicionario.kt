package br.com.sinalize.ui.dicionario

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

enum class Aba(val rotulo: String) { PALAVRAS("Palavras"), CATEGORIAS("Categorias") }

enum class Ordenacao(val rotulo: String) { AZ("A-Z"), ZA("Z-A"), RECENTES("Mais recentes") }

data class ItemDicionario(val id: Long, val nome: String)

private val collatorPtBr: Collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"))

internal fun normalizar(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase(Locale.ROOT)

fun filtrarEOrdenar(
    itens: List<ItemDicionario>,
    busca: String,
    letra: Char?,
    ordenacao: Ordenacao,
): List<ItemDicionario> {
    val termo = normalizar(busca.trim())
    val inicial = letra?.let { normalizar(it.toString()) }

    val filtrados = itens.filter { item ->
        val nome = normalizar(item.nome)
        nome.contains(termo) && (inicial == null || nome.startsWith(inicial))
    }

    return when (ordenacao) {
        Ordenacao.AZ -> filtrados.sortedWith { a, b -> collatorPtBr.compare(a.nome, b.nome) }
        Ordenacao.ZA -> filtrados.sortedWith { a, b -> collatorPtBr.compare(b.nome, a.nome) }
        Ordenacao.RECENTES -> filtrados.sortedByDescending { it.id }
    }
}
