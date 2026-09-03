package com.jcondo.mobile.ui.avisos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcondo.mobile.api.model.Aviso;
import com.jcondo.mobile.databinding.ItemAvisoBinding;
import com.jcondo.mobile.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class AvisoAdapter extends RecyclerView.Adapter<AvisoAdapter.Item> {

    public interface AoTocar {
        void aviso(Aviso aviso);
    }

    private final List<Aviso> itens = new ArrayList<>();
    private final AoTocar aoTocar;

    public AvisoAdapter(AoTocar aoTocar) {
        this.aoTocar = aoTocar;
    }

    public void substituir(List<Aviso> novos) {
        itens.clear();
        if (novos != null) {
            itens.addAll(novos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Item onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Item(ItemAvisoBinding.inflate(
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

        private final ItemAvisoBinding b;

        Item(ItemAvisoBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void mostrar(Aviso aviso, AoTocar aoTocar) {
            b.avisoTitulo.setText(aviso.titulo);
            b.avisoResumo.setText(aviso.conteudo);
            b.avisoData.setText(aviso.dataPublicacao);
            b.avisoAutor.setText(aviso.autor);

            // a prioridade vira etiqueta colorida COM texto - quem nao
            // distingue a cor continua lendo "Urgente" ou "Importante"
            int[] estilo = Ui.estiloPrioridade(aviso.prioridade);
            Ui.etiqueta(b.avisoPrioridade,
                    Ui.rotuloPrioridade(b.getRoot().getContext(), aviso.prioridade),
                    estilo[0], estilo[1]);

            b.avisoCartao.setOnClickListener(v -> {
                if (aoTocar != null) {
                    aoTocar.aviso(aviso);
                }
            });
        }
    }
}
