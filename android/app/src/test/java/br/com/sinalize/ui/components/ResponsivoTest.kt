package br.com.sinalize.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponsivoTest {

    @Test
    fun `colunas do dicionario seguem xs sm md lg`() {
        assertEquals(1, Responsivo.colunasDoDicionario(360.dp))
        assertEquals(1, Responsivo.colunasDoDicionario(575.dp))
        assertEquals(2, Responsivo.colunasDoDicionario(576.dp))
        assertEquals(2, Responsivo.colunasDoDicionario(767.dp))
        assertEquals(3, Responsivo.colunasDoDicionario(768.dp))
        assertEquals(3, Responsivo.colunasDoDicionario(991.dp))
        assertEquals(4, Responsivo.colunasDoDicionario(992.dp))
        assertEquals(4, Responsivo.colunasDoDicionario(1200.dp))
    }

    @Test
    fun `alternativas do quiz ficam em uma ou duas colunas`() {
        assertEquals(1, Responsivo.colunasDasAlternativas(360.dp))
        assertEquals(2, Responsivo.colunasDasAlternativas(576.dp))
        assertEquals(2, Responsivo.colunasDasAlternativas(760.dp))
    }
}
