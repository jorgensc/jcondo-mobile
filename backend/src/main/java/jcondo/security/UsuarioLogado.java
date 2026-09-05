package jcondo.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jcondo.entity.Morador;

/**
 * Morador autenticado na requisicao atual, preenchido pelo AuthInterceptor.
 * E o que permite os endpoints nao receberem o id do usuario por parametro.
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
