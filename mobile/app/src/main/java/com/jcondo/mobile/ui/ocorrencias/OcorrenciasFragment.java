package com.jcondo.mobile.ui.ocorrencias;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.ApiClient;
import com.jcondo.mobile.api.RespostaApi;
import com.jcondo.mobile.api.model.Ocorrencia;
import com.jcondo.mobile.databinding.FragmentListaBinding;
import com.jcondo.mobile.ui.MainActivity;
import com.jcondo.mobile.util.EstadoUi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Chamados abertos pelo morador e o andamento de cada um. */
public class OcorrenciasFragment extends Fragment implements MainActivity.Recarregavel {

    private FragmentListaBinding binding;
    private OcorrenciaAdapter adapter;
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

        binding.tituloLista.setText(R.string.ocorrencias_titulo);
        binding.fabAcao.setText(R.string.ocorrencias_nova);
        binding.fabAcao.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NovaOcorrenciaActivity.class)));

        adapter = new OcorrenciaAdapter(this::abrirDetalhe);
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
        if (binding != null && adapter != null) {
            carregar();
        }
    }

    @Override
    public void recarregar() {
        carregar();
    }

    private void carregar() {
        ApiClient.get(requireContext()).minhasOcorrencias()
                .enqueue(new Callback<List<Ocorrencia>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Ocorrencia>> call,
                                           @NonNull Response<List<Ocorrencia>> resposta) {
                        if (binding == null) {
                            return;
                        }
                        binding.swipeLista.setRefreshing(false);

                        if (resposta.isSuccessful() && resposta.body() != null) {
                            List<Ocorrencia> lista = resposta.body();
                            adapter.substituir(lista);
                            if (lista.isEmpty()) {
                                estado.vazio(getString(R.string.ocorrencias_vazio_titulo),
                                        getString(R.string.ocorrencias_vazio_texto));
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
                    public void onFailure(@NonNull Call<List<Ocorrencia>> call,
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

    private void abrirDetalhe(Ocorrencia ocorrencia) {
        Intent intent = new Intent(requireContext(), OcorrenciaDetalheActivity.class);
        intent.putExtra(OcorrenciaDetalheActivity.EXTRA_OCORRENCIA, ocorrencia);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
