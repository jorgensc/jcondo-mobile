package com.jcondo.mobile.ui.reservas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.ApiClient;
import com.jcondo.mobile.api.RespostaApi;
import com.jcondo.mobile.api.model.Reserva;
import com.jcondo.mobile.databinding.FragmentListaBinding;
import com.jcondo.mobile.ui.MainActivity;
import com.jcondo.mobile.util.EstadoUi;
import com.jcondo.mobile.util.Ui;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Reservas do morador, com cancelamento confirmado por diálogo. */
public class ReservasFragment extends Fragment implements MainActivity.Recarregavel {

    private FragmentListaBinding binding;
    private ReservaAdapter adapter;
    private EstadoUi estado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tituloLista.setText(R.string.reservas_titulo);
        binding.fabAcao.setText(R.string.reservas_nova);
        binding.fabAcao.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NovaReservaActivity.class)));

        adapter = new ReservaAdapter(this::confirmarCancelamento);
        binding.listaRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaRecycler.setAdapter(adapter);

        estado = new EstadoUi(binding.estado, binding.listaRecycler);
        binding.swipeLista.setOnRefreshListener(this::carregar);
        binding.swipeLista.setColorSchemeResources(R.color.jc_azul_800);

        estado.carregando();
        carregar();
    }

    @Override
    public void onResume() {
        super.onResume();
        // ao voltar da tela de nova reserva a lista precisa refletir o novo item
        if (binding != null && adapter != null) {
            carregar();
        }
    }

    @Override
    public void recarregar() {
        carregar();
    }

    private void carregar() {
        ApiClient.get(requireContext()).minhasReservas()
                .enqueue(new Callback<List<Reserva>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Reserva>> call,
                                           @NonNull Response<List<Reserva>> resposta) {
                        if (binding == null) {
                            return;
                        }
                        binding.swipeLista.setRefreshing(false);

                        if (resposta.isSuccessful() && resposta.body() != null) {
                            List<Reserva> reservas = resposta.body();
                            adapter.substituir(reservas);
                            if (reservas.isEmpty()) {
                                estado.vazio(getString(R.string.reservas_vazio_titulo),
                                        getString(R.string.reservas_vazio_texto));
                            } else {
                                estado.conteudo();
                            }
                        } else {
                            estado.erro(RespostaApi.mensagemDeErro(requireContext(), resposta),
                                    v -> {
                                        estado.carregando();
                                        carregar();
                                    });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Reserva>> call,
                                          @NonNull Throwable erro) {
                        if (binding == null) {
                            return;
                        }
                        binding.swipeLista.setRefreshing(false);
                        estado.erro(RespostaApi.mensagemDeFalha(requireContext(), erro),
                                v -> {
                                    estado.carregando();
                                    carregar();
                                });
                    }
                });
    }

    /**
     * Cancelar e uma acao destrutiva, entao passa por confirmacao explicita.
     * O texto do dialogo diz o efeito real ("o horario volta a ficar
     * disponivel") em vez de um "tem certeza?" generico.
     */
    private void confirmarCancelamento(Reserva reserva) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reservas_cancelar_pergunta)
                .setMessage(getString(R.string.reservas_cancelar_detalhe))
                .setPositiveButton(R.string.reservas_cancelar_confirmar,
                        (d, w) -> cancelar(reserva))
                .setNegativeButton(R.string.reservas_cancelar_voltar, null)
                .show();
    }

    private void cancelar(Reserva reserva) {
        if (reserva.id == null) {
            return;
        }
        ApiClient.get(requireContext()).cancelarReserva(reserva.id)
                .enqueue(new Callback<Reserva>() {
                    @Override
                    public void onResponse(@NonNull Call<Reserva> call,
                                           @NonNull Response<Reserva> resposta) {
                        if (binding == null) {
                            return;
                        }
                        if (resposta.isSuccessful()) {
                            Ui.aviso(binding.getRoot(),
                                    getString(R.string.reservas_cancelada_ok));
                            carregar();
                        } else {
                            Ui.aviso(binding.getRoot(),
                                    RespostaApi.mensagemDeErro(requireContext(), resposta));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Reserva> call, @NonNull Throwable erro) {
                        if (binding == null) {
                            return;
                        }
                        Ui.aviso(binding.getRoot(),
                                RespostaApi.mensagemDeFalha(requireContext(), erro));
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
