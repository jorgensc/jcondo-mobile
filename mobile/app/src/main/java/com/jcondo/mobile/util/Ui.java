package com.jcondo.mobile.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.jcondo.mobile.R;

/** Pequenos atalhos de interface repetidos em varias telas. */
public final class Ui {

    private Ui() {
    }

    /** Aviso curto e nao bloqueante, ancorado no proprio conteudo da tela. */
    public static void aviso(View ancora, String mensagem) {
        if (ancora == null || mensagem == null) {
            return;
        }
        Snackbar.make(ancora, mensagem, Snackbar.LENGTH_LONG).show();
    }

    public static void avisoComAcao(View ancora, String mensagem,
                                    String acao, View.OnClickListener aoClicar) {
        if (ancora == null || mensagem == null) {
            return;
        }
        Snackbar.make(ancora, mensagem, Snackbar.LENGTH_INDEFINITE)
                .setAction(acao, aoClicar)
                .show();
    }

    /** Aplica a etiqueta colorida de status/prioridade. */
    public static void etiqueta(TextView alvo, String texto, int fundoRes, int corTextoRes) {
        Context ctx = alvo.getContext();
        alvo.setText(texto);
        alvo.setBackgroundResource(fundoRes);
        alvo.setTextColor(ContextCompat.getColor(ctx, corTextoRes));
    }

    public static void mostrar(View v, boolean visivel) {
        if (v != null) {
            v.setVisibility(visivel ? View.VISIBLE : View.GONE);
        }
    }

    public static void esconderTeclado(Activity activity) {
        if (activity == null) {
            return;
        }
        View foco = activity.getCurrentFocus();
        if (foco == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(foco.getWindowToken(), 0);
        }
    }

    /** Cor de fundo e de texto da etiqueta de prioridade de um aviso. */
    public static int[] estiloPrioridade(String prioridade) {
        if ("URGENTE".equalsIgnoreCase(prioridade)) {
            return new int[]{R.drawable.bg_etiqueta_vermelho, R.color.jc_erro};
        }
        if ("ALTA".equalsIgnoreCase(prioridade)) {
            return new int[]{R.drawable.bg_etiqueta_amarelo, R.color.jc_atencao};
        }
        return new int[]{R.drawable.bg_etiqueta_azul, R.color.jc_azul_600};
    }

    public static String rotuloPrioridade(Context ctx, String prioridade) {
        if ("URGENTE".equalsIgnoreCase(prioridade)) {
            return ctx.getString(R.string.prioridade_urgente);
        }
        if ("ALTA".equalsIgnoreCase(prioridade)) {
            return ctx.getString(R.string.prioridade_alta);
        }
        return ctx.getString(R.string.prioridade_normal);
    }

    /** Cor de fundo e de texto da etiqueta de status de uma ocorrencia. */
    public static int[] estiloStatusOcorrencia(String status) {
        if ("RESOLVIDA".equalsIgnoreCase(status)) {
            return new int[]{R.drawable.bg_etiqueta_verde, R.color.jc_sucesso};
        }
        if ("EM_ANDAMENTO".equalsIgnoreCase(status)) {
            return new int[]{R.drawable.bg_etiqueta_amarelo, R.color.jc_atencao};
        }
        return new int[]{R.drawable.bg_etiqueta_azul, R.color.jc_azul_600};
    }
}
