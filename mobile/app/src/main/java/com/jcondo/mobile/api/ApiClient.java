package com.jcondo.mobile.api;

import android.content.Context;

import com.jcondo.mobile.BuildConfig;
import com.jcondo.mobile.session.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Monta e reaproveita o cliente Retrofit do aplicativo.
 *
 * Dois pontos importantes:
 *
 * 1) o token de sessao entra num interceptor, e nao em cada chamada. Assim
 *    nenhuma tela precisa lembrar de enviar o header Authorization, e trocar
 *    a forma de autenticacao no futuro (JWT, por exemplo) mexe em um lugar so;
 *
 * 2) a instancia e recriada quando o morador muda o endereco da API na tela
 *    de login - por isso o cache guarda tambem a URL que gerou o cliente.
 */
public final class ApiClient {

    private static Retrofit retrofit;
    private static String urlEmUso;

    private ApiClient() {
    }

    public static synchronized JCondoService get(Context context) {
        SessionManager sessao = SessionManager.get(context);
        String url = sessao.getApiUrl();

        if (retrofit == null || !url.equals(urlEmUso)) {
            retrofit = construir(url, sessao);
            urlEmUso = url;
        }
        return retrofit.create(JCondoService.class);
    }

    /** Força a recriação do cliente (usado quando o endereço da API muda). */
    public static synchronized void reiniciar() {
        retrofit = null;
        urlEmUso = null;
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
            return chain.proceed(comToken);
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
}
