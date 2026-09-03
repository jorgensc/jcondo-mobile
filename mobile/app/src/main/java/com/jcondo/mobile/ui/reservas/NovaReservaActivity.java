package com.jcondo.mobile.ui.reservas;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.jcondo.mobile.R;
import com.jcondo.mobile.api.ApiClient;
import com.jcondo.mobile.api.RespostaApi;
import com.jcondo.mobile.api.model.AreaComum;
import com.jcondo.mobile.api.model.Horario;
import com.jcondo.mobile.api.model.Reserva;
import com.jcondo.mobile.api.model.ReservaRequest;
import com.jcondo.mobile.databinding.ActivityNovaReservaBinding;
import com.jcondo.mobile.util.Datas;
import com.jcondo.mobile.util.Ui;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Nova reserva em tres passos: area, data e horario.
 *
 * A tela consulta a agenda do dia (/api/areas/{id}/horarios) e desabilita as
 * faixas ocupadas, mas a decisao final continua sendo do servidor: se dois
 * moradores confirmarem o mesmo horario ao mesmo tempo, a API devolve 409 e
 * o segundo recebe a mensagem explicando o motivo, com a agenda ja atualizada.
 */
public class NovaReservaActivity extends AppCompatActivity {

    private ActivityNovaReservaBinding binding;
    private HorarioAdapter horarioAdapter;

    private final List<AreaComum> areas = new ArrayList<>();
    private AreaComum areaEscolhida;
    private LocalDate dataEscolhida;
    private Horario horarioEscolhido;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNovaReservaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.barra.setNavigationOnClickListener(v -> finish());

        horarioAdapter = new HorarioAdapter(this::selecionarHorario);
        binding.horariosRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        binding.horariosRecycler.setAdapter(horarioAdapter);
        binding.horariosRecycler.setVisibility(View.GONE);

        binding.areaInput.setOnItemClickListener((parent, view, posicao, id) ->
                selecionarArea(areas.get(posicao)));
        binding.dataBotao.setOnClickListener(v -> escolherData());
        binding.confirmarBotao.setOnClickListener(v -> confirmar());

