package com.jcondo.mobile.api;

import android.content.Context;
import android.content.Intent;

import com.jcondo.mobile.BuildConfig;
import com.jcondo.mobile.session.SessionManager;
import com.jcondo.mobile.ui.LoginActivity;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente Retrofit do aplicativo.
 *
 * O token entra por interceptor, e nao em cada chamada: nenhuma tela precisa
 * lembrar de mandar o header Authorization. O cliente e recriado quando o
 * endereco da API muda na tela de login, por isso o cache guarda a URL.
 */
public final class ApiClient {

    private static Retrofit retrofit;
    private static String urlEmUso;
    private static Context aplicacao;

    /**
     * Trava do retorno ao login. Varias telas costumam pedir dados ao mesmo
     * tempo; sem ela, um token vencido abriria a tela de login uma vez por
     * chamada em andamento.
     */
    private static final AtomicBoolean voltandoAoLogin = new AtomicBoolean(false);

    private ApiClient() {
    }

    public static synchronized JCondoService get(Context context) {
        SessionManager sessao = SessionManager.get(context);
        String url = sessao.getApiUrl();

        // guarda o contexto da aplicacao, e nao o da tela: o interceptor roda
        // em outra thread e pode terminar depois de a Activity ter sido fechada
        aplicacao = context.getApplicationContext();

        if (retrofit == null || !url.equals(urlEmUso)) {
            retrofit = construir(url, sessao);
            urlEmUso = url;
        }
        return retrofit.create(JCondoService.class);
    }

    public static synchronized void reiniciar() {
        retrofit = null;
        urlEmUso = null;
        voltandoAoLogin.set(false);
    }

    private static Retrofit construir(String url, SessionManager sessao) {
        OkHttpClient.Builder http = new OkHttpClient.Builder()
                // timeouts curtos: numa rede ruim, e melhor avisar o morador
                // rapido do que deixar a tela girando por um minuto
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        // anexa o token em toda requisicao que nao seja o proprio login
        http.addInterceptor(chain -> {
            Request original = chain.request();
            String token = sessao.getToken();

            if (token == null || token.isEmpty()
                    || original.url().encodedPath().endsWith("/api/auth/login")) {
                return chain.proceed(original);
            }

            Request comToken = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .build();

            Response resposta = chain.proceed(comToken);

            // 401 aqui significa token vencido ou API reiniciada. Como a sessao
            // do servidor vive em memoria, isso acontece toda vez que a API e
            // parada e subida de novo. Em vez de deixar o morador preso numa
            // tela que so mostra erro, a sessao e apagada e o login volta.
            if (resposta.code() == 401) {
                sessao.encerrar();
                voltarAoLogin();
            }
            return resposta;
        });

        // o log completo do corpo so existe na build de debug
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);
            http.addInterceptor(log);
        }

        return new Retrofit.Builder()
                .baseUrl(url)
                .client(http.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Leva o aplicativo de volta ao login limpando a pilha de telas, para que o
     * botao voltar nao devolva o morador a uma tela sem sessao.
     */
    private static void voltarAoLogin() {
        Context contexto = aplicacao;
        if (contexto == null || !voltandoAoLogin.compareAndSet(false, true)) {
            return;
        }

        Intent login = new Intent(contexto, LoginActivity.class);
        login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        login.putExtra(LoginActivity.EXTRA_SESSAO_EXPIRADA, true);
        contexto.startActivity(login);
    }

    /** Chamado pela tela de login assim que ela aparece, liberando a trava. */
    public static void loginRetomado() {
        voltandoAoLogin.set(false);
    }
}
