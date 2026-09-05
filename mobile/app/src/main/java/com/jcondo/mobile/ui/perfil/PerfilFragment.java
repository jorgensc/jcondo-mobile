package com.jcondo.mobile.ui.perfil;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.jcondo.mobile.BuildConfig;
import com.jcondo.mobile.R;
import com.jcondo.mobile.api.ApiClient;
import com.jcondo.mobile.api.RespostaApi;
import com.jcondo.mobile.api.model.AtualizacaoPerfil;
import com.jcondo.mobile.api.model.Morador;
import com.jcondo.mobile.databinding.FragmentPerfilBinding;
import com.jcondo.mobile.session.SessionManager;
import com.jcondo.mobile.ui.MainActivity;
import com.jcondo.mobile.util.Ui;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Perfil do morador. Bloco, apartamento e CPF ficam fora da edicao de
 * proposito: sao dados cadastrais, e o PUT /api/perfil tambem os ignora.
 */
public class PerfilFragment extends Fragment implements MainActivity.Recarregavel {

    private FragmentPerfilBinding binding;
    private SessionManager sessao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessao = SessionManager.get(requireContext());

        binding.perfilSalvarBotao.setOnClickListener(v -> salvar());
        binding.sairBotao.setOnClickListener(v -> confirmarSaida());

        binding.notificacoesSwitch.setChecked(sessao.isNotificarAvisos());
        binding.notificacoesSwitch.setOnCheckedChangeListener(
                (botao, marcado) -> sessao.setNotificarAvisos(marcado));

        binding.versaoTexto.setText(getString(R.string.perfil_sobre, BuildConfig.VERSION_NAME));
        binding.servidorTexto.setText(getString(R.string.perfil_servidor, sessao.getApiUrl()));

        mostrar(sessao.getMorador());
        carregar();
    }

    @Override
    public void recarregar() {
        carregar();
    }

    private void carregar() {
        ApiClient.get(requireContext()).perfil().enqueue(new Callback<Morador>() {
            @Override
            public void onResponse(@NonNull Call<Morador> call,
                                   @NonNull Response<Morador> resposta) {
                if (binding == null) {
                    return;
                }
                if (resposta.isSuccessful() && resposta.body() != null) {
                    sessao.atualizarMorador(resposta.body());
                    mostrar(resposta.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Morador> call, @NonNull Throwable erro) {
                // a tela ja mostra os dados guardados na sessao; nao adianta
                // esvaziar o perfil so porque a rede oscilou
            }
        });
    }

    private void mostrar(Morador morador) {
        if (morador == null || binding == null) {
            return;
        }
        binding.avatarTexto.setText(morador.iniciais());
        binding.perfilNome.setText(morador.nome);
        binding.perfilUnidade.setText(morador.unidade);
        binding.perfilEmailInput.setText(morador.email);
        binding.perfilTelefoneInput.setText(morador.telefone);
    }

    private void salvar() {
        Ui.esconderTeclado(requireActivity());

        String email = texto(binding.perfilEmailInput.getText());
        String telefone = texto(binding.perfilTelefoneInput.getText());

        binding.perfilEmailLayout.setError(null);
        if (email.isEmpty() || !email.contains("@")) {
            binding.perfilEmailLayout.setError(getString(R.string.login_erro_email));
            return;
        }

        salvando(true);

        ApiClient.get(requireContext())
                .atualizarPerfil(new AtualizacaoPerfil(telefone, email))
                .enqueue(new Callback<Morador>() {
                    @Override
                    public void onResponse(@NonNull Call<Morador> call,
                                           @NonNull Response<Morador> resposta) {
                        if (binding == null) {
                            return;
                        }
                        salvando(false);
                        if (resposta.isSuccessful() && resposta.body() != null) {
                            sessao.atualizarMorador(resposta.body());
                            mostrar(resposta.body());
                            Ui.aviso(binding.getRoot(), getString(R.string.perfil_salvo));
                        } else {
                            Ui.aviso(binding.getRoot(),
                                    RespostaApi.mensagemDeErro(requireContext(), resposta));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Morador> call, @NonNull Throwable erro) {
                        if (binding == null) {
                            return;
                        }
                        salvando(false);
                        Ui.aviso(binding.getRoot(),
                                RespostaApi.mensagemDeFalha(requireContext(), erro));
                    }
                });
    }

    private void salvando(boolean ligado) {
        binding.perfilProgresso.setVisibility(ligado ? View.VISIBLE : View.GONE);
        binding.perfilSalvarBotao.setEnabled(!ligado);
    }

    private void confirmarSaida() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.perfil_sair_pergunta)
                .setMessage(R.string.perfil_sair_detalhe)
                .setPositiveButton(R.string.perfil_sair, (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).sair();
                    }
                })
                .setNegativeButton(R.string.acao_cancelar, null)
                .show();
    }

    private static String texto(Editable editable) {
        return editable == null ? "" : editable.toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
