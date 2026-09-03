package com.jcondo.mobile.ui.reservas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.model.Reserva;
import com.jcondo.mobile.databinding.ItemReservaBinding;
import com.jcondo.mobile.util.Datas;
import com.jcondo.mobile.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.Item> {

    public interface AoCancelar {
        void cancelar(Reserva reserva);
    }

    private final List<Reserva> itens = new ArrayList<>();
    private final AoCancelar aoCancelar;

    public ReservaAdapter(AoCancelar aoCancelar) {
        this.aoCancelar = aoCancelar;
    }

    public void substituir(List<Reserva> novos) {
        itens.clear();
        if (novos != null) {
            itens.addAll(novos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Item onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Item(ItemReservaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Item holder, int position) {
        holder.mostrar(itens.get(position), aoCancelar);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Item extends RecyclerView.ViewHolder {

        private final ItemReservaBinding b;

        Item(ItemReservaBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void mostrar(Reserva reserva, AoCancelar aoCancelar) {
            b.reservaArea.setText(reserva.areaNome);
            b.reservaData.setText(Datas.relativa(reserva.data));
            b.reservaHorario.setText(reserva.faixaHorario());

            boolean passou = Datas.jaPassou(reserva.data, reserva.horaFim);

            if (reserva.isCancelada()) {
                Ui.etiqueta(b.reservaStatus,
                        b.getRoot().getContext().getString(R.string.status_cancelada),
                        R.drawable.bg_etiqueta_vermelho, R.color.jc_erro);
            } else if (passou) {
                Ui.etiqueta(b.reservaStatus,
                        b.getRoot().getContext().getString(R.string.status_realizada),
                        R.drawable.bg_etiqueta_neutro, R.color.jc_texto_secundario);
            } else {
                Ui.etiqueta(b.reservaStatus,
                        b.getRoot().getContext().getString(R.string.status_confirmada),
                        R.drawable.bg_etiqueta_verde, R.color.jc_sucesso);
            }

            // so faz sentido cancelar uma reserva confirmada que ainda vai acontecer;
            // nos outros casos o botao some em vez de aparecer desabilitado
            boolean podeCancelar = reserva.isConfirmada() && !passou;
            b.reservaCancelarBotao.setVisibility(podeCancelar ? View.VISIBLE : View.GONE);
            b.reservaCancelarBotao.setOnClickListener(v -> {
                if (aoCancelar != null) {
                    aoCancelar.cancelar(reserva);
                }
            });
        }
    }
}
