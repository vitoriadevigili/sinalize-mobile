package br.com.sinalize.api.service;

import br.com.sinalize.api.dto.categoria.CategoriaAtualizacaoRequest;
import br.com.sinalize.api.dto.categoria.CategoriaRequest;
import br.com.sinalize.api.dto.categoria.CategoriaResponse;
import br.com.sinalize.api.entity.Categoria;
import br.com.sinalize.api.entity.Usuario;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.repository.CategoriaRepository;
import br.com.sinalize.api.repository.PalavraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private PalavraRepository palavraRepository;

    @Mock
    private UsuarioLogadoProvider usuarioLogado;

    @InjectMocks
    private CategoriaService categoriaService;

    private Usuario admin;

    @BeforeEach
    void setUp() {
        admin = new Usuario("Administrador", "admin@sinalize.com.br", "hash");
    }

    @Test
    void criarRemoveEspacosERegistraAutor() {
        when(usuarioLogado.obter()).thenReturn(admin);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaResponse resposta = categoriaService.criar(new CategoriaRequest("  Esportes ", " Modalidades. "));

        ArgumentCaptor<Categoria> salva = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(salva.capture());
        assertThat(salva.getValue().getNome()).isEqualTo("Esportes");
        assertThat(salva.getValue().getDescricao()).isEqualTo("Modalidades.");
        assertThat(salva.getValue().getCriadoPor()).isSameAs(admin);
        assertThat(resposta.criadoPor().nome()).isEqualTo("Administrador");
        assertThat(resposta.atualizadoPor()).isNull();
    }

    @Test
    void atualizarAlteraSomenteCamposInformadosERegistraAtualizacao() {
        Categoria existente = new Categoria("Cores", "Nomes das cores.", admin);
        when(categoriaRepository.findDetalhadaById(1L)).thenReturn(Optional.of(existente));
        when(usuarioLogado.obter()).thenReturn(admin);

        CategoriaResponse resposta = categoriaService.atualizar(1L, new CategoriaAtualizacaoRequest("Cores e tons", null));

        assertThat(resposta.nome()).isEqualTo("Cores e tons");
        assertThat(resposta.descricao()).isEqualTo("Nomes das cores.");
        assertThat(resposta.atualizadoEm()).isNotNull();
        assertThat(resposta.atualizadoPor().nome()).isEqualTo("Administrador");
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(categoriaRepository.findDetalhadaById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Categoria não encontrada.");
    }

    @Test
    void excluirInexistenteLancaNaoEncontradoSemApagar() {
        when(categoriaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoriaService.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(categoriaRepository, never()).deleteById(any());
    }

    @Test
    void buscarEntidadesPorIdsIgnoraRepeticoesEMantemOrdem() {
        Categoria cores = comId(new Categoria("Cores", "d", admin), 3L);
        Categoria animais = comId(new Categoria("Animais", "d", admin), 4L);
        when(categoriaRepository.findAllById(Set.of(4L, 3L))).thenReturn(List.of(cores, animais));

        List<Categoria> resultado = categoriaService.buscarEntidadesPorIds(List.of(4L, 3L, 4L));

        assertThat(resultado).containsExactly(animais, cores);
    }

    @Test
    void buscarEntidadesPorIdsInformaQualIdNaoExiste() {
        Categoria cores = comId(new Categoria("Cores", "d", admin), 3L);
        when(categoriaRepository.findAllById(Set.of(3L, 99L))).thenReturn(List.of(cores));

        assertThatThrownBy(() -> categoriaService.buscarEntidadesPorIds(List.of(3L, 99L)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Categoria com id 99 não encontrada.");
    }

    private static Categoria comId(Categoria categoria, Long id) {
        ReflectionTestUtils.setField(categoria, "id", id);
        return categoria;
    }

    @Test
    void excluirCategoriaSemPalavrasApaga() {
        when(categoriaRepository.existsById(3L)).thenReturn(true);
        when(palavraRepository.existsByCategorias_Id(3L)).thenReturn(false);

        categoriaService.excluir(3L);

        verify(categoriaRepository).deleteById(3L);
    }

    @Test
    void excluirCategoriaComPalavrasLancaConflitoSemApagar() {
        when(categoriaRepository.existsById(3L)).thenReturn(true);
        when(palavraRepository.existsByCategorias_Id(3L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.excluir(3L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Esta categoria possui palavras vinculadas. "
                        + "Remova-as ou mude-as de categoria antes de excluir.");
        verify(categoriaRepository, never()).deleteById(any());
    }

    @Test
    void criarComNomeRepetidoLancaConflitoSemSalvar() {
        when(categoriaRepository.existsByNomeIgnoreCase("cores")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.criar(new CategoriaRequest("  cores ", "Outra descrição.")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe uma categoria com esse nome.");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void atualizarParaNomeDeOutraCategoriaLancaConflito() {
        Categoria cores = comId(new Categoria("Cores", "d", admin), 3L);
        when(categoriaRepository.findDetalhadaById(3L)).thenReturn(Optional.of(cores));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Animais", 3L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.atualizar(3L, new CategoriaAtualizacaoRequest(" Animais ", null)))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe uma categoria com esse nome.");
        assertThat(cores.getNome()).isEqualTo("Cores");
    }

    @Test
    void atualizarMudandoSoAsMaiusculasDoProprioNomeEhPermitido() {
        Categoria cores = comId(new Categoria("Cores", "d", admin), 3L);
        when(categoriaRepository.findDetalhadaById(3L)).thenReturn(Optional.of(cores));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("CORES", 3L)).thenReturn(false);
        when(usuarioLogado.obter()).thenReturn(admin);

        CategoriaResponse resposta = categoriaService.atualizar(3L, new CategoriaAtualizacaoRequest("CORES", null));

        assertThat(resposta.nome()).isEqualTo("CORES");
    }
}
