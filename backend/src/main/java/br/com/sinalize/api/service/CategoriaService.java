package br.com.sinalize.api.service;

import br.com.sinalize.api.dto.categoria.CategoriaAtualizacaoRequest;
import br.com.sinalize.api.dto.categoria.CategoriaRequest;
import br.com.sinalize.api.dto.categoria.CategoriaResponse;
import br.com.sinalize.api.entity.Categoria;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.repository.CategoriaRepository;
import br.com.sinalize.api.repository.PalavraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    static final String CATEGORIA_NAO_ENCONTRADA = "Categoria não encontrada.";
    static final String CATEGORIA_DUPLICADA = "Já existe uma categoria com esse nome.";
    static final String CATEGORIA_COM_PALAVRAS =
            "Esta categoria possui palavras vinculadas. Remova-as ou mude-as de categoria antes de excluir.";

    private final CategoriaRepository categoriaRepository;
    private final PalavraRepository palavraRepository;
    private final UsuarioLogadoProvider usuarioLogado;

    public CategoriaService(CategoriaRepository categoriaRepository,
                            PalavraRepository palavraRepository,
                            UsuarioLogadoProvider usuarioLogado) {
        this.categoriaRepository = categoriaRepository;
        this.palavraRepository = palavraRepository;
        this.usuarioLogado = usuarioLogado;
    }

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByCriadoEmAscIdAsc().stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException(CATEGORIA_DUPLICADA);
        }
        Categoria categoria = new Categoria(
                nome,
                request.descricao().trim(),
                usuarioLogado.obter());
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaAtualizacaoRequest request) {
        Categoria categoria = buscarEntidade(id);

        if (request.nome() != null) {
            String nome = request.nome().trim();
            if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
                throw new ConflitoException(CATEGORIA_DUPLICADA);
            }
            categoria.setNome(nome);
        }
        if (request.descricao() != null) {
            categoria.setDescricao(request.descricao().trim());
        }
        categoria.registrarAtualizacao(usuarioLogado.obter());

        return CategoriaResponse.from(categoria);
    }

    /**
     * Exclui a categoria. Categorias com palavras vinculadas são mantidas: excluí-las
     * poderia deixar palavras sem nenhuma categoria, o que a regra de palavras não permite.
     */
    @Transactional
    public void excluir(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(CATEGORIA_NAO_ENCONTRADA);
        }
        if (palavraRepository.existsByCategorias_Id(id)) {
            throw new ConflitoException(CATEGORIA_COM_PALAVRAS);
        }
        categoriaRepository.deleteById(id);
    }

    /**
     * Busca as categorias pelos ids informados (ignorando repetições), para uso por outros serviços.
     *
     * @throws RecursoNaoEncontradoException se algum id não existir
     */
    public List<Categoria> buscarEntidadesPorIds(Collection<Long> ids) {
        Set<Long> idsUnicos = new LinkedHashSet<>(ids);
        Map<Long, Categoria> encontradas = categoriaRepository.findAllById(idsUnicos).stream()
                .collect(Collectors.toMap(Categoria::getId, Function.identity()));

        return idsUnicos.stream()
                .map(id -> Optional.ofNullable(encontradas.get(id))
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Categoria com id " + id + " não encontrada.")))
                .toList();
    }

    private Categoria buscarEntidade(Long id) {
        return categoriaRepository.findDetalhadaById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(CATEGORIA_NAO_ENCONTRADA));
    }
}
