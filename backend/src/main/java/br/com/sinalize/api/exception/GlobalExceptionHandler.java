package br.com.sinalize.api.exception;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final PropertyNamingStrategies.SnakeCaseStrategy SNAKE_CASE =
            new PropertyNamingStrategies.SnakeCaseStrategy();

    private static final String MENSAGEM_DADOS_INVALIDOS = "Dados inválidos.";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNaoEncontrado(RecursoNaoEncontradoException ex,
                                                             HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErrorResponse> handleConflito(ConflitoException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponse> handleRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacaoCorpo(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<ErrorResponse.CampoErro> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErrorResponse.CampoErro(paraSnakeCase(erro.getField()), erro.getDefaultMessage()))
                .sorted(Comparator.comparing(ErrorResponse.CampoErro::campo)
                        .thenComparing(ErrorResponse.CampoErro::mensagem))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, MENSAGEM_DADOS_INVALIDOS, request, campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidacaoParametros(HandlerMethodValidationException ex,
                                                                   HttpServletRequest request) {
        List<ErrorResponse.CampoErro> campos = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> new ErrorResponse.CampoErro(
                                paraSnakeCase(resultado.getMethodParameter().getParameterName()),
                                erro.getDefaultMessage())))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, MENSAGEM_DADOS_INVALIDOS, request, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleCorpoIlegivel(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        String mensagem = switch (ex.getCause()) {
            case UnrecognizedPropertyException e -> "O campo " + e.getPropertyName() + " não é permitido.";
            case MismatchedInputException e when nomeDoCampo(e) != null ->
                    "O campo " + nomeDoCampo(e) + " possui um formato inválido.";
            case null, default -> "O corpo da requisição é inválido ou está mal formatado.";
        };
        return responder(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    private static String nomeDoCampo(MismatchedInputException ex) {
        return ex.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(Objects::nonNull)
                .reduce((anterior, ultimo) -> ultimo)
                .orElse(null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoParametro(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String mensagem = "O parâmetro " + ex.getName() + " possui um valor inválido: " + ex.getValue() + ".";
        return responder(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleParametroAusente(MissingServletRequestParameterException ex,
                                                               HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "O parâmetro " + ex.getParameterName() + " é obrigatório.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRotaInexistente(NoResourceFoundException ex,
                                                              HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Rota não encontrada.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "O método " + ex.getMethod() + " não é suportado nesta rota.", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex,
                                                        HttpServletRequest request) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de conteúdo não suportado. Use application/json.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridade(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        log.warn("Violação de integridade em {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensagem, HttpServletRequest request) {
        return responder(status, mensagem, request, List.of());
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensagem, HttpServletRequest request,
                                                    List<ErrorResponse.CampoErro> campos) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, mensagem, request.getRequestURI(), campos));
    }

    private static String paraSnakeCase(String nome) {
        return nome == null ? null : SNAKE_CASE.translate(nome);
    }
}
