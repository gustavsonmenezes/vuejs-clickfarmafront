package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.FarmaciaRequestDTO;
import com.clickfarma.backend.dto.FarmaciaResponseDTO;
import com.clickfarma.backend.model.Farmacia;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.FarmaciaRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FarmaciaService {

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public FarmaciaResponseDTO registrarFarmacia(FarmaciaRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Criar Usuário
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole("PHARMACY");
        usuario.setCidade(dto.getCidade());
        usuario.setEndereco(dto.getEndereco());
        usuario.setTelefone(dto.getTelefone());
        usuario = usuarioRepository.save(usuario);

        // Criar Farmácia
        Farmacia farmacia = new Farmacia();
        farmacia.setNome(dto.getNome());
        farmacia.setCnpj(dto.getCnpj());
        farmacia.setEndereco(dto.getEndereco());
        farmacia.setCidade(dto.getCidade());
        farmacia.setTelefone(dto.getTelefone());
        farmacia.setEmail(dto.getEmail());
        farmacia.setFotoUrl(dto.getFotoUrl());
        farmacia.setUsuario(usuario);
        
        farmacia = farmaciaRepository.save(farmacia);
        return new FarmaciaResponseDTO(farmacia);
    }

    public List<FarmaciaResponseDTO> listarTodas() {
        return farmaciaRepository.findAll().stream()
                .map(FarmaciaResponseDTO::new)
                .collect(Collectors.toList());
    }

    public FarmaciaResponseDTO buscarPorId(Long id) {
        return new FarmaciaResponseDTO(buscarEntidade(id));
    }

    public Farmacia buscarEntidade(Long id) {
        return farmaciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmácia não encontrada"));
    }

    @Transactional
    public FarmaciaResponseDTO atualizar(Long id, FarmaciaRequestDTO dto) {
        Farmacia farmacia = buscarEntidade(id);

        if (dto.getNome() != null) farmacia.setNome(dto.getNome());
        if (dto.getCnpj() != null) farmacia.setCnpj(dto.getCnpj());
        if (dto.getEndereco() != null) farmacia.setEndereco(dto.getEndereco());
        if (dto.getCidade() != null) farmacia.setCidade(dto.getCidade());
        if (dto.getTelefone() != null) farmacia.setTelefone(dto.getTelefone());
        if (dto.getEmail() != null) farmacia.setEmail(dto.getEmail());
        if (dto.getFotoUrl() != null) farmacia.setFotoUrl(dto.getFotoUrl());

        farmacia = farmaciaRepository.save(farmacia);
        return new FarmaciaResponseDTO(farmacia);
    }

    @Transactional
    public FarmaciaResponseDTO atualizarEntidade(Long id, Farmacia f) {
        Farmacia farmacia = buscarEntidade(id);
        if (f.getNome() != null) farmacia.setNome(f.getNome());
        if (f.getChavePix() != null) farmacia.setChavePix(f.getChavePix());
        if (f.getTipoChavePix() != null) farmacia.setTipoChavePix(f.getTipoChavePix());
        // Adicionar outros campos se necessário
        return new FarmaciaResponseDTO(farmaciaRepository.save(farmacia));
    }

    public void deletar(Long id) {
        farmaciaRepository.deleteById(id);
    }
}
