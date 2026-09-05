package com.jcondo.mobile.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.ApiClient;
import com.jcondo.mobile.api.RespostaApi;
import com.jcondo.mobile.api.model.Aviso;
import com.jcondo.mobile.api.model.Morador;
import com.jcondo.mobile.api.model.Reserva;
import com.jcondo.mobile.api.model.ResumoHome;
import com.jcondo.mobile.databinding.FragmentHomeBinding;
import com.jcondo.mobile.databinding.ItemAvisoResumoBinding;
import com.jcondo.mobile.session.SessionManager;
import com.jcondo.mobile.ui.MainActivity;
import com.jcondo.mobile.ui.avisos.AvisoDetalheActivity;
import com.jcondo.mobile.ui.ocorrencias.NovaOcorrenciaActivity;
import com.jcondo.mobile.ui.reservas.NovaReservaActivity;
import com.jcondo.mobile.util.Datas;
import com.jcondo.mobile.util.EstadoUi;
import com.jcondo.mobile.util.Notificacoes;
import com.jcondo.mobile.util.Ui;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Tela inicial. Consome um endpoint unico (/api/home/resumo) em vez de quatro:
 * a tela carrega de uma vez e gasta menos rede.
 */
public class HomeFragment extends Fragment implements MainActivity.Recarregavel {

    private FragmentHomeBinding binding;
    private EstadoUi estado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        estado = new EstadoUi(binding.estado, binding.conteudoHome);

        binding.swipeRefresh.setOnRefreshListener(this::carregar);
        binding.swipeRefresh.setColorSchemeResources(R.color.jc_azul_800);

        binding.verTodosAvisos.setOnClickListener(v -> abrirAba(MainActivity.ABA_AVISOS));
        binding.atalhoReservar.setOnClickListener(v -> abrirNovaReserva());
        binding.reservarBotao.setOnClickListener(v -> abrirNovaReserva());
        binding.atalhoOcorrencia.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NovaOcorrenciaActivity.class)));

        // mostra logo o que ja se sabe do morador, antes da resposta chegar
        Morador guardado = SessionManager.get(requireContext()).getMorador();
        if (guardado != null) {
            binding.nomeTexto.setText(guardado.primeiroNome());
            binding.unidadeTexto.setText(guardado.unidade);
        }

        estado.carregando();
        carregar();
    }

    @Override
    public void onResume() {
        super.onResume();
        // volta de uma nova reserva ou ocorrencia: os numeros mudaram
        if (binding != null && binding.conteudoHome.getVisibility() == View.VISIBLE) {
            carregar();
        }
    }

    @Override
    public void recarregar() {
        carregar();
    }

    private void carregar() {
        ApiClient.get(requireContext()).resumoHome().enqueue(new Callback<ResumoHome>() {
            @Override
            public void onResponse(@NonNull Call<ResumoHome> call,
                                   @NonNull Response<ResumoHome> resposta) {
                if (binding == null) {
                    return;
                }
                binding.swipeRefresh.setRefreshing(false);

                if (resposta.isSuccessful() && resposta.body() != null) {
                    preencher(resposta.body());
                    estado.conteudo();
                } else {
                    estado.erro(RespostaApi.mensagemDeErro(requireContext(), resposta),
                            v -> {
                                estado.carregando();
                                carregar();
                            });
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResumoHome> call, @NonNull Throwable erro) {
                if (binding == null) {
                    return;
                }
                binding.swipeRefresh.setRefreshing(false);
                estado.erro(RespostaApi.mensagemDeFalha(requireContext(), erro),
                        v -> {
                            estado.carregando();
                            carregar();
                        });
            }
        });
    }

    private void preencher(ResumoHome resumo) {
        if (resumo.morador != null) {
            binding.saudacaoTexto.setText(resumo.saudacao == null ? "" : resumo.saudacao + ",");
            binding.nomeTexto.setText(resumo.morador.primeiroNome());
            binding.unidadeTexto.setText(resumo.morador.unidade);
            SessionManager.get(requireContext()).atualizarMorador(resumo.morador);
        }

        binding.cardAvisosNumero.setText(String.valueOf(resumo.totalAvisos));
        binding.cardReservasNumero.setText(String.valueOf(resumo.reservasAtivas));
        binding.cardOcorrenciasNumero.setText(String.valueOf(resumo.ocorrenciasAbertas));

        preencherProximaReserva(resumo.proximaReserva);
        preencherAvisos(resumo.avisosRecentes);
    }

    private void preencherProximaReserva(Reserva reserva) {
        boolean tem = reserva != null;
        Ui.mostrar(binding.proximaReservaCartao, tem);
        Ui.mostrar(binding.semReservaCartao, !tem);

        if (tem) {
            binding.proximaReservaArea.setText(reserva.areaNome);
            binding.proximaReservaData.setText(Datas.relativa(reserva.data));
            binding.proximaReservaHora.setText(reserva.horaInicio);
        }
    }

    /**
     * Os avisos recentes sao inflados direto num LinearLayout em vez de um
     * RecyclerView: sao no maximo tres itens e aninhar listas roláveis dentro
     * de um scroll piora a rolagem da tela.
     */
    private void preencherAvisos(List<Aviso> avisos) {
        binding.avisosContainer.removeAllViews();
        if (avisos == null || avisos.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Aviso aviso : avisos) {
            ItemAvisoResumoBinding item = ItemAvisoResumoBinding.inflate(
                    inflater, binding.avisosContainer, false);
            item.resumoTitulo.setText(aviso.titulo);
            item.resumoData.setText(aviso.dataPublicacao);
            item.resumoCartao.setOnClickListener(v -> abrirAviso(aviso));
            binding.avisosContainer.addView(item.getRoot());
        }

        // notifica se o aviso mais recente ainda nao tinha sido visto
        Notificacoes.notificarSeNovo(requireContext(), avisos.get(0));
    }

    private void abrirAviso(Aviso aviso) {
        Intent intent = new Intent(requireContext(), AvisoDetalheActivity.class);
        intent.putExtra(AvisoDetalheActivity.EXTRA_AVISO, aviso);
        startActivity(intent);
    }

    private void abrirNovaReserva() {
        startActivity(new Intent(requireContext(), NovaReservaActivity.class));
    }

    private void abrirAba(int aba) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).irPara(aba);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // evita segurar as views depois que o fragmento sai da tela
        binding = null;
    }
}
