package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.ProdutoRequestDTO;
import com.clickfarma.backend.dto.ProdutoResponseDTO;
import com.clickfarma.backend.model.Categoria;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.CategoriaRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Criar produto
    public ProdutoResponseDTO criarProduto(ProdutoRequestDTO produtoDTO) {
        Produto produto = new Produto();
        produto.setNome(produtoDTO.getNome());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setPreco(produtoDTO.getPreco());
        produto.setEstoque(produtoDTO.getEstoque());

        if (produtoDTO.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(produtoDTO.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + produtoDTO.getCategoriaId()));
            produto.setCategoria(categoria);
        }

        Produto produtoSalvo = produtoRepository.save(produto);
        return new ProdutoResponseDTO(produtoSalvo);
    }

    // Listar todos
    public List<ProdutoResponseDTO> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar por ID
    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
        return new ProdutoResponseDTO(produto);
    }

    // Buscar por categoria
    public List<ProdutoResponseDTO> buscarPorCategoria(Long categoriaId) {
        return produtoRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar com filtros (atualizado com emEstoque)
    public List<ProdutoResponseDTO> buscarComFiltros(
            String nome,
            Long categoriaId,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Boolean emEstoque) {

        List<Produto> produtos = produtoRepository.buscarProdutosFiltrados(
                nome, categoriaId, precoMin, precoMax);

        // Filtrar por disponibilidade em estoque se solicitado
        if (emEstoque != null && emEstoque) {
            produtos = produtos.stream()
                    .filter(p -> p.getEstoque() > 0)
                    .collect(Collectors.toList());
        }

        return produtos.stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar produtos com estoque baixo
    public List<ProdutoResponseDTO> buscarEstoqueBaixo(Integer limite) {
        return produtoRepository.findByEstoqueLessThan(limite)
                .stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Atualizar produto
    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoRequestDTO produtoDTO) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));

        produto.setNome(produtoDTO.getNome());
        produto.setDescricao(produtoDTO.getDescricao());
        produto.setPreco(produtoDTO.getPreco());
        produto.setEstoque(produtoDTO.getEstoque());

        if (produtoDTO.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(produtoDTO.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            produto.setCategoria(categoria);
        }

        produto.setDataAtualizacao(LocalDateTime.now());

        Produto produtoAtualizado = produtoRepository.save(produto);
        return new ProdutoResponseDTO(produtoAtualizado);
    }

    // Atualizar estoque
    public ProdutoResponseDTO atualizarEstoque(Long id, Integer quantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        int novoEstoque = produto.getEstoque() + quantidade;
        if (novoEstoque < 0) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + produto.getEstoque());
        }

        produto.setEstoque(novoEstoque);
        produto.setDataAtualizacao(LocalDateTime.now());

        Produto produtoAtualizado = produtoRepository.save(produto);
        return new ProdutoResponseDTO(produtoAtualizado);
    }

    // Deletar produto
    public void deletarProduto(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado com ID: " + id);
        }
        produtoRepository.deleteById(id);
    }
}
