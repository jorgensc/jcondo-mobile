package jcondo.exception;

// 409 - o horario pedido bate com uma reserva ja confirmada
public class ConflitoReservaException extends RuntimeException {

    public ConflitoReservaException(String mensagem) {
        super(mensagem);
    }
}
