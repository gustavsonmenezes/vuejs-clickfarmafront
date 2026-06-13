package com.clickfarma.backend.service;

import com.clickfarma.backend.dto.ProdutoRequestDTO;
import com.clickfarma.backend.dto.ProdutoResponseDTO;
import com.clickfarma.backend.model.Categoria;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.CategoriaRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private GroqService groqService;

    @InjectMocks
    private ProdutoService produtoService;

    private Categoria categoria;
    private Produto produto;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Medicamentos");

        produto = new Produto("Paracetamol", BigDecimal.valueOf(15.90), 100);
        produto.setId(1L);
        produto.setCategoria(categoria);
    }

    @Test
    void deveCriarProdutoComSucesso() {
        ProdutoRequestDTO dto = new ProdutoRequestDTO();
        dto.setNome("Paracetamol");
        dto.setPreco(BigDecimal.valueOf(15.90));
        dto.setEstoque(100);
        dto.setCategoriaId(1L);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        ProdutoResponseDTO resultado = produtoService.criarProduto(dto);

        assertNotNull(resultado);
        assertEquals("Paracetamol", resultado.getNome());

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoRepository).save(captor.capture());
        assertEquals(100, captor.getValue().getEstoque());
    }

    @Test
    void deveLancarErroAoBuscarProdutoInexistente() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> produtoService.buscarPorId(999L));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }

    @Test
    void deveListarProdutosComEstoqueBaixo() {
        when(produtoRepository.findByEstoqueLessThan(10))
                .thenReturn(List.of(produto));

        List<ProdutoResponseDTO> resultado = produtoService.buscarEstoqueBaixo(10);

        assertEquals(1, resultado.size());
        assertEquals("Paracetamol", resultado.get(0).getNome());
    }

    @Test
    void deveAtualizarEstoque() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        ProdutoResponseDTO resultado = produtoService.atualizarEstoque(1L, 50);

        assertEquals(150, produto.getEstoque());
    }

    @Test
    void deveLancarErroAoRemoverEstoqueInsuficiente() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> produtoService.atualizarEstoque(1L, -200));
        assertTrue(ex.getMessage().contains("Estoque insuficiente"));
    }

    @Test
    void deveFiltrarPorNomeOuCategoria() {
        when(produtoRepository.buscarProdutosFiltrados(
                "paracetamol", 1L, null, null))
                .thenReturn(List.of(produto));

        List<ProdutoResponseDTO> resultado = produtoService.buscarComFiltros(
                "paracetamol", 1L, null, null, null);

        assertEquals(1, resultado.size());
    }
}
