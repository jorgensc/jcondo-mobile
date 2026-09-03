package com.jcondo.mobile.util;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.model.Aviso;
import com.jcondo.mobile.session.SessionManager;
import com.jcondo.mobile.ui.MainActivity;

/**
 * Notificacao local de novo aviso.
 *
 * A cada carregamento da tela inicial o app compara o id do aviso mais recente
 * com o ultimo que o morador ja viu (guardado no SharedPreferences) e notifica
 * apenas quando aparece algo novo. E uma notificacao local, disparada pelo
 * proprio aplicativo - o push real via Firebase Cloud Messaging esta descrito
 * no relatorio como evolucao, porque exige um projeto no Firebase e um serviço
 * de envio no backend, fora do escopo desta entrega.
 */
public final class Notificacoes {

    public static final String CANAL_AVISOS = "jcondo_avisos";
    private static final int ID_NOTIFICACAO = 1001;

    private Notificacoes() {
    }

    /** Criado uma vez, no start do aplicativo (obrigatorio a partir do Android 8). */
    public static void criarCanal(Context contexto) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel canal = new NotificationChannel(
                CANAL_AVISOS,
                contexto.getString(R.string.canal_avisos_nome),
                NotificationManager.IMPORTANCE_DEFAULT);
        canal.setDescription(contexto.getString(R.string.canal_avisos_descricao));

        NotificationManager gerente = contexto.getSystemService(NotificationManager.class);
        if (gerente != null) {
            gerente.createNotificationChannel(canal);
        }
    }

    /** A partir do Android 13 a permissao precisa ser pedida em runtime. */
    public static void pedirPermissaoSeNecessario(Activity activity, int codigoPedido) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        boolean concedida = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        if (!concedida) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, codigoPedido);
        }
    }

    /**
     * Notifica se o aviso mais recente for mais novo que o ultimo visto.
     * Nao faz nada quando o morador desligou a preferencia no perfil.
     */
    public static void notificarSeNovo(Context contexto, Aviso maisRecente) {
        if (maisRecente == null || maisRecente.id == null) {
            return;
        }
        SessionManager sessao = SessionManager.get(contexto);
        if (!sessao.isNotificarAvisos()) {
            return;
        }

        long ultimoVisto = sessao.getUltimoAvisoVisto();
        sessao.setUltimoAvisoVisto(maisRecente.id);

        // primeira execucao: so registra o marcador, sem notificar
        if (ultimoVisto == 0L || maisRecente.id <= ultimoVisto) {
            return;
        }
        if (!temPermissao(contexto)) {
            return;
        }

        Intent abrir = new Intent(contexto, MainActivity.class);
        abrir.putExtra(MainActivity.EXTRA_ABA, MainActivity.ABA_AVISOS);
        abrir.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent toque = PendingIntent.getActivity(contexto, 0, abrir,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificacao =
                new NotificationCompat.Builder(contexto, CANAL_AVISOS)
                        .setSmallIcon(R.drawable.ic_avisos)
                        .setContentTitle(contexto.getString(R.string.notificacao_novo_aviso))
                        .setContentText(maisRecente.titulo)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(maisRecente.titulo))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(toque);

        try {
            NotificationManagerCompat.from(contexto)
                    .notify(ID_NOTIFICACAO, notificacao.build());
        } catch (SecurityException e) {
            // permissao revogada entre a checagem e o envio: nada a fazer
        }
    }

    private static boolean temPermissao(Context contexto) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationManagerCompat.from(contexto).areNotificationsEnabled();
        }
        return ContextCompat.checkSelfPermission(contexto,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }
}
