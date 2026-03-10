package com.clickfarma.backend.controller;

import com.clickfarma.backend.dto.CategoriaRequestDTO;
import com.clickfarma.backend.dto.CategoriaResponseDTO;
import com.clickfarma.backend.dto.MensagemResponseDTO;
import com.clickfarma.backend.dto.ProdutoResponseDTO;
import com.clickfarma.backend.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:8082")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    // POST - Criar nova categoria
    @PostMapping
    public ResponseEntity<?> criarCategoria(@Valid @RequestBody CategoriaRequestDTO categoriaDTO) {
        try {
            CategoriaResponseDTO novaCategoria = categoriaService.criarCategoria(categoriaDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MensagemResponseDTO(
                            "Categoria criada com sucesso!",
                            true,
                            novaCategoria
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Listar todas as categorias
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    // GET - Buscar categoria por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            CategoriaResponseDTO categoria = categoriaService.buscarPorId(id);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Buscar categoria por nome
    @GetMapping("/nome/{nome}")
    public ResponseEntity<?> buscarPorNome(@PathVariable String nome) {
        try {
            CategoriaResponseDTO categoria = categoriaService.buscarPorNome(nome);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Listar produtos de uma categoria
    @GetMapping("/{id}/produtos")
    public ResponseEntity<?> listarProdutosPorCategoria(@PathVariable Long id) {
        try {
            List<ProdutoResponseDTO> produtos = categoriaService.listarProdutosPorCategoria(id);
            return ResponseEntity.ok(produtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // PUT - Atualizar categoria
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO categoriaDTO) {
        try {
            CategoriaResponseDTO categoriaAtualizada = categoriaService.atualizarCategoria(id, categoriaDTO);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Categoria atualizada com sucesso!",
                    true,
                    categoriaAtualizada
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // DELETE - Deletar categoria
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCategoria(@PathVariable Long id) {
        try {
            categoriaService.deletarCategoria(id);
            return ResponseEntity.ok(new MensagemResponseDTO(
                    "Categoria deletada com sucesso!",
                    true
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }

    // GET - Relatório de categorias
    @GetMapping("/relatorio")
    public ResponseEntity<?> gerarRelatorio() {
        try {
            Object relatorio = categoriaService.gerarRelatorio();
            return ResponseEntity.ok(relatorio);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensagemResponseDTO(e.getMessage(), false));
        }
    }
}