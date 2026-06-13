package com.clickfarma.backend.service;

import com.clickfarma.backend.model.ItemPedido;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.model.Usuario;
import com.clickfarma.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private ItemPedidoRepository itemPedidoRepository;
    @Mock private RastreioRepository rastreioRepository;
    @Mock private RastreioStreamService rastreioStreamService;
    @Mock private PagamentoService pagamentoService;
    @Mock private AgendamentoRecompraRepository agendamentoRecompraRepository;
    @Mock private EmailNotificationService emailNotificationService;
    @Mock private WhatsAppService whatsAppService;
    @Mock private TelegramService telegramService;
    @Mock private WhatsAppCloudService whatsAppCloudService;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;
    private Produto produto;
    private ItemPedido item;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("João");

        produto = new Produto("Dipirona", BigDecimal.valueOf(8.50), 50);
        produto.setId(1L);

        pedido = new Pedido(usuario);
        pedido.setId(1L);

        item = new ItemPedido(produto, 3);
        item.setId(1L);
        item.setPedido(pedido);
        pedido.adicionarItem(item);
        pedido.setStatus(Pedido.StatusPedido.AGUARDANDO_PAGAMENTO);
    }

    @Test
    void deveCancelarPedidoERestaurarEstoque() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        pedidoService.cancelarPedido(1L);

        assertEquals(Pedido.StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals(53, produto.getEstoque());
        verify(pedidoRepository).save(pedido);
        verify(produtoRepository).save(produto);
    }

    @Test
    void naoDeveCancelarPedidoJaEntregue() {
        pedido.setStatus(Pedido.StatusPedido.ENTREGUE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.cancelarPedido(1L));
        assertTrue(ex.getMessage().contains("não pode ser cancelado"));
    }

    @Test
    void naoDeveCancelarPedidoJaCancelado() {
        pedido.setStatus(Pedido.StatusPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.cancelarPedido(1L));
        assertTrue(ex.getMessage().contains("já está cancelado"));
    }

    @Test
    void deveLancarErroAoBuscarPedidoInexistente() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.buscarPorId(999L));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }

    @Test
    void deveGerarRelatorioComCalculosCorretos() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fim = LocalDateTime.now();

        Pedido pedido2 = new Pedido();
        pedido2.setValorTotal(BigDecimal.valueOf(50.00));

        when(pedidoRepository.findByDataPedidoBetween(inicio, fim))
                .thenReturn(List.of(pedido, pedido2));

        Map<String, Object> relatorio = pedidoService.gerarRelatorio(inicio, fim);

        assertNotNull(relatorio);
        assertTrue(((int) relatorio.get("totalPedidos")) >= 2);
        assertNotNull(relatorio.get("valorTotal"));
        assertNotNull(relatorio.get("contagemStatus"));
    }
}
