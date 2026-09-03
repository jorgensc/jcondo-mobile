package jcondo.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jcondo.dto.ErroResponse;

/**
 * Converte qualquer excecao da API num JSON unico {status, erro, mensagem}.
 *
 * Isso e o que permite o app mostrar "Este horário já está reservado" em vez
 * de "HTTP 500" - a mensagem que o usuario le vem pronta do servidor.
 */
// so vale para os @RestController: as telas Thymeleaf continuam
// tratando erro do jeito delas, com mensagem na propria pagina
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(NaoAutorizadoException.class)
    public ResponseEntity<ErroResponse> naoAutorizado(NaoAutorizadoException e) {
        return montar(HttpStatus.UNAUTHORIZED, "nao_autorizado", e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException e) {
        return montar(HttpStatus.NOT_FOUND, "nao_encontrado", e.getMessage());
    }

    @ExceptionHandler(ConflitoReservaException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoReservaException e) {
        return montar(HttpStatus.CONFLICT, "conflito_reserva", e.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException e) {
        return montar(HttpStatus.BAD_REQUEST, "regra_negocio", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> argumentoInvalido(IllegalArgumentException e) {
        return montar(HttpStatus.NOT_FOUND, "nao_encontrado", e.getMessage());
    }

    // erros do @Valid: devolve campo a campo, e o app marca o input errado
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        String primeira = campos.values().stream().findFirst()
                .orElse("Verifique os dados informados.");
        return ResponseEntity.badRequest()
                .body(ErroResponse.de(400, "validacao", primeira, campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e) {
        return montar(HttpStatus.BAD_REQUEST, "corpo_invalido",
                "Não foi possível ler os dados enviados. Confira o formato de datas e horários.");
    }

    // rede de seguranca: nada de stack trace vazando pro cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> generico(Exception e) {
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "erro_interno",
                "Erro inesperado no servidor. Tente novamente em instantes.");
    }

    private ResponseEntity<ErroResponse> montar(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status)
                .body(ErroResponse.de(status.value(), erro, mensagem));
    }
}
