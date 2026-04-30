package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.MotoboyRequestDTO;
import com.clickfarma.backend.dto.MotoboyResponseDTO;
import com.clickfarma.backend.model.Motoboy;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.MotoboyRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotoboyService {

    @Autowired
    private MotoboyRepository motoboyRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public MotoboyResponseDTO registrarMotoboy(MotoboyRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Criar Usuário
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole("COURIER");
        usuario.setTelefone(dto.getTelefone());
        usuario = usuarioRepository.save(usuario);

        // Criar Motoboy
        Motoboy motoboy = new Motoboy();
        motoboy.setNome(dto.getNome());
        motoboy.setCpf(dto.getCpf());
        motoboy.setTelefone(dto.getTelefone());
        motoboy.setUsuario(usuario);
        
        motoboy = motoboyRepository.save(motoboy);
        return new MotoboyResponseDTO(motoboy);
    }

    public List<MotoboyResponseDTO> listarTodos() {
        return motoboyRepository.findAll().stream()
                .map(MotoboyResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void deletar(Long id) {
        motoboyRepository.deleteById(id);
    }

    @Transactional
    public MotoboyResponseDTO atualizar(Long id, MotoboyRequestDTO dto) {
        Motoboy m = motoboyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motoboy não encontrado"));
        if (dto.getNome() != null) m.setNome(dto.getNome());
        if (dto.getCpf() != null) m.setCpf(dto.getCpf());
        if (dto.getTelefone() != null) m.setTelefone(dto.getTelefone());
        if (dto.getChavePix() != null) m.setChavePix(dto.getChavePix());
        if (dto.getTipoChavePix() != null) m.setTipoChavePix(dto.getTipoChavePix());
        return new MotoboyResponseDTO(motoboyRepository.save(m));
    }

    @Transactional
    public MotoboyResponseDTO atualizarPix(Long id, String chavePix, String tipoChavePix) {
        Motoboy motoboy = motoboyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motoboy não encontrado"));
        if (chavePix != null) motoboy.setChavePix(chavePix);
        if (tipoChavePix != null) motoboy.setTipoChavePix(tipoChavePix);
        return new MotoboyResponseDTO(motoboyRepository.save(motoboy));
    }
}
