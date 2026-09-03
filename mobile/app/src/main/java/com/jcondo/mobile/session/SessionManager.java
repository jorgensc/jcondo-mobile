package com.jcondo.mobile.session;

import android.content.Context;
import android.content.SharedPreferences;

import com.jcondo.mobile.BuildConfig;
import com.jcondo.mobile.api.model.Morador;

/**
 * Guarda a sessao do morador entre execucoes do aplicativo.
 *
 * Fica no SharedPreferences: token, dados basicos do usuario, endereco da API
 * e a preferencia de notificacao. Isso e o que permite o app abrir direto na
 * tela inicial no segundo acesso, sem pedir a senha de novo.
 *
 * Nao guardamos a senha do morador em lugar nenhum - so o token, que expira
 * em 12h no servidor e e apagado no logout.
 */
public class SessionManager {

    private static final String ARQUIVO = "jcondo_sessao";

    private static final String K_TOKEN = "token";
    private static final String K_ID = "morador_id";
    private static final String K_NOME = "morador_nome";
    private static final String K_EMAIL = "morador_email";
    private static final String K_TELEFONE = "morador_telefone";
    private static final String K_BLOCO = "morador_bloco";
    private static final String K_APTO = "morador_apartamento";
    private static final String K_UNIDADE = "morador_unidade";
    private static final String K_PERFIL = "morador_perfil";
    private static final String K_API = "api_url";
    private static final String K_NOTIFICAR = "notificar_avisos";
    private static final String K_ULTIMO_AVISO = "ultimo_aviso_visto";

    private static SessionManager instancia;

    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager get(Context context) {
        if (instancia == null) {
            instancia = new SessionManager(context);
        }
        return instancia;
    }

    // ----- sessao ----------------------------------------------------------

    public void salvarSessao(String token, Morador morador) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString(K_TOKEN, token);
        if (morador != null) {
            e.putLong(K_ID, morador.id == null ? 0L : morador.id);
            e.putString(K_NOME, morador.nome);
            e.putString(K_EMAIL, morador.email);
            e.putString(K_TELEFONE, morador.telefone);
            e.putString(K_BLOCO, morador.bloco);
            e.putString(K_APTO, morador.apartamento);
            e.putString(K_UNIDADE, morador.unidade);
            e.putString(K_PERFIL, morador.perfil);
        }
        e.apply();
    }

    public void atualizarMorador(Morador morador) {
        salvarSessao(getToken(), morador);
    }

    public String getToken() {
        return prefs.getString(K_TOKEN, null);
    }

    public boolean estaLogado() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public Morador getMorador() {
        if (!estaLogado()) {
            return null;
        }
        Morador m = new Morador();
        m.id = prefs.getLong(K_ID, 0L);
        m.nome = prefs.getString(K_NOME, "");
        m.email = prefs.getString(K_EMAIL, "");
        m.telefone = prefs.getString(K_TELEFONE, "");
        m.bloco = prefs.getString(K_BLOCO, "");
        m.apartamento = prefs.getString(K_APTO, "");
        m.unidade = prefs.getString(K_UNIDADE, "");
        m.perfil = prefs.getString(K_PERFIL, "MORADOR");
        return m;
    }

    /** Apaga a sessao, mas preserva o endereco da API e as preferencias. */
    public void encerrar() {
        prefs.edit()
                .remove(K_TOKEN).remove(K_ID).remove(K_NOME).remove(K_EMAIL)
                .remove(K_TELEFONE).remove(K_BLOCO).remove(K_APTO)
                .remove(K_UNIDADE).remove(K_PERFIL)
                .apply();
    }

    // ----- endereco da API -------------------------------------------------

    /**
     * O endereco e configuravel na tela de login porque o mesmo APK precisa
     * rodar no emulador (10.0.2.2) e num celular fisico apontando para o IP
     * do computador na rede local, sem recompilar.
     */
    public String getApiUrl() {
        return prefs.getString(K_API, BuildConfig.API_URL);
    }

    public void setApiUrl(String url) {
        prefs.edit().putString(K_API, normalizar(url)).apply();
    }

    public static String normalizar(String url) {
        if (url == null || url.trim().isEmpty()) {
            return BuildConfig.API_URL;
        }
        String limpa = url.trim();
        if (!limpa.startsWith("http://") && !limpa.startsWith("https://")) {
            limpa = "http://" + limpa;
        }
        if (!limpa.endsWith("/")) {
            limpa = limpa + "/";
        }
        return limpa;
    }

    // ----- preferencias ----------------------------------------------------

    public boolean isNotificarAvisos() {
        return prefs.getBoolean(K_NOTIFICAR, true);
    }

    public void setNotificarAvisos(boolean valor) {
        prefs.edit().putBoolean(K_NOTIFICAR, valor).apply();
    }

    public long getUltimoAvisoVisto() {
        return prefs.getLong(K_ULTIMO_AVISO, 0L);
    }

    public void setUltimoAvisoVisto(long avisoId) {
        prefs.edit().putLong(K_ULTIMO_AVISO, avisoId).apply();
    }
}
