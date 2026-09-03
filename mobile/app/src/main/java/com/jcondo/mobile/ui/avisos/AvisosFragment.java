package com.jcondo.mobile.ui.avisos;

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
import com.jcondo.mobile.api.model.Aviso;
import com.jcondo.mobile.databinding.FragmentListaBinding;
import com.jcondo.mobile.ui.MainActivity;
import com.jcondo.mobile.util.EstadoUi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Lista de comunicados publicados pela administracao. */
public class AvisosFragment extends Fragment implements MainActivity.Recarregavel {

    private FragmentListaBinding binding;
    private AvisoAdapter adapter;
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

        binding.tituloLista.setText(R.string.avisos_titulo);
        // avisos sao so leitura para o morador: a tela nao tem acao flutuante
        binding.fabAcao.setVisibility(View.GONE);

        adapter = new AvisoAdapter(this::abrirDetalhe);
        binding.listaRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaRecycler.setAdapter(adapter);

        estado = new EstadoUi(binding.estado, binding.listaRecycler);
        binding.swipeLista.setOnRefreshListener(this::carregar);
        binding.swipeLista.setColorSchemeResources(R.color.jc_azul_800);

        estado.carregando();
        carregar();
    }

    @Override
    public void recarregar() {
        binding.listaRecycler.smoothScrollToPosition(0);
        carregar();
    }

    private void carregar() {
        ApiClient.get(requireContext()).avisos().enqueue(new Callback<List<Aviso>>() {
            @Override
            public void onResponse(@NonNull Call<List<Aviso>> call,
                                   @NonNull Response<List<Aviso>> resposta) {
                if (binding == null) {
                    return;
                }
                binding.swipeLista.setRefreshing(false);

                if (resposta.isSuccessful() && resposta.body() != null) {
                    List<Aviso> avisos = resposta.body();
                    adapter.substituir(avisos);
                    if (avisos.isEmpty()) {
                        estado.vazio(getString(R.string.avisos_vazio_titulo),
                                getString(R.string.avisos_vazio_texto));
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
            public void onFailure(@NonNull Call<List<Aviso>> call, @NonNull Throwable erro) {
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

    private void abrirDetalhe(Aviso aviso) {
        Intent intent = new Intent(requireContext(), AvisoDetalheActivity.class);
        intent.putExtra(AvisoDetalheActivity.EXTRA_AVISO, aviso);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
