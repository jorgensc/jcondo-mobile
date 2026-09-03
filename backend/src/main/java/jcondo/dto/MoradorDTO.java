package jcondo.dto;

import jcondo.entity.Morador;

// Versao "publica" do morador: e o que o app recebe, sem a senha
public record MoradorDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String bloco,
        String apartamento,
        String unidade,
        String perfil) {

    public static MoradorDTO de(Morador m) {
        return new MoradorDTO(
                m.getId(),
                m.getNome(),
                m.getEmail(),
                m.getTelefone(),
                m.getBloco(),
                m.getApartamento(),
                m.getUnidade(),
                m.getPerfil());
    }
}
