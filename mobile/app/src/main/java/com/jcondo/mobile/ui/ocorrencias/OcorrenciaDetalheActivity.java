package com.jcondo.mobile.ui.ocorrencias;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.model.Ocorrencia;
import com.jcondo.mobile.databinding.ActivityOcorrenciaDetalheBinding;
import com.jcondo.mobile.util.Ui;

/** Acompanhamento de uma ocorrencia: status, descricao e resposta da administracao. */
public class OcorrenciaDetalheActivity extends AppCompatActivity {

    public static final String EXTRA_OCORRENCIA = "ocorrencia";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityOcorrenciaDetalheBinding binding =
                ActivityOcorrenciaDetalheBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.barra.setTitle(getString(R.string.ocorrencias_titulo));
        binding.barra.setNavigationOnClickListener(v -> finish());

        Ocorrencia o = (Ocorrencia) getIntent().getSerializableExtra(EXTRA_OCORRENCIA);
        if (o == null) {
            finish();
            return;
        }

        binding.ocTitulo.setText(o.titulo);
        binding.ocDescricao.setText(o.descricao);
        binding.ocCategoria.setText(o.categoria);
        binding.ocProtocolo.setText(getString(R.string.ocorrencia_protocolo, o.protocolo));

        int[] estilo = Ui.estiloStatusOcorrencia(o.status);
        Ui.etiqueta(binding.ocStatus,
                o.statusLabel == null ? o.status : o.statusLabel, estilo[0], estilo[1]);

        StringBuilder datas = new StringBuilder(
                getString(R.string.ocorrencia_aberta_em, o.dataAbertura));
        if (o.dataAtualizacao != null && !o.dataAtualizacao.isEmpty()) {
            datas.append("\n").append(
                    getString(R.string.ocorrencia_atualizada_em, o.dataAtualizacao));
        }
        binding.ocDatas.setText(datas.toString());

        boolean temResposta = o.resposta != null && !o.resposta.trim().isEmpty();
        binding.ocRespostaCartao.setVisibility(temResposta ? View.VISIBLE : View.GONE);
        if (temResposta) {
            binding.ocResposta.setText(o.resposta);
        }
    }
}
