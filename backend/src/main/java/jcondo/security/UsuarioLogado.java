package jcondo.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jcondo.entity.Morador;

/**
 * Guarda o morador autenticado durante uma requisicao.
 *
 * O AuthInterceptor preenche isso depois de validar o token, e os controllers
 * so pedem o objeto - assim nenhum endpoint precisa receber o id do usuario
 * por parametro, o que impediria um morador de consultar dados de outro.
 */
@Component
@RequestScope
public class UsuarioLogado {

    private Morador morador;

    public Morador getMorador() {
        return morador;
    }

    public void setMorador(Morador morador) {
        this.morador = morador;
    }

    public Long getId() {
        return morador == null ? null : morador.getId();
    }

    public boolean isAdmin() {
        return morador != null && Morador.PERFIL_ADMIN.equalsIgnoreCase(morador.getPerfil());
    }
}
