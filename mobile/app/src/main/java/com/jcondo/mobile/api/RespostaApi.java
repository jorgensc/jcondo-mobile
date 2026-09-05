package com.jcondo.mobile.api;

import android.content.Context;

import com.google.gson.Gson;
import com.jcondo.mobile.R;
import com.jcondo.mobile.api.model.ErroApi;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Response;

/**
 * Traduz falha de rede e resposta de erro da API em frase que o morador entende.
 * Sem isso, uma queda de Wi-Fi apareceria como "java.net.ConnectException" e um
 * horario ocupado como "HTTP 409".
 */
public final class RespostaApi {

    private static final Gson GSON = new Gson();

    private RespostaApi() {
    }

    /** Mensagem para uma resposta HTTP que veio com codigo de erro. */
    public static String mensagemDeErro(Context contexto, Response<?> resposta) {
        String doServidor = extrairMensagem(resposta);
        if (doServidor != null && !doServidor.trim().isEmpty()) {
            return doServidor;
        }
        if (resposta != null && resposta.code() == 401) {
            return contexto.getString(R.string.erro_sessao_expirada);
        }
        if (resposta != null && resposta.code() >= 500) {
            return contexto.getString(R.string.erro_servidor_indisponivel);
        }
        return contexto.getString(R.string.erro_generico);
    }

    /** Mensagem para uma falha antes da resposta chegar (rede, DNS, timeout). */
    public static String mensagemDeFalha(Context contexto, Throwable erro) {
        if (erro instanceof UnknownHostException
                || erro instanceof ConnectException
                || erro instanceof SocketTimeoutException) {
            return contexto.getString(R.string.erro_servidor_indisponivel);
        }
        if (erro instanceof IOException) {
            return contexto.getString(R.string.erro_sem_conexao);
        }
        return contexto.getString(R.string.erro_generico);
    }

    /** true quando o token nao vale mais e o app precisa voltar ao login. */
    public static boolean sessaoExpirou(Response<?> resposta) {
        return resposta != null && resposta.code() == 401;
    }

    /** Le o campo "mensagem" do corpo de erro padronizado do backend. */
    private static String extrairMensagem(Response<?> resposta) {
        if (resposta == null || resposta.errorBody() == null) {
            return null;
        }
        try {
            String corpo = resposta.errorBody().string();
            if (corpo.trim().isEmpty()) {
                return null;
            }
            ErroApi erro = GSON.fromJson(corpo, ErroApi.class);
            return erro == null ? null : erro.mensagem;
        } catch (Exception e) {
            // corpo que nao e o JSON esperado (uma pagina de erro do Tomcat,
            // por exemplo): cai na mensagem generica
            return null;
        }
    }
}
