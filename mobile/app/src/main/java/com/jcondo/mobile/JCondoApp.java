package com.jcondo.mobile;

import android.app.Application;

import com.jcondo.mobile.util.Notificacoes;

/**
 * Ponto de partida do aplicativo. Só cria o canal de notificação, que
 * precisa existir antes de qualquer notificação ser enviada (Android 8+).
 */
public class JCondoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Notificacoes.criarCanal(this);
    }
}