        carregarAreas();
    }

    // ----- passo 1: areas ---------------------------------------------------

    private void carregarAreas() {
        ApiClient.get(this).areas().enqueue(new Callback<List<AreaComum>>() {
            @Override
            public void onResponse(@NonNull Call<List<AreaComum>> call,
                                   @NonNull Response<List<AreaComum>> resposta) {
                if (resposta.isSuccessful() && resposta.body() != null) {
                    areas.clear();
                    areas.addAll(resposta.body());
                    binding.areaInput.setAdapter(new ArrayAdapter<>(
                            NovaReservaActivity.this,
                            android.R.layout.simple_list_item_1,
                            areas));
                } else {
                    Ui.aviso(binding.getRoot(),
                            RespostaApi.mensagemDeErro(NovaReservaActivity.this, resposta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AreaComum>> call, @NonNull Throwable erro) {
                Ui.aviso(binding.getRoot(),
                        RespostaApi.mensagemDeFalha(NovaReservaActivity.this, erro));
            }
        });
    }

    private void selecionarArea(AreaComum area) {
        areaEscolhida = area;
        binding.areaInfo.setVisibility(View.VISIBLE);
        binding.areaInfo.setText(String.format("%s · funciona das %s às %s",
                area.descricao == null ? "" : area.descricao,
                area.horarioAbertura, area.horarioFechamento));
        // trocar de area invalida o horario que estava escolhido
        horarioEscolhido = null;
        horarioAdapter.limparSelecao();
        atualizarBotao();
        carregarHorarios();
    }

    // ----- passo 2: data ----------------------------------------------------

    private void escolherData() {
        // o calendario nao deixa escolher data passada; a mesma regra existe
        // no servidor, aqui e so para o morador nao perder tempo
        CalendarConstraints limites = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        MaterialDatePicker<Long> seletor = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.nova_reserva_data)
                .setCalendarConstraints(limites)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        seletor.addOnPositiveButtonClickListener(millis -> {
            dataEscolhida = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.of("UTC")).toLocalDate();
            binding.dataBotao.setText(Datas.porExtenso(dataEscolhida));
            horarioEscolhido = null;
            horarioAdapter.limparSelecao();
            atualizarBotao();
            carregarHorarios();
        });

        seletor.show(getSupportFragmentManager(), "data");
    }

    // ----- passo 3: horarios ------------------------------------------------

    private void carregarHorarios() {
        if (areaEscolhida == null || dataEscolhida == null) {
            binding.horariosAviso.setVisibility(View.VISIBLE);
            binding.horariosAviso.setText(areaEscolhida == null
                    ? R.string.nova_reserva_selecione_area
                    : R.string.nova_reserva_selecione_data);
            binding.horariosRecycler.setVisibility(View.GONE);
            return;
        }

        binding.horariosProgresso.setVisibility(View.VISIBLE);
        binding.horariosAviso.setVisibility(View.GONE);
        binding.horariosRecycler.setVisibility(View.GONE);

        ApiClient.get(this)
                .horarios(areaEscolhida.id, Datas.paraIso(dataEscolhida))
                .enqueue(new Callback<List<Horario>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Horario>> call,
                                           @NonNull Response<List<Horario>> resposta) {
                        binding.horariosProgresso.setVisibility(View.GONE);

                        if (!resposta.isSuccessful() || resposta.body() == null) {
                            binding.horariosAviso.setVisibility(View.VISIBLE);
                            binding.horariosAviso.setText(
                                    RespostaApi.mensagemDeErro(NovaReservaActivity.this, resposta));
                            return;
                        }

                        List<Horario> horarios = resposta.body();
                        horarioAdapter.substituir(horarios);

                        boolean temLivre = false;
                        for (Horario h : horarios) {
                            if (h.disponivel) {
                                temLivre = true;
                                break;
                            }
                        }

                        binding.horariosRecycler.setVisibility(View.VISIBLE);
                        binding.horariosAviso.setVisibility(temLivre ? View.GONE : View.VISIBLE);
                        if (!temLivre) {
                            binding.horariosAviso.setText(R.string.nova_reserva_sem_horario);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Horario>> call,
                                          @NonNull Throwable erro) {
                        binding.horariosProgresso.setVisibility(View.GONE);
                        binding.horariosAviso.setVisibility(View.VISIBLE);
                        binding.horariosAviso.setText(
                                RespostaApi.mensagemDeFalha(NovaReservaActivity.this, erro));
                    }
                });
    }

    private void selecionarHorario(Horario horario) {
        horarioEscolhido = horario;
        atualizarBotao();
    }

    /** O botao so habilita quando os tres passos estao completos. */
    private void atualizarBotao() {
        binding.confirmarBotao.setEnabled(
                areaEscolhida != null && dataEscolhida != null && horarioEscolhido != null);
    }

    // ----- confirmacao ------------------------------------------------------

    private void confirmar() {
        if (areaEscolhida == null || dataEscolhida == null || horarioEscolhido == null) {
            Ui.aviso(binding.getRoot(), getString(R.string.nova_reserva_selecione_horario));
            return;
        }

        enviando(true);

        ReservaRequest pedido = new ReservaRequest(
                areaEscolhida.id,
                Datas.paraIso(dataEscolhida),
                horarioEscolhido.horaInicio,
                horarioEscolhido.horaFim);

        ApiClient.get(this).criarReserva(pedido).enqueue(new Callback<Reserva>() {
            @Override
            public void onResponse(@NonNull Call<Reserva> call,
                                   @NonNull Response<Reserva> resposta) {
                enviando(false);

                if (resposta.isSuccessful() && resposta.body() != null) {
                    mostrarSucesso(resposta.body());
                    return;
                }

                String mensagem = RespostaApi.mensagemDeErro(NovaReservaActivity.this, resposta);
                Ui.aviso(binding.getRoot(), mensagem);

                // 409: outro morador pegou o horario nesse meio tempo.
                // Recarregar a agenda deixa a tela coerente com o servidor.
                if (resposta.code() == 409) {
                    horarioEscolhido = null;
                    atualizarBotao();
                    carregarHorarios();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Reserva> call, @NonNull Throwable erro) {
                enviando(false);
                Ui.aviso(binding.getRoot(),
                        RespostaApi.mensagemDeFalha(NovaReservaActivity.this, erro));
            }
        });
    }

    private void enviando(boolean ligado) {
        binding.confirmarProgresso.setVisibility(ligado ? View.VISIBLE : View.GONE);
        binding.confirmarBotao.setEnabled(!ligado);
    }

    /** Confirmacao explicita: o morador ve o que ficou agendado antes de sair. */
    private void mostrarSucesso(Reserva reserva) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.nova_reserva_ok)
                .setMessage(String.format("%s%n%s, das %s às %s",
                        reserva.areaNome,
                        Datas.relativa(reserva.data),
                        reserva.horaInicio,
                        reserva.horaFim))
                .setPositiveButton(R.string.acao_entendi, (d, w) -> finish())
                .setCancelable(false)
                .show();
    }
}
