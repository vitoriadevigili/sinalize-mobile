package br.com.sinalize.ui.dicionario

import org.junit.Assert.assertEquals
import org.junit.Test

class FiltroDicionarioTest {

    private val itens = listOf(
        ItemDicionario(1, "Banana"),
        ItemDicionario(2, "Água"),
        ItemDicionario(3, "azul"),
        ItemDicionario(4, "Café"),
        ItemDicionario(5, "Cachorro"),
    )

    private fun nomes(lista: List<ItemDicionario>) = lista.map { it.nome }

    @Test
    fun `ordena de A a Z respeitando acentos e maiusculas`() {
        val resultado = filtrarEOrdenar(itens, busca = "", letra = null, ordenacao = Ordenacao.AZ)
        assertEquals(listOf("Água", "azul", "Banana", "Cachorro", "Café"), nomes(resultado))
    }

    @Test
    fun `ordena de Z a A`() {
        val resultado = filtrarEOrdenar(itens, busca = "", letra = null, ordenacao = Ordenacao.ZA)
        assertEquals(listOf("Café", "Cachorro", "Banana", "azul", "Água"), nomes(resultado))
    }

    @Test
    fun `mais recentes vem primeiro pelo maior id`() {
        val resultado = filtrarEOrdenar(itens, busca = "", letra = null, ordenacao = Ordenacao.RECENTES)
        assertEquals(listOf(5L, 4L, 3L, 2L, 1L), resultado.map { it.id })
    }

    @Test
    fun `letra filtra pela inicial ignorando acento`() {
        val resultado = filtrarEOrdenar(itens, busca = "", letra = 'A', ordenacao = Ordenacao.AZ)
        assertEquals(listOf("Água", "azul"), nomes(resultado))
    }

    @Test
    fun `busca encontra trecho no meio do nome sem acento`() {
        val resultado = filtrarEOrdenar(itens, busca = "cafe", letra = null, ordenacao = Ordenacao.AZ)
        assertEquals(listOf("Café"), nomes(resultado))
    }

    @Test
    fun `busca e letra combinadas`() {
        val resultado = filtrarEOrdenar(itens, busca = "a", letra = 'C', ordenacao = Ordenacao.AZ)
        assertEquals(listOf("Cachorro", "Café"), nomes(resultado))
    }

    @Test
    fun `sem resultados devolve lista vazia`() {
        val resultado = filtrarEOrdenar(itens, busca = "xyz", letra = null, ordenacao = Ordenacao.AZ)
        assertEquals(emptyList<ItemDicionario>(), resultado)
    }
}
