package jcondo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Corpo do POST /api/ocorrencias
public class OcorrenciaRequest {

    @NotBlank(message = "Selecione a categoria")
    @Size(max = 40)
    private String categoria;

    @NotBlank(message = "Informe um título para a ocorrência")
    @Size(max = 120, message = "O título deve ter no máximo 120 caracteres")
    private String titulo;

    @NotBlank(message = "Descreva o que aconteceu")
    @Size(max = 2000)
    private String descricao;

    public OcorrenciaRequest() {
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
