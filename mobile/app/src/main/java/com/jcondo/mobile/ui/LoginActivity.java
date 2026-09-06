package com.jcondo.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.ApiClient;
import com.jcondo.mobile.api.RespostaApi;
import com.jcondo.mobile.api.model.LoginRequest;
import com.jcondo.mobile.api.model.LoginResponse;
import com.jcondo.mobile.databinding.ActivityLoginBinding;
import com.jcondo.mobile.session.SessionManager;
import com.jcondo.mobile.util.Ui;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Tela de entrada. Se ja existe token guardado, nem chega a ser desenhada:
 * o app abre direto na tela inicial.
 */
public class LoginActivity extends AppCompatActivity {

    /** Marca que o app voltou para ca por causa de um token vencido. */
    public static final String EXTRA_SESSAO_EXPIRADA = "sessao_expirada";

    private ActivityLoginBinding binding;
    private SessionManager sessao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessao = SessionManager.get(this);

        // a trava do ApiClient so e liberada aqui: enquanto a tela de login nao
        // aparece, as outras chamadas que tambem tomaram 401 nao reabrem nada
        ApiClient.loginRetomado();

        if (sessao.estaLogado()) {
            abrirPrincipal();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_SESSAO_EXPIRADA, false)) {
            Ui.aviso(binding.getRoot(), getString(R.string.erro_sessao_expirada));
        }

        binding.entrarBotao.setOnClickListener(v -> entrar());
        binding.esqueciTexto.setOnClickListener(v -> mostrarAjudaSenha());
        binding.configServidorBotao.setOnClickListener(v -> alternarConfigServidor());
        binding.salvarServidorBotao.setOnClickListener(v -> salvarServidor());

        binding.servidorInput.setText(sessao.getApiUrl());

        // a mensagem de erro do campo some assim que o morador comeca a corrigir
        limparErroAoDigitar();
    }

    private void limparErroAoDigitar() {
        binding.emailInput.addTextChangedListener(new LimpadorDeErro() {
            @Override
            void aoMudar() {
                binding.emailLayout.setError(null);
            }
        });
        binding.senhaInput.addTextChangedListener(new LimpadorDeErro() {
            @Override
            void aoMudar() {
                binding.senhaLayout.setError(null);
            }
        });
    }

    private void entrar() {
        Ui.esconderTeclado(this);

        String email = texto(binding.emailInput.getText());
        String senha = texto(binding.senhaInput.getText());

        // validacao no aparelho antes de gastar uma chamada de rede
        boolean valido = true;
        if (email.isEmpty()) {
            binding.emailLayout.setError(getString(R.string.login_erro_email));
            valido = false;
        }
        if (senha.isEmpty()) {
            binding.senhaLayout.setError(getString(R.string.login_erro_senha));
            valido = false;
        }
        if (!valido) {
            return;
        }

        carregando(true);

        ApiClient.get(this).login(new LoginRequest(email, senha))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call,
                                           @NonNull Response<LoginResponse> resposta) {
                        carregando(false);
                        if (resposta.isSuccessful() && resposta.body() != null
                                && resposta.body().token != null) {
                            sessao.salvarSessao(resposta.body().token, resposta.body().morador);
                            abrirPrincipal();
                        } else {
                            Ui.aviso(binding.getRoot(),
                                    RespostaApi.mensagemDeErro(LoginActivity.this, resposta));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call,
                                          @NonNull Throwable erro) {
                        carregando(false);
                        Ui.aviso(binding.getRoot(),
                                RespostaApi.mensagemDeFalha(LoginActivity.this, erro));
                    }
                });
    }

    private void carregando(boolean ligado) {
        binding.progresso.setVisibility(ligado ? View.VISIBLE : View.GONE);
        binding.entrarBotao.setEnabled(!ligado);
        binding.entrarBotao.setText(ligado ? R.string.login_entrando : R.string.login_entrar);
    }

    private void alternarConfigServidor() {
        boolean visivel = binding.servidorGrupo.getVisibility() == View.VISIBLE;
        binding.servidorGrupo.setVisibility(visivel ? View.GONE : View.VISIBLE);
    }

    private void salvarServidor() {
        String url = SessionManager.normalizar(texto(binding.servidorInput.getText()));
        sessao.setApiUrl(url);
        // o cliente Retrofit precisa ser recriado com a nova baseUrl
        ApiClient.reiniciar();
        binding.servidorInput.setText(url);
        binding.servidorGrupo.setVisibility(View.GONE);
        Ui.esconderTeclado(this);
        Ui.aviso(binding.getRoot(), getString(R.string.perfil_servidor, url));
    }

    private void mostrarAjudaSenha() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.login_esqueci)
                .setMessage(R.string.login_recuperacao)
                .setPositiveButton(R.string.acao_entendi, null)
                .show();
    }

    private void abrirPrincipal() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String texto(Editable editable) {
        return editable == null ? "" : editable.toString().trim();
    }

    private abstract static class LimpadorDeErro implements TextWatcher {

        abstract void aoMudar();

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            aoMudar();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
