package jcondo.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jcondo.entity.AreaComum;
import jcondo.entity.Aviso;
import jcondo.entity.Morador;
import jcondo.entity.Ocorrencia;
import jcondo.entity.Reserva;
import jcondo.repository.AreaComumRepository;
import jcondo.repository.AvisoRepository;
import jcondo.repository.MoradorRepository;
import jcondo.repository.OcorrenciaRepository;
import jcondo.repository.ReservaRepository;
import jcondo.security.SenhaUtil;

/**
 * Popula o banco na primeira execucao, para o app nunca abrir com telas vazias
 * na apresentacao. Cada bloco so roda se a tabela correspondente estiver vazia,
 * entao rodar a aplicacao de novo nao duplica nada.
 *
 * Credenciais de teste:
 *   ana.souza@email.com  / 123456   (perfil MORADOR)
 *   sindico@jcondo.com   / admin123 (perfil ADMIN)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final MoradorRepository moradorRepository;
    private final AvisoRepository avisoRepository;
    private final AreaComumRepository areaRepository;
    private final ReservaRepository reservaRepository;
    private final OcorrenciaRepository ocorrenciaRepository;

    public DataSeeder(MoradorRepository moradorRepository,
                      AvisoRepository avisoRepository,
                      AreaComumRepository areaRepository,
                      ReservaRepository reservaRepository,
                      OcorrenciaRepository ocorrenciaRepository) {
        this.moradorRepository = moradorRepository;
        this.avisoRepository = avisoRepository;
        this.areaRepository = areaRepository;
        this.reservaRepository = reservaRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
    }

    @Override
    public void run(String... args) {
        semearMoradores();
        semearAreas();
        semearAvisos();
        semearMovimento();
    }

    private void semearMoradores() {
        if (moradorRepository.count() > 0) {
            // banco ja tem gente cadastrada: so garante que todo mundo
            // consegue entrar no app, mesmo quem foi criado antes da AA2
            List<Morador> semSenha = moradorRepository.findAll().stream()
                    .filter(m -> m.getSenha() == null || m.getSenha().isBlank())
                    .toList();
            for (Morador m : semSenha) {
                m.setSenha(SenhaUtil.hash("123456"));
                if (m.getPerfil() == null || m.getPerfil().isBlank()) {
                    m.setPerfil(Morador.PERFIL_MORADOR);
                }
            }
            if (!semSenha.isEmpty()) {
                moradorRepository.saveAll(semSenha);
            }
            return;
        }

        Morador ana = novo("Ana Paula Souza", "101", "A", "123.456.789-01",
                "(49) 99911-1111", "ana.souza@email.com", "123456", Morador.PERFIL_MORADOR);
        Morador bruno = novo("Bruno Oliveira Lima", "202", "B", "234.567.890-12",
                "(49) 99922-2222", "bruno.lima@email.com", "123456", Morador.PERFIL_MORADOR);
        Morador carla = novo("Carla Mendes Rocha", "303", "A", "345.678.901-23",
                "(49) 99933-3333", "carla.rocha@email.com", "123456", Morador.PERFIL_MORADOR);
        Morador diego = novo("Diego Ferreira Alves", "104", "C", "456.789.012-34",
                "(49) 99944-4444", "diego.alves@email.com", "123456", Morador.PERFIL_MORADOR);
        Morador sindico = novo("Roberto Síndico", "001", "A", "999.888.777-66",
                "(49) 99900-0000", "sindico@jcondo.com", "admin123", Morador.PERFIL_ADMIN);

        moradorRepository.saveAll(List.of(ana, bruno, carla, diego, sindico));
    }

    private void semearAreas() {
        if (areaRepository.count() > 0) {
            return;
        }
        areaRepository.saveAll(List.of(
                new AreaComum("Salão de Festas",
                        "Espaço coberto para até 60 pessoas, com cozinha de apoio.",
                        60, LocalTime.of(10, 0), LocalTime.of(22, 0),
                        "Devolver o espaço limpo. Som permitido até as 22h."),
                new AreaComum("Churrasqueira",
                        "Área externa com duas churrasqueiras e mesas.",
                        30, LocalTime.of(10, 0), LocalTime.of(22, 0),
                        "Carvão e utensílios por conta do morador."),
                new AreaComum("Quadra Poliesportiva",
                        "Quadra descoberta para futsal, vôlei e basquete.",
                        20, LocalTime.of(8, 0), LocalTime.of(22, 0),
                        "Uso de calçado adequado é obrigatório."),
                new AreaComum("Espaço Gourmet",
                        "Sala climatizada com mesa para 12 lugares.",
                        12, LocalTime.of(10, 0), LocalTime.of(22, 0),
                        "Reserva limitada a uma por final de semana."),
                new AreaComum("Sala de Reuniões",
                        "Sala para reuniões de condomínio e home office.",
                        10, LocalTime.of(8, 0), LocalTime.of(20, 0),
                        "Agendar com antecedência mínima de 24 horas.")));
    }

    private void semearAvisos() {
        if (avisoRepository.count() > 0) {
            return;
        }
        LocalDateTime agora = LocalDateTime.now();
        avisoRepository.saveAll(List.of(
                new Aviso("Manutenção dos elevadores na quinta-feira",
                        "A manutenção preventiva dos elevadores dos blocos A e B acontece na "
                        + "quinta-feira, das 8h às 12h. Durante o período apenas um elevador por "
                        + "bloco ficará em operação. Pedimos que evitem mudanças nesse horário.",
                        "Administração", "ALTA", agora.minusHours(3)),
                new Aviso("Assembleia ordinária dia 20",
                        "A assembleia ordinária para aprovação da previsão orçamentária do "
                        + "próximo exercício acontece no dia 20, às 19h30, no salão de festas. "
                        + "A pauta completa está afixada no mural da portaria.",
                        "Síndico", "NORMAL", agora.minusDays(1)),
                new Aviso("Limpeza da caixa d'água - interrupção do fornecimento",
                        "Na próxima terça-feira o fornecimento de água será interrompido das 9h "
                        + "às 14h para a limpeza semestral das caixas d'água. Recomendamos "
                        + "armazenar água para o consumo durante o período.",
                        "Administração", "URGENTE", agora.minusDays(2)),
                new Aviso("Nova regra para uso da churrasqueira",
                        "A partir deste mês a churrasqueira deve ser reservada pelo aplicativo. "
                        + "A reserva presencial na portaria fica descontinuada. Cada unidade "
                        + "pode reservar até duas vezes por mês.",
                        "Administração", "NORMAL", agora.minusDays(5)),
                new Aviso("Coleta seletiva às segundas e quintas",
                        "Lembramos que a coleta seletiva passa às segundas e quintas pela manhã. "
                        + "Os recicláveis devem ser depositados no container azul do subsolo.",
                        "Administração", "NORMAL", agora.minusDays(9))));
    }

    /** Deixa uma reserva e uma ocorrencia de exemplo no cadastro da Ana. */
    private void semearMovimento() {
        if (reservaRepository.count() > 0 && ocorrenciaRepository.count() > 0) {
            return;
        }
        Morador ana = moradorRepository.findByEmailIgnoreCase("ana.souza@email.com").orElse(null);
        if (ana == null) {
            return;
        }

        if (reservaRepository.count() == 0) {
            List<AreaComum> areas = areaRepository.findByAtivaTrueOrderByNomeAsc();
            if (!areas.isEmpty()) {
                AreaComum salao = areas.stream()
                        .filter(a -> a.getNome().startsWith("Salão"))
                        .findFirst().orElse(areas.get(0));
                Reserva r = new Reserva(ana, salao, LocalDate.now().plusDays(6),
                        LocalTime.of(18, 0), LocalTime.of(20, 0));
                reservaRepository.save(r);
            }
        }

        if (ocorrenciaRepository.count() == 0) {
            Ocorrencia o = new Ocorrencia("20260901-1042", ana, "Iluminação",
                    "Lâmpada queimada na garagem",
                    "A lâmpada da vaga 42, no subsolo 1, está queimada há três dias. "
                    + "À noite o corredor fica bem escuro.");
            o.setStatus(Ocorrencia.STATUS_EM_ANDAMENTO);
            o.setDataAbertura(LocalDateTime.now().minusDays(2));
            o.setDataAtualizacao(LocalDateTime.now().minusHours(20));
            o.setResposta("Material solicitado ao fornecedor. Troca prevista para esta semana.");
            ocorrenciaRepository.save(o);
        }
    }

    private Morador novo(String nome, String apto, String bloco, String cpf,
                         String telefone, String email, String senha, String perfil) {
        Morador m = new Morador(nome, apto, bloco, cpf, telefone, email);
        m.setSenha(SenhaUtil.hash(senha));
        m.setPerfil(perfil);
        return m;
    }
}
