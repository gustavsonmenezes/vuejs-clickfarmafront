package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.entregador.*;
import com.clickfarma.backend.model.Entregador;
import com.clickfarma.backend.model.Entregador.StatusEntregador;
import com.clickfarma.backend.model.Transacao;
import com.clickfarma.backend.repository.EntregadorRepository;
import com.clickfarma.backend.repository.TransacaoRepository;
import com.clickfarma.backend.security.JwtUtil;
import com.clickfarma.backend.service.EntregaTrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/entregadores")
public class EntregadorController {

    private final EntregadorRepository entregadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EntregaTrackingService trackingService;
    private final TransacaoRepository transacaoRepository;

    public EntregadorController(EntregadorRepository entregadorRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtUtil jwtUtil,
                                 EntregaTrackingService trackingService,
                                 TransacaoRepository transacaoRepository) {
        this.entregadorRepository = entregadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.trackingService = trackingService;
        this.transacaoRepository = transacaoRepository;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody EntregadorRequestDTO dto) {
        if (entregadorRepository.findByCpf(dto.getCpf()).isPresent()) {
            return ResponseEntity.badRequest().body("CPF já cadastrado");
        }

        Entregador e = new Entregador();
        e.setNome(dto.getNome());
        e.setCpf(dto.getCpf());
        e.setSenha(passwordEncoder.encode(dto.getSenha()));
        e.setTelefone(dto.getTelefone());
        e.setCnh(dto.getCnh());
        e.setPlacaVeiculo(dto.getPlacaVeiculo());
        e.setModeloVeiculo(dto.getModeloVeiculo());
        e.setChavePix(dto.getChavePix());
        e.setStatus(StatusEntregador.PENDENTE);
        e.setAtivo(true);
        e.setDataCadastro(LocalDateTime.now());

        entregadorRepository.save(e);
        return ResponseEntity.status(HttpStatus.CREATED).body(EntregadorResponseDTO.fromEntity(e));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody EntregadorLoginRequestDTO dto) {
        Entregador e = entregadorRepository.findByCpfAndAtivoTrue(dto.getCpf()).orElse(null);
        if (e == null || !passwordEncoder.matches(dto.getSenha(), e.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("CPF ou senha inválidos");
        }
        if (e.getStatus() == StatusEntregador.PENDENTE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cadastro aguardando aprovação");
        }
        if (e.getStatus() == StatusEntregador.BLOQUEADO) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Entregador bloqueado");
        }

        String token = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(
                e.getCpf(), e.getSenha(), java.util.Collections.emptyList()));

