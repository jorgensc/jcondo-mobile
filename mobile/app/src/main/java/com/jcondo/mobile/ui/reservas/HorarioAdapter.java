package com.jcondo.mobile.ui.reservas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcondo.mobile.api.model.Horario;
import com.jcondo.mobile.databinding.ItemHorarioBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Grade de horarios da area no dia escolhido. Os ocupados aparecem
 * desabilitados em vez de sumirem, para nao dar a impressao de opcao perdida.
 */
public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.Item> {

    public interface AoSelecionar {
        void horario(Horario horario);
    }

    private final List<Horario> itens = new ArrayList<>();
    private final AoSelecionar aoSelecionar;
    private int posicaoSelecionada = RecyclerView.NO_POSITION;

    public HorarioAdapter(AoSelecionar aoSelecionar) {
        this.aoSelecionar = aoSelecionar;
    }

    public void substituir(List<Horario> novos) {
        itens.clear();
        if (novos != null) {
            itens.addAll(novos);
        }
        posicaoSelecionada = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public void limparSelecao() {
        int anterior = posicaoSelecionada;
        posicaoSelecionada = RecyclerView.NO_POSITION;
        if (anterior != RecyclerView.NO_POSITION) {
            notifyItemChanged(anterior);
        }
    }

    @NonNull
    @Override
    public Item onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Item(ItemHorarioBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Item holder, int position) {
        Horario horario = itens.get(position);
        holder.mostrar(horario, position == posicaoSelecionada);

        holder.b.horarioCaixa.setOnClickListener(v -> {
            if (!horario.disponivel) {
                return;
            }
            int anterior = posicaoSelecionada;
            posicaoSelecionada = holder.getBindingAdapterPosition();
            if (anterior != RecyclerView.NO_POSITION) {
                notifyItemChanged(anterior);
            }
            notifyItemChanged(posicaoSelecionada);
            if (aoSelecionar != null) {
                aoSelecionar.horario(horario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Item extends RecyclerView.ViewHolder {

        final ItemHorarioBinding b;

        Item(ItemHorarioBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void mostrar(Horario horario, boolean selecionado) {
            b.horarioFaixa.setText(horario.faixa());
            b.horarioCaixa.setEnabled(horario.disponivel);
            b.horarioCaixa.setSelected(selecionado);
            b.horarioAviso.setVisibility(horario.disponivel ? View.GONE : View.VISIBLE);

            // leitor de tela anuncia o estado junto com o horario
            b.horarioCaixa.setContentDescription(horario.faixa()
                    + (horario.disponivel ? ", disponível" : ", ocupado"));
        }
    }
}
