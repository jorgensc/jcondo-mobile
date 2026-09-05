package com.jcondo.mobile.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.ApiClient;
import com.jcondo.mobile.databinding.ActivityMainBinding;
import com.jcondo.mobile.session.SessionManager;
import com.jcondo.mobile.ui.avisos.AvisosFragment;
import com.jcondo.mobile.ui.home.HomeFragment;
import com.jcondo.mobile.ui.ocorrencias.OcorrenciasFragment;
import com.jcondo.mobile.ui.perfil.PerfilFragment;
import com.jcondo.mobile.ui.reservas.ReservasFragment;
import com.jcondo.mobile.util.Notificacoes;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Navegacao do aplicativo: cinco fragmentos e a barra inferior.
 * Os fragmentos sao ocultados e reexibidos em vez de recriados, entao voltar
 * para uma aba mantem a posicao da lista.
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_ABA = "aba";
    public static final int ABA_INICIO = 0;
    public static final int ABA_AVISOS = 1;
    public static final int ABA_RESERVAS = 2;
    public static final int ABA_OCORRENCIAS = 3;

    private static final int PEDIDO_NOTIFICACAO = 91;

    private ActivityMainBinding binding;
    private Fragment fragmentoAtual;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.get(this).estaLogado()) {
            voltarParaLogin();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navegacao.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                mostrar("inicio", HomeFragment::new);
            } else if (id == R.id.nav_avisos) {
                mostrar("avisos", AvisosFragment::new);
            } else if (id == R.id.nav_reservas) {
                mostrar("reservas", ReservasFragment::new);
            } else if (id == R.id.nav_ocorrencias) {
                mostrar("ocorrencias", OcorrenciasFragment::new);
            } else if (id == R.id.nav_perfil) {
                mostrar("perfil", PerfilFragment::new);
            }
            return true;
        });

        // tocar de novo na aba ativa rola a lista para o topo
        binding.navegacao.setOnItemReselectedListener(item -> {
            if (fragmentoAtual instanceof Recarregavel) {
                ((Recarregavel) fragmentoAtual).recarregar();
            }
        });

        if (savedInstanceState == null) {
            selecionarAba(getIntent().getIntExtra(EXTRA_ABA, ABA_INICIO));
        }

        Notificacoes.pedirPermissaoSeNecessario(this, PEDIDO_NOTIFICACAO);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // chegou pela notificacao de novo aviso: abre direto na aba de avisos
        selecionarAba(intent.getIntExtra(EXTRA_ABA, ABA_INICIO));
    }

    private void selecionarAba(int aba) {
        int itemId;
        switch (aba) {
            case ABA_AVISOS:
                itemId = R.id.nav_avisos;
                break;
            case ABA_RESERVAS:
                itemId = R.id.nav_reservas;
                break;
            case ABA_OCORRENCIAS:
                itemId = R.id.nav_ocorrencias;
                break;
            default:
                itemId = R.id.nav_inicio;
                break;
        }
        binding.navegacao.setSelectedItemId(itemId);
    }

    /** Atalho usado pelos fragmentos (ex.: botao "Reservar área" da tela inicial). */
    public void irPara(int aba) {
        selecionarAba(aba);
    }

    private void mostrar(String tag, Criador criador) {
        Fragment existente = getSupportFragmentManager().findFragmentByTag(tag);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();

        if (fragmentoAtual != null) {
            tx.hide(fragmentoAtual);
        }
        if (existente == null) {
            existente = criador.criar();
            tx.add(R.id.conteudo, existente, tag);
        } else {
            tx.show(existente);
        }
        tx.commit();
        fragmentoAtual = existente;
    }

    private interface Criador {
        Fragment criar();
    }

    /** Encerra a sessao no servidor e volta para o login. */
    public void sair() {
        SessionManager sessao = SessionManager.get(this);

        // avisa o servidor para invalidar o token; o resultado nao muda o
        // que acontece na tela - a sessao local e apagada de qualquer forma
        ApiClient.get(this).logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });

        sessao.encerrar();
        voltarParaLogin();
    }

    private void voltarParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public interface Recarregavel {
        void recarregar();
    }
}