        return ResponseEntity.ok(new EntregadorLoginResponseDTO(token, EntregadorResponseDTO.fromEntity(e)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return entregadorRepository.findById(id)
                .map(e -> ResponseEntity.ok(EntregadorResponseDTO.fromEntity(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/localizacao")
    public ResponseEntity<?> atualizarLocalizacao(@PathVariable Long id,
                                                   @Valid @RequestBody LocalizacaoRequestDTO dto) {
        Entregador e = entregadorRepository.findById(id).orElse(null);
        if (e == null) return ResponseEntity.notFound().build();

        e.setLatitude(dto.getLatitude());
        e.setLongitude(dto.getLongitude());
        e.setUltimaAtualizacao(LocalDateTime.now());
        entregadorRepository.save(e);

        trackingService.atualizarPosicao(id, dto.getLatitude(), dto.getLongitude());

        return ResponseEntity.ok(EntregadorResponseDTO.fromEntity(e));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                        @RequestBody EntregadorRequestDTO dto) {
        Entregador e = entregadorRepository.findById(id).orElse(null);
        if (e == null) return ResponseEntity.notFound().build();

        e.setNome(dto.getNome());
        e.setTelefone(dto.getTelefone());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            e.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        e.setCnh(dto.getCnh());
        e.setPlacaVeiculo(dto.getPlacaVeiculo());
        e.setModeloVeiculo(dto.getModeloVeiculo());
        e.setChavePix(dto.getChavePix());
        entregadorRepository.save(e);
        return ResponseEntity.ok(EntregadorResponseDTO.fromEntity(e));
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<EntregadorResponseDTO>> listarDisponiveis() {
        return ResponseEntity.ok(
                entregadorRepository.findByStatusAndAtivoTrue(StatusEntregador.ATIVO)
                        .stream().map(EntregadorResponseDTO::fromEntity).toList()
        );
    }

    @GetMapping("/com-localizacao")
    public ResponseEntity<List<EntregadorResponseDTO>> listarComLocalizacao() {
        return ResponseEntity.ok(
                entregadorRepository.findByAtivoTrueAndLatitudeIsNotNull()
                        .stream().map(EntregadorResponseDTO::fromEntity).toList()
        );
    }

    @PostMapping("/{id}/saque")
    public ResponseEntity<?> solicitarSaque(@PathVariable Long id,
                                             @Valid @RequestBody SaqueRequestDTO dto) {
        Entregador e = entregadorRepository.findById(id).orElse(null);
        if (e == null) return ResponseEntity.notFound().build();
        if (e.getChavePix() == null || e.getChavePix().isBlank()) {
            return ResponseEntity.badRequest().body("Chave PIX não configurada");
        }
        if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Valor inválido");
        }

        Transacao t = new Transacao();
        t.setEntregadorId(id);
        t.setValor(dto.getValor());
        t.setChavePix(e.getChavePix());
        t.setTipo("SAQUE");
        t.setStatus(Transacao.StatusTransacao.PENDENTE);
        transacaoRepository.save(t);

        return ResponseEntity.ok(TransacaoResponseDTO.fromEntity(t));
    }

    @GetMapping("/{id}/transacoes")
    public ResponseEntity<List<TransacaoResponseDTO>> listarTransacoes(@PathVariable Long id) {
        return ResponseEntity.ok(
                transacaoRepository.findByEntregadorIdOrderByCriadoEmDesc(id)
                        .stream().map(TransacaoResponseDTO::fromEntity).toList()
        );
    }

    // --- Admin endpoints ---

    @GetMapping("/admin/aprovados")
    public ResponseEntity<List<EntregadorResponseDTO>> listarAprovados() {
        return ResponseEntity.ok(
                entregadorRepository.findByStatusAndAtivoTrue(StatusEntregador.ATIVO)
                        .stream().map(EntregadorResponseDTO::fromEntity).toList()
        );
    }

    @GetMapping("/admin/rejeitados")
    public ResponseEntity<List<EntregadorResponseDTO>> listarRejeitados() {
        return ResponseEntity.ok(
                entregadorRepository.findByStatus(StatusEntregador.BLOQUEADO)
                        .stream().map(EntregadorResponseDTO::fromEntity).toList()
        );
    }

    @GetMapping("/admin/pendentes")
    public ResponseEntity<List<EntregadorResponseDTO>> listarPendentes() {
        return ResponseEntity.ok(
                entregadorRepository.findByStatusAndAtivoTrue(StatusEntregador.PENDENTE)
                        .stream().map(EntregadorResponseDTO::fromEntity).toList()
        );
    }

    @PutMapping("/admin/{id}/aprovar")
    public ResponseEntity<?> aprovar(@PathVariable Long id) {
        Entregador e = entregadorRepository.findById(id).orElse(null);
        if (e == null) return ResponseEntity.notFound().build();
        e.setStatus(StatusEntregador.ATIVO);
        entregadorRepository.save(e);
        return ResponseEntity.ok(EntregadorResponseDTO.fromEntity(e));
    }

    @PutMapping("/admin/{id}/rejeitar")
    public ResponseEntity<?> rejeitar(@PathVariable Long id) {
        Entregador e = entregadorRepository.findById(id).orElse(null);
        if (e == null) return ResponseEntity.notFound().build();
        e.setStatus(StatusEntregador.BLOQUEADO);
        entregadorRepository.save(e);
        return ResponseEntity.ok(EntregadorResponseDTO.fromEntity(e));
    }
}
