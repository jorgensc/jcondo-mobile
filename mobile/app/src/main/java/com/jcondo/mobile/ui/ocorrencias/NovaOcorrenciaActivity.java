package com.jcondo.mobile.ui.ocorrencias;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.ApiClient;
import com.jcondo.mobile.api.RespostaApi;
import com.jcondo.mobile.api.model.Ocorrencia;
import com.jcondo.mobile.api.model.OcorrenciaRequest;
import com.jcondo.mobile.databinding.ActivityNovaOcorrenciaBinding;
import com.jcondo.mobile.util.Ui;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Abertura de ocorrencia.
 *
 * As categorias vem da propria API, para nao existirem duas listas
 * divergentes (uma no app e outra no servidor). Se a chamada falhar, uma
 * lista local equivalente entra no lugar - o morador consegue abrir o
 * chamado mesmo com a rede instavel no momento de carregar o formulario.
 */
public class NovaOcorrenciaActivity extends AppCompatActivity {

    private static final List<String> CATEGORIAS_PADRAO = Arrays.asList(
            "Iluminação", "Limpeza", "Elevador", "Manutenção", "Segurança",
            "Barulho", "Área comum", "Outros");

    private ActivityNovaOcorrenciaBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNovaOcorrenciaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.barra.setNavigationOnClickListener(v -> finish());
        binding.enviarBotao.setOnClickListener(v -> enviar());

        aplicarCategorias(CATEGORIAS_PADRAO);
        carregarCategorias();
    }

    private void carregarCategorias() {
        ApiClient.get(this).categoriasOcorrencia().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> resposta) {
                if (resposta.isSuccessful() && resposta.body() != null
                        && !resposta.body().isEmpty()) {
                    aplicarCategorias(resposta.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable erro) {
                // segue com a lista local; nao vale interromper o morador por isso
            }
        });
    }

    private void aplicarCategorias(List<String> categorias) {
        binding.categoriaInput.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, categorias));
    }

    private void enviar() {
        Ui.esconderTeclado(this);

        String categoria = texto(binding.categoriaInput.getText());
        String titulo = texto(binding.assuntoInput.getText());
        String descricao = texto(binding.descricaoInput.getText());

        binding.categoriaLayout.setError(null);
        binding.assuntoLayout.setError(null);
        binding.descricaoLayout.setError(null);

        boolean valido = true;
        if (categoria.isEmpty()) {
            binding.categoriaLayout.setError(getString(R.string.nova_ocorrencia_erro_categoria));
            valido = false;
        }
        if (titulo.isEmpty()) {
            binding.assuntoLayout.setError(getString(R.string.nova_ocorrencia_erro_assunto));
            valido = false;
        }
        if (descricao.isEmpty()) {
            binding.descricaoLayout.setError(getString(R.string.nova_ocorrencia_erro_descricao));
            valido = false;
        }
        if (!valido) {
            return;
        }

        enviando(true);

        ApiClient.get(this)
                .abrirOcorrencia(new OcorrenciaRequest(categoria, titulo, descricao))
                .enqueue(new Callback<Ocorrencia>() {
                    @Override
                    public void onResponse(@NonNull Call<Ocorrencia> call,
                                           @NonNull Response<Ocorrencia> resposta) {
                        enviando(false);
                        if (resposta.isSuccessful() && resposta.body() != null) {
                            mostrarProtocolo(resposta.body());
                        } else {
                            Ui.aviso(binding.getRoot(), RespostaApi.mensagemDeErro(
                                    NovaOcorrenciaActivity.this, resposta));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Ocorrencia> call,
                                          @NonNull Throwable erro) {
                        enviando(false);
                        Ui.aviso(binding.getRoot(), RespostaApi.mensagemDeFalha(
                                NovaOcorrenciaActivity.this, erro));
                    }
                });
    }

    private void enviando(boolean ligado) {
        binding.enviarProgresso.setVisibility(ligado ? View.VISIBLE : View.GONE);
        binding.enviarBotao.setEnabled(!ligado);
    }

    /** O protocolo e a prova de que o chamado entrou - por isso vira dialogo. */
    private void mostrarProtocolo(Ocorrencia ocorrencia) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.nova_ocorrencia_titulo)
                .setMessage(getString(R.string.ocorrencia_registrada, ocorrencia.protocolo))
                .setPositiveButton(R.string.acao_entendi, (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private static String texto(Editable editable) {
        return editable == null ? "" : editable.toString().trim();
    }
}
