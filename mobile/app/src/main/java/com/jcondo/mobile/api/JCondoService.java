package com.jcondo.mobile.api;

import com.jcondo.mobile.api.model.AreaComum;
import com.jcondo.mobile.api.model.AtualizacaoPerfil;
import com.jcondo.mobile.api.model.Aviso;
import com.jcondo.mobile.api.model.Horario;
import com.jcondo.mobile.api.model.LoginRequest;
import com.jcondo.mobile.api.model.LoginResponse;
import com.jcondo.mobile.api.model.Morador;
import com.jcondo.mobile.api.model.Ocorrencia;
import com.jcondo.mobile.api.model.OcorrenciaRequest;
import com.jcondo.mobile.api.model.Reserva;
import com.jcondo.mobile.api.model.ReservaRequest;
import com.jcondo.mobile.api.model.ResumoHome;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Contrato da API REST. Cada metodo corresponde a um endpoint do backend.
 * O token e anexado pelo interceptor do ApiClient, por isso nenhuma assinatura
 * o recebe como parametro.
 */
public interface JCondoService {

    // ----- autenticacao ----------------------------------------------------

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest credenciais);

    @POST("api/auth/logout")
    Call<Void> logout();

    // ----- tela inicial e perfil -------------------------------------------

    @GET("api/home/resumo")
    Call<ResumoHome> resumoHome();

    @GET("api/perfil")
    Call<Morador> perfil();

    @PUT("api/perfil")
    Call<Morador> atualizarPerfil(@Body AtualizacaoPerfil dados);

    // ----- avisos ----------------------------------------------------------

    @GET("api/avisos")
    Call<List<Aviso>> avisos();

    @GET("api/avisos/{id}")
    Call<Aviso> aviso(@Path("id") long id);

    // ----- areas comuns e agenda -------------------------------------------

    @GET("api/areas")
    Call<List<AreaComum>> areas();

    @GET("api/areas/{id}/horarios")
    Call<List<Horario>> horarios(@Path("id") long areaId, @Query("data") String dataIso);

    // ----- reservas --------------------------------------------------------

    @GET("api/reservas/minhas")
    Call<List<Reserva>> minhasReservas();

    @POST("api/reservas")
    Call<Reserva> criarReserva(@Body ReservaRequest reserva);

    @DELETE("api/reservas/{id}")
    Call<Reserva> cancelarReserva(@Path("id") long id);

    // ----- ocorrencias -----------------------------------------------------

    @GET("api/ocorrencias/minhas")
    Call<List<Ocorrencia>> minhasOcorrencias();

    @GET("api/ocorrencias/categorias")
    Call<List<String>> categoriasOcorrencia();

    @POST("api/ocorrencias")
    Call<Ocorrencia> abrirOcorrencia(@Body OcorrenciaRequest ocorrencia);
}
