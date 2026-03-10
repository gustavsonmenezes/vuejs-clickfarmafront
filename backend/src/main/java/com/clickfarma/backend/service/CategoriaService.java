package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.CategoriaRequestDTO;
import com.clickfarma.backend.dto.CategoriaResponseDTO;
import com.clickfarma.backend.dto.ProdutoResponseDTO;
import com.clickfarma.backend.model.Categoria;
import com.clickfarma.backend.repository.CategoriaRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    // Criar categoria
    public CategoriaResponseDTO criarCategoria(CategoriaRequestDTO categoriaDTO) {
        if (categoriaRepository.existsByNome(categoriaDTO.getNome())) {
            throw new RuntimeException("Categoria já existe: " + categoriaDTO.getNome());
        }

        Categoria categoria = new Categoria();
        categoria.setNome(categoriaDTO.getNome());
        categoria.setDescricao(categoriaDTO.getDescricao());

        Categoria categoriaSalva = categoriaRepository.save(categoria);
        return new CategoriaResponseDTO(categoriaSalva);
    }

    // Listar todas
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(CategoriaResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Buscar por ID
    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        return new CategoriaResponseDTO(categoria);
    }

    // Buscar por nome
    public CategoriaResponseDTO buscarPorNome(String nome) {
        Categoria categoria = categoriaRepository.findByNome(nome)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com nome: " + nome));
        return new CategoriaResponseDTO(categoria);
    }

    // Listar produtos por categoria
    public List<ProdutoResponseDTO> listarProdutosPorCategoria(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        return produtoRepository.findByCategoriaId(categoriaId)
                .stream()
                .map(ProdutoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Atualizar categoria
    public CategoriaResponseDTO atualizarCategoria(Long id, CategoriaRequestDTO categoriaDTO) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));

        // Verificar se já existe outra categoria com o mesmo nome
        if (!categoria.getNome().equals(categoriaDTO.getNome()) &&
                categoriaRepository.existsByNome(categoriaDTO.getNome())) {
            throw new RuntimeException("Já existe uma categoria com o nome: " + categoriaDTO.getNome());
        }

        categoria.setNome(categoriaDTO.getNome());
        categoria.setDescricao(categoriaDTO.getDescricao());

        Categoria categoriaAtualizada = categoriaRepository.save(categoria);
        return new CategoriaResponseDTO(categoriaAtualizada);
    }

    // Deletar categoria
    public void deletarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));

        // Verificar se existem produtos vinculados
        if (!categoria.getProdutos().isEmpty()) {
            throw new RuntimeException("Não é possível deletar categoria com produtos vinculados");
        }

        categoriaRepository.deleteById(id);
    }

    // Gerar relatório
    public Map<String, Object> gerarRelatorio() {
        List<Categoria> categorias = categoriaRepository.findAll();

        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("totalCategorias", categorias.size());

        // Categorias com mais produtos
        Map<String, Integer> produtosPorCategoria = new HashMap<>();
        for (Categoria cat : categorias) {
            produtosPorCategoria.put(
                    cat.getNome(),
                    cat.getProdutos() != null ? cat.getProdutos().size() : 0
            );
        }
        relatorio.put("produtosPorCategoria", produtosPorCategoria);

        return relatorio;
    }
}


