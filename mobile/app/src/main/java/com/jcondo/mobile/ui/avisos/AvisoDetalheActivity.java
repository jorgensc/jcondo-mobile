package com.jcondo.mobile.ui.avisos;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jcondo.mobile.api.model.Aviso;
import com.jcondo.mobile.databinding.ActivityAvisoDetalheBinding;
import com.jcondo.mobile.util.Ui;

/** Leitura do aviso completo. Recebe o objeto ja carregado, sem nova chamada. */
public class AvisoDetalheActivity extends AppCompatActivity {

    public static final String EXTRA_AVISO = "aviso";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAvisoDetalheBinding binding =
                ActivityAvisoDetalheBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.barra.setNavigationOnClickListener(v -> finish());

        Aviso aviso = (Aviso) getIntent().getSerializableExtra(EXTRA_AVISO);
        if (aviso == null) {
            finish();
            return;
        }

        binding.detTitulo.setText(aviso.titulo);
        binding.detConteudo.setText(aviso.conteudo);
        binding.detData.setText(aviso.dataPublicacao);
        binding.detAutor.setText(aviso.autor == null ? "" : "Publicado por " + aviso.autor);

        int[] estilo = Ui.estiloPrioridade(aviso.prioridade);
        Ui.etiqueta(binding.detPrioridade,
                Ui.rotuloPrioridade(this, aviso.prioridade), estilo[0], estilo[1]);
    }
}
