package com.jcondo.mobile.ui.ocorrencias;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcondo.mobile.R;
import com.jcondo.mobile.api.model.Ocorrencia;
import com.jcondo.mobile.databinding.ItemOcorrenciaBinding;
import com.jcondo.mobile.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class OcorrenciaAdapter extends RecyclerView.Adapter<OcorrenciaAdapter.Item> {

    public interface AoTocar {
        void ocorrencia(Ocorrencia ocorrencia);
    }

    private final List<Ocorrencia> itens = new ArrayList<>();
    private final AoTocar aoTocar;

    public OcorrenciaAdapter(AoTocar aoTocar) {
        this.aoTocar = aoTocar;
    }

    public void substituir(List<Ocorrencia> novos) {
        itens.clear();
        if (novos != null) {
            itens.addAll(novos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Item onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Item(ItemOcorrenciaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Item holder, int position) {
        holder.mostrar(itens.get(position), aoTocar);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Item extends RecyclerView.ViewHolder {

        private final ItemOcorrenciaBinding b;

        Item(ItemOcorrenciaBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void mostrar(Ocorrencia o, AoTocar aoTocar) {
            b.ocorrenciaTitulo.setText(o.titulo);
            b.ocorrenciaCategoria.setText(o.categoria);
            b.ocorrenciaProtocolo.setText(b.getRoot().getContext()
                    .getString(R.string.ocorrencia_protocolo, o.protocolo));
            b.ocorrenciaData.setText(b.getRoot().getContext()
                    .getString(R.string.ocorrencia_aberta_em, o.dataAbertura));

            int[] estilo = Ui.estiloStatusOcorrencia(o.status);
            Ui.etiqueta(b.ocorrenciaStatus,
                    o.statusLabel == null ? o.status : o.statusLabel,
                    estilo[0], estilo[1]);

            b.ocorrenciaCartao.setOnClickListener(v -> {
                if (aoTocar != null) {
                    aoTocar.ocorrencia(o);
                }
            });
        }
    }
}
