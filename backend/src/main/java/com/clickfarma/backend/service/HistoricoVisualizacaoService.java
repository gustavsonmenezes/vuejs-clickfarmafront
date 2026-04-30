package com.clickfarma.backend.service;

import com.clickfarma.backend.model.HistoricoVisualizacao;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.HistoricoVisualizacaoRepository;
import com.clickfarma.backend.repository.ProdutoRepository;
import com.clickfarma.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoVisualizacaoService {

    @Autowired private HistoricoVisualizacaoRepository historicoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProdutoRepository produtoRepository;

    public void registrar(Long usuarioId, Long produtoId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            Produto produto = produtoRepository.findById(produtoId).orElse(null);
            if (usuario == null || produto == null) return;
            historicoRepository.save(new HistoricoVisualizacao(usuario, produto));
        } catch (Exception ignored) {}
    }

    public List<Long> getProdutosMaisVistos(Long usuarioId) {
        List<Object[]> rows = historicoRepository.findTopProdutosByUsuarioId(usuarioId);
        return rows.stream()
                .map(r -> (Long) r[0])
                .collect(Collectors.toList());
    }

    public List<Long> getProdutosRecentes(Long usuarioId) {
        return historicoRepository.findProdutoIdsByUsuarioIdRecent(usuarioId);
    }
}
