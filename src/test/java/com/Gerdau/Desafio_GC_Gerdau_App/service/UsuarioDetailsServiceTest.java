package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.model.PerfilUsuario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Usuario;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    UsuarioRepository repository;

    @InjectMocks
    UsuarioDetailsService service;

    private Usuario usuario(String username, PerfilUsuario perfil, boolean ativo) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setSenhaHash("$2a$hash");
        u.setPerfil(perfil);
        u.setAtivo(ativo);
        return u;
    }

    @Test
    void carrega_usuarioAtivo() {
        when(repository.findByUsername("ana")).thenReturn(Optional.of(usuario("ana", PerfilUsuario.APROVADOR, true)));

        UserDetails detalhes = service.loadUserByUsername("ana");

        assertThat(detalhes.getUsername()).isEqualTo("ana");
        assertThat(detalhes.getPassword()).isEqualTo("$2a$hash");
        assertThat(detalhes.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_APROVADOR");
        assertThat(detalhes.isEnabled()).isTrue();
    }

    @Test
    void usuarioInativo_ficaDesabilitado() {
        when(repository.findByUsername("ana")).thenReturn(Optional.of(usuario("ana", PerfilUsuario.COMPRADOR, false)));

        assertThat(service.loadUserByUsername("ana").isEnabled()).isFalse();
    }

    @Test
    void usuarioInexistente_lancaUsernameNotFound() {
        when(repository.findByUsername("zé")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("zé"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
