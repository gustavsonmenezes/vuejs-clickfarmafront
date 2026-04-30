package com.clickfarma.backend.security;

import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (usuario.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole()));
        }

        return new User(usuario.getEmail(), usuario.getSenha(), authorities);
    }
}