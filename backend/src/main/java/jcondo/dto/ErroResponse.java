package jcondo.dto;

import java.time.LocalDateTime;
import java.util.Map;

// Formato unico de erro da API. O app le o campo "mensagem" e mostra na tela,
// em vez de exibir stack trace ou codigo cru pro usuario.
public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        Map<String, String> campos,
        LocalDateTime momento) {

    public static ErroResponse de(int status, String erro, String mensagem) {
        return new ErroResponse(status, erro, mensagem, null, LocalDateTime.now());
    }

    public static ErroResponse de(int status, String erro, String mensagem,
                                  Map<String, String> campos) {
        return new ErroResponse(status, erro, mensagem, campos, LocalDateTime.now());
    }
}
