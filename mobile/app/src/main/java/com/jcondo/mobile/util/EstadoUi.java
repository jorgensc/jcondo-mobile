package com.jcondo.mobile.util;

import android.view.View;

import com.jcondo.mobile.R;
import com.jcondo.mobile.databinding.ViewEstadoBinding;

/**
 * Controla o bloco view_estado, que cobre os tres momentos em que uma lista
 * nao tem conteudo para mostrar: carregando, vazia e com erro.
 *
 * Centralizar isso resolve um problema classico de UX em aplicativo que
 * consome API: a tela em branco. O morador sempre ve ou um indicador de
 * progresso, ou uma explicacao do porque a lista esta vazia, ou o motivo da
 * falha com um botao para tentar de novo.
 */
public class EstadoUi {

    private final ViewEstadoBinding binding;
    private final View conteudo;

    public EstadoUi(ViewEstadoBinding binding, View conteudo) {
        this.binding = binding;
        this.conteudo = conteudo;
    }

    /** Primeira carga: indicador central, sem lista por baixo. */
    public void carregando() {
        binding.getRoot().setVisibility(View.VISIBLE);
        binding.estadoProgresso.setVisibility(View.VISIBLE);
        binding.estadoIcone.setVisibility(View.GONE);
        binding.estadoTitulo.setVisibility(View.GONE);
        binding.estadoTexto.setVisibility(View.GONE);
        binding.estadoBotao.setVisibility(View.GONE);
        Ui.mostrar(conteudo, false);
    }

    /** Deu certo e veio conteudo: some com o bloco e mostra a lista. */
    public void conteudo() {
        binding.getRoot().setVisibility(View.GONE);
        Ui.mostrar(conteudo, true);
    }

    /** Requisicao bem-sucedida, porem sem registros. */
    public void vazio(String titulo, String texto) {
        binding.getRoot().setVisibility(View.VISIBLE);
        binding.estadoProgresso.setVisibility(View.GONE);
        binding.estadoIcone.setVisibility(View.VISIBLE);
        binding.estadoIcone.setImageResource(R.drawable.ic_caixa_vazia);
        binding.estadoTitulo.setVisibility(View.VISIBLE);
        binding.estadoTitulo.setText(titulo);
        binding.estadoTexto.setVisibility(View.VISIBLE);
        binding.estadoTexto.setText(texto);
        binding.estadoBotao.setVisibility(View.GONE);
        Ui.mostrar(conteudo, false);
    }

    /** Falhou: mostra o motivo e oferece o caminho de volta. */
    public void erro(String mensagem, View.OnClickListener aoTentarNovamente) {
        binding.getRoot().setVisibility(View.VISIBLE);
        binding.estadoProgresso.setVisibility(View.GONE);
        binding.estadoIcone.setVisibility(View.VISIBLE);
        binding.estadoIcone.setImageResource(R.drawable.ic_sem_conexao);
        binding.estadoTitulo.setVisibility(View.VISIBLE);
        binding.estadoTitulo.setText(R.string.erro_generico);
        binding.estadoTexto.setVisibility(View.VISIBLE);
        binding.estadoTexto.setText(mensagem);
        binding.estadoBotao.setVisibility(View.VISIBLE);
        binding.estadoBotao.setOnClickListener(aoTentarNovamente);
        Ui.mostrar(conteudo, false);
    }
}
