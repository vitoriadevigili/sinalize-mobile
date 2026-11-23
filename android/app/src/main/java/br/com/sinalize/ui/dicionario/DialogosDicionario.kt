package br.com.sinalize.ui.dicionario

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sinalize.data.model.Categoria
import br.com.sinalize.data.model.Palavra
import br.com.sinalize.ui.components.barraDeRolagem
import br.com.sinalize.ui.components.BotaoPrimario
import br.com.sinalize.ui.components.BotaoSecundario
import br.com.sinalize.ui.components.OpcaoSelecao
import br.com.sinalize.ui.components.SeletorMultiplo
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.BordaCampo
import br.com.sinalize.ui.theme.TextoEscuro
import br.com.sinalize.ui.theme.VermelhoExcluir

@Composable
fun PalavraDialog(
    palavraEditada: Palavra?,
    categorias: List<Categoria>,
    salvando: Boolean,
    onCancelar: () -> Unit,
    onSalvar: (palavra: String, categoriaIds: List<Long>) -> Unit,
) {
    val chave = palavraEditada?.id ?: -1L
    var texto by rememberSaveable(chave) { mutableStateOf(palavraEditada?.palavra.orEmpty()) }
    var selecionadas by rememberSaveable(chave) {
        mutableStateOf(palavraEditada?.categorias?.map { it.id }.orEmpty())
    }
    var tentouSalvar by rememberSaveable(chave) { mutableStateOf(false) }
    val invalido = tentouSalvar && texto.isBlank()
    val semCategoria = tentouSalvar && selecionadas.isEmpty()

    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                if (palavraEditada != null) "Editar Palavra" else "Criar Nova Palavra",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            val rolagem = rememberScrollState()
            Column(Modifier.barraDeRolagem(rolagem).verticalScroll(rolagem).padding(end = 12.dp)) {
                RotuloDeCampo("Palavra", obrigatorio = true)
                CampoDeTexto(
                    valor = texto,
                    onValor = { texto = it },
                    placeholder = "Digite aqui a sua palavra...",
                    erro = if (invalido) "Por favor, digite a palavra desejada!" else null,
                )

                RotuloDeCampo("Categorias", obrigatorio = true, modifier = Modifier.padding(top = 20.dp))
                SeletorMultiplo(
                    opcoes = categorias.map { OpcaoSelecao(it.id, it.nome) },
                    selecionadas = selecionadas,
                    onAlternar = { id ->
                        selecionadas = if (id in selecionadas) selecionadas - id else selecionadas + id
                    },
                    placeholder = "Selecione uma ou mais categorias",
                    textoSemOpcoes = "Nenhuma categoria cadastrada.",
                )
                if (semCategoria) {
                    Text(
                        "Selecione pelo menos uma categoria!",
                        color = VermelhoExcluir,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            BotaoPrimario(
                texto = if (palavraEditada != null) "Salvar Alterações" else "Adicionar",
                enabled = !salvando,
                onClick = {
                    tentouSalvar = true
                    if (texto.isNotBlank() && selecionadas.isNotEmpty()) onSalvar(texto.trim(), selecionadas)
                },
            )
        },
        dismissButton = { BotaoSecundario("Cancelar", onCancelar) },
    )
}

@Composable
fun CategoriaDialog(
    categoriaEditada: Categoria?,
    salvando: Boolean,
    onCancelar: () -> Unit,
    onSalvar: (nome: String, descricao: String) -> Unit,
) {
    val chave = categoriaEditada?.id ?: -1L
    var nome by rememberSaveable(chave) { mutableStateOf(categoriaEditada?.nome.orEmpty()) }
    var descricao by rememberSaveable(chave) { mutableStateOf(categoriaEditada?.descricao.orEmpty()) }
    var tentouSalvar by rememberSaveable(chave) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                if (categoriaEditada != null) "Editar Categoria" else "Criar Nova Categoria",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            val rolagem = rememberScrollState()
            Column(Modifier.barraDeRolagem(rolagem).verticalScroll(rolagem).padding(end = 12.dp)) {
                RotuloDeCampo("Categoria", obrigatorio = true)
                CampoDeTexto(
                    valor = nome,
                    onValor = { nome = it },
                    placeholder = "Digite aqui o nome da categoria...",
                    erro = if (tentouSalvar && nome.isBlank()) "Por favor, digite o nome da categoria!" else null,
                )
                RotuloDeCampo("Descrição", obrigatorio = true, modifier = Modifier.padding(top = 20.dp))
                CampoDeTexto(
                    valor = descricao,
                    onValor = { descricao = it },
                    placeholder = "Digite uma descrição para a categoria...",
                    erro = if (tentouSalvar && descricao.isBlank()) "Por favor, digite a descrição!" else null,
                    linhas = 3,
                )
            }
        },
        confirmButton = {
            BotaoPrimario(
                texto = if (categoriaEditada != null) "Salvar Alterações" else "Adicionar",
                enabled = !salvando,
                onClick = {
                    tentouSalvar = true
                    if (nome.isNotBlank() && descricao.isNotBlank()) onSalvar(nome.trim(), descricao.trim())
                },
            )
        },
        dismissButton = { BotaoSecundario("Cancelar", onCancelar) },
    )
}

@Composable
private fun RotuloDeCampo(texto: String, modifier: Modifier = Modifier, obrigatorio: Boolean = false) {
    Text(
        text = buildAnnotatedString {
            if (obrigatorio) withStyle(SpanStyle(color = VermelhoExcluir)) { append("* ") }
            append(texto)
        },
        fontSize = 15.sp,
        color = TextoEscuro,
        modifier = modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun CampoDeTexto(
    valor: String,
    onValor: (String) -> Unit,
    placeholder: String,
    erro: String?,
    linhas: Int = 1,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFFBFBFBF)) },
        singleLine = linhas == 1,
        minLines = linhas,
        isError = erro != null,
        supportingText = erro?.let { { Text(it) } },
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AzulSinalize,
            unfocusedBorderColor = BordaCampo,
            errorBorderColor = VermelhoExcluir,
            errorSupportingTextColor = VermelhoExcluir,
            cursorColor = AzulSinalize,
        ),
    )
}
