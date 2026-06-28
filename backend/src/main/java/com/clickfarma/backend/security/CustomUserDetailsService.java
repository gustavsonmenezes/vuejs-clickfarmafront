package com.clickfarma.backend.security;

import com.clickfarma.backend.model.Entregador;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.EntregadorRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username).orElse(null);
        if (usuario != null) {
            return new User(usuario.getEmail(), usuario.getSenha(), new ArrayList<>());
        }

        Entregador entregador = entregadorRepository.findByCpf(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário ou entregador não encontrado: " + username));

        return User.withUsername(entregador.getCpf())
                .password(entregador.getSenha())
                .roles("ENTREGADOR")
                .build();
    }
}