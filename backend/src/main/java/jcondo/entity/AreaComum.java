package jcondo.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Area comum reservavel: salao de festas, churrasqueira, quadra...
@Entity
@Table(name = "areas_comuns")
public class AreaComum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da área é obrigatório")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nome;

    @Size(max = 300)
    @Column(length = 300)
    private String descricao;

    @Column(nullable = false)
    private Integer capacidade = 0;

    // limites de funcionamento: o servidor recusa reserva fora dessa janela
    @Column(name = "horario_abertura", nullable = false)
    private LocalTime horarioAbertura = LocalTime.of(8, 0);

    @Column(name = "horario_fechamento", nullable = false)
    private LocalTime horarioFechamento = LocalTime.of(22, 0);

    @Size(max = 300)
    @Column(length = 300)
    private String regras;

    @Column(nullable = false)
    private boolean ativa = true;

    public AreaComum() {
    }

    public AreaComum(String nome, String descricao, Integer capacidade,
                     LocalTime horarioAbertura, LocalTime horarioFechamento, String regras) {
        this.nome = nome;
        this.descricao = descricao;
        this.capacidade = capacidade;
        this.horarioAbertura = horarioAbertura;
        this.horarioFechamento = horarioFechamento;
        this.regras = regras;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public LocalTime getHorarioAbertura() {
        return horarioAbertura;
    }

    public void setHorarioAbertura(LocalTime horarioAbertura) {
        this.horarioAbertura = horarioAbertura;
    }

    public LocalTime getHorarioFechamento() {
        return horarioFechamento;
    }

    public void setHorarioFechamento(LocalTime horarioFechamento) {
        this.horarioFechamento = horarioFechamento;
    }

    public String getRegras() {
        return regras;
    }

    public void setRegras(String regras) {
        this.regras = regras;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}
