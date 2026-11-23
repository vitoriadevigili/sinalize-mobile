package br.com.sinalize.api.service;

import br.com.sinalize.api.dto.palavra.PalavraAtualizacaoRequest;
import br.com.sinalize.api.dto.palavra.PalavraRequest;
import br.com.sinalize.api.dto.palavra.PalavraResponse;
import br.com.sinalize.api.dto.palavra.PalavraResumoResponse;
import br.com.sinalize.api.entity.Categoria;
import br.com.sinalize.api.entity.Palavra;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.repository.PalavraRepository;
import br.com.sinalize.api.repository.QuizPalavraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PalavraService {

    static final String PALAVRA_NAO_ENCONTRADA = "Palavra não encontrada.";
    static final String PALAVRA_USADA_EM_QUIZ =
            "Não é possível excluir a palavra: ela já foi usada em um quiz.";
    static final String PALAVRA_DUPLICADA = "Já existe uma palavra com esse nome.";

    private final PalavraRepository palavraRepository;
    private final QuizPalavraRepository quizPalavraRepository;
    private final CategoriaService categoriaService;
    private final UsuarioLogadoProvider usuarioLogado;

    public PalavraService(PalavraRepository palavraRepository,
                          QuizPalavraRepository quizPalavraRepository,
                          CategoriaService categoriaService,
                          UsuarioLogadoProvider usuarioLogado) {
        this.palavraRepository = palavraRepository;
        this.quizPalavraRepository = quizPalavraRepository;
        this.categoriaService = categoriaService;
        this.usuarioLogado = usuarioLogado;
    }

    public List<PalavraResponse> listar() {
        return palavraRepository.findAllByOrderByCriadoEmAscIdAsc().stream()
                .map(PalavraResponse::from)
                .toList();
    }

    public PalavraResponse buscar(Long id) {
        return PalavraResponse.from(buscarEntidade(id));
    }

    public List<PalavraResumoResponse> sortearAleatorias(Collection<Long> excluir, int quantidade) {
        List<Palavra> sorteadas = excluir.isEmpty()
                ? palavraRepository.sortear(quantidade)
                : palavraRepository.sortearExcluindo(excluir, quantidade);

        return sorteadas.stream()
                .map(PalavraResumoResponse::from)
                .toList();
    }

    @Transactional
    public PalavraResponse criar(PalavraRequest request) {
        String texto = request.palavra().trim();
        if (palavraRepository.existsByPalavraIgnoreCase(texto)) {
            throw new ConflitoException(PALAVRA_DUPLICADA);
        }
        List<Categoria> categorias = categoriaService.buscarEntidadesPorIds(request.categoriaIds());

        Palavra palavra = new Palavra(texto, categorias, usuarioLogado.obter());

        return PalavraResponse.from(palavraRepository.save(palavra));
    }

    @Transactional
    public PalavraResponse atualizar(Long id, PalavraAtualizacaoRequest request) {
        Palavra palavra = buscarEntidade(id);

        if (request.palavra() != null) {
            String texto = request.palavra().trim();
            if (palavraRepository.existsByPalavraIgnoreCaseAndIdNot(texto, id)) {
                throw new ConflitoException(PALAVRA_DUPLICADA);
            }
            palavra.setPalavra(texto);
        }
        if (request.categoriaIds() != null) {
            palavra.substituirCategorias(categoriaService.buscarEntidadesPorIds(request.categoriaIds()));
        }
        palavra.registrarAtualizacao(usuarioLogado.obter());

        return PalavraResponse.from(palavra);
    }

    @Transactional
    public void excluir(Long id) {
        if (!palavraRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(PALAVRA_NAO_ENCONTRADA);
        }
        if (quizPalavraRepository.existsByPalavraId(id)) {
            throw new ConflitoException(PALAVRA_USADA_EM_QUIZ);
        }
        palavraRepository.deleteById(id);
    }

    private Palavra buscarEntidade(Long id) {
        return palavraRepository.findDetalhadaById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(PALAVRA_NAO_ENCONTRADA));
    }
}
