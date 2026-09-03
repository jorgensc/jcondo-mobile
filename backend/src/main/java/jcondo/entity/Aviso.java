package jcondo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Comunicado publicado pela administracao e lido pelos moradores no app
@Entity
@Table(name = "avisos")
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String titulo;

    @NotBlank(message = "O conteúdo é obrigatório")
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String conteudo;

    @Size(max = 80)
    @Column(length = 80)
    private String autor;

    // NORMAL, ALTA ou URGENTE - o app usa isso pra colorir a etiqueta do card
    @Column(nullable = false, length = 10)
    private String prioridade = "NORMAL";

    @Column(name = "data_publicacao", nullable = false)
    private LocalDateTime dataPublicacao;

    @Column(nullable = false)
    private boolean ativo = true;

    public Aviso() {
    }

    public Aviso(String titulo, String conteudo, String autor, String prioridade,
                 LocalDateTime dataPublicacao) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.autor = autor;
        this.prioridade = prioridade;
        this.dataPublicacao = dataPublicacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDateTime dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
