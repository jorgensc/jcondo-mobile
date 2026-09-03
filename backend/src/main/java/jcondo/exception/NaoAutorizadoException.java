package jcondo.exception;

// 401 - token ausente, invalido ou expirado
public class NaoAutorizadoException extends RuntimeException {

    public NaoAutorizadoException(String mensagem) {
        super(mensagem);
    }
}
