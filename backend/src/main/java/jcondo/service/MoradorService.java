package jcondo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jcondo.entity.Morador;
import jcondo.repository.MoradorRepository;
import jcondo.security.SenhaUtil;

// Camada de servico - o controller nunca fala direto com o repository
@Service
public class MoradorService {

    // usada quando a administracao cadastra um morador sem definir senha;
    // o morador troca no primeiro acesso, pelo perfil no app
    public static final String SENHA_PADRAO = "123456";

    private final MoradorRepository repository;

    // injecao pelo construtor (nao precisa de @Autowired)
    public MoradorService(MoradorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Morador> listarTodos() {
        return repository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Morador buscarPorId(Long id) {
        // se nao achar lanca excecao, quem chamou trata e mostra msg de erro
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Morador não encontrado com o ID: " + id));
    }

    @Transactional
    public Morador salvar(Morador morador) {
        if (morador.getId() == null) {
            prepararNovo(morador);
        } else {
            preservarDadosNaoEnviados(morador);
        }
        // o save do JPA ja resolve os dois casos: insert se id null, update se nao
        return repository.save(morador);
    }

    @Transactional
    public void excluir(Long id) {
        Morador morador = buscarPorId(id);
        repository.delete(morador);
    }

    private void prepararNovo(Morador morador) {
        String senha = morador.getSenha();
        if (senha == null || senha.isBlank()) {
            senha = SENHA_PADRAO;
        }
        morador.setSenha(pareceHash(senha) ? senha : SenhaUtil.hash(senha));
        if (morador.getPerfil() == null || morador.getPerfil().isBlank()) {
            morador.setPerfil(Morador.PERFIL_MORADOR);
        }
    }

    /**
     * Na edicao, o formulario web nao envia a senha. Sem este tratamento o
     * update gravaria null por cima do hash e o morador perderia o acesso ao app.
     */
    private void preservarDadosNaoEnviados(Morador morador) {
        Morador atual = repository.findById(morador.getId()).orElse(null);

        if (morador.getSenha() == null || morador.getSenha().isBlank()) {
            morador.setSenha(atual == null ? SenhaUtil.hash(SENHA_PADRAO) : atual.getSenha());
        } else if (!pareceHash(morador.getSenha())) {
            // veio senha nova em texto puro (troca de senha) - grava o hash
            morador.setSenha(SenhaUtil.hash(morador.getSenha()));
        }

        if (morador.getPerfil() == null || morador.getPerfil().isBlank()) {
            morador.setPerfil(atual == null ? Morador.PERFIL_MORADOR : atual.getPerfil());
        }
    }

    /**
     * Distingue uma senha em texto puro de um hash SHA-256 ja gravado
     * (64 caracteres hexadecimais), pra nao aplicar hash duas vezes no mesmo valor.
     */
    private static boolean pareceHash(String valor) {
        if (valor == null || valor.length() != 64) {
            return false;
        }
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            boolean hex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
            if (!hex) {
                return false;
            }
        }
        return true;
    }
}
