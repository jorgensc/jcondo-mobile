package jcondo.exception;

// 400 - a requisicao esta bem formada mas fere uma regra do condominio
// (reserva no passado, horario fora do funcionamento da area, etc.)
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
