package com.clickfarma.backend.service;

import com.clickfarma.backend.model.ItemPedido;
import com.clickfarma.backend.model.Pedido;
import com.clickfarma.backend.model.Produto;
import com.clickfarma.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatToolService {

    private static final Logger log = LoggerFactory.getLogger(ChatToolService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private AgendamentoRecompraRepository agendamentoRecompraRepository;
    @Autowired
    private RastreioRepository rastreioRepository;

    public List<Map<String, Object>> getToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();

        tools.add(Map.of(
            "type", "function",
            "function", Map.of(
                "name", "buscarPedidos",
                "description", "Lista os pedidos recentes do usuario logado com status, valor total e data",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(),
                    "required", List.of()
                )
            )
        ));

        tools.add(Map.of(
            "type", "function",
            "function", Map.of(
                "name", "buscarMedicamentos",
                "description", "Lista os medicamentos do usuario com status de estoque (critico/atencao/ok) e dias restantes",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(),
                    "required", List.of()
                )
            )
        ));

        tools.add(Map.of(
            "type", "function",
            "function", Map.of(
                "name", "rastrearPedido",
                "description", "Busca informacao de rastreio de um pedido pelo codigo do pedido",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "codigo", Map.of(
                            "type", "string",
                            "description", "Codigo do pedido (ex: PED-XXXX)"
                        )
                    ),
                    "required", List.of("codigo")
                )
            )
        ));

        tools.add(Map.of(
            "type", "function",
            "function", Map.of(
                "name", "consultarEstoque",
                "description", "Consulta o estoque disponivel de um medicamento ou produto pelo nome",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "produtoNome", Map.of(
                            "type", "string",
                            "description", "Nome do produto para consultar estoque"
                        )
                    ),
                    "required", List.of("produtoNome")
                )
            )
        ));

        tools.add(Map.of(
            "type", "function",
            "function", Map.of(
                "name", "buscarProdutos",
                "description", "Busca produtos disponiveis na farmacia por nome, sintoma ou categoria",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "termo", Map.of(
                            "type", "string",
                            "description", "Termo de busca (nome, sintoma ou categoria)"
                        )
                    ),
                    "required", List.of("termo")
                )
            )
        ));

        tools.add(Map.of(
            "type", "function",
            "function", Map.of(
                "name", "historicoMedicamento",
                "description", "Historico de compras de um medicamento especifico pelo nome",
                "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "produtoNome", Map.of(
                            "type", "string",
                            "description", "Nome do medicamento"
                        )
                    ),
                    "required", List.of("produtoNome")
                )
            )
        ));

        return tools;
    }

    public String executeTool(String toolName, String argumentsJson, Long usuarioId) {
        try {
            return switch (toolName) {
                case "buscarPedidos" -> executeBuscarPedidos(usuarioId);
                case "buscarMedicamentos" -> executeBuscarMedicamentos(usuarioId);
                case "rastrearPedido" -> executeRastrearPedido(argumentsJson);
                case "consultarEstoque" -> executeConsultarEstoque(argumentsJson);
                case "buscarProdutos" -> executeBuscarProdutos(argumentsJson);
                case "historicoMedicamento" -> executeHistoricoMedicamento(usuarioId, argumentsJson);
                default -> "{\"erro\":\"Ferramenta desconhecida: " + toolName + "\"}";
            };
        } catch (Exception e) {
            log.error("Erro executando tool {}: {}", toolName, e.getMessage());
            return "{\"erro\":\"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    private String executeBuscarPedidos(Long usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioIdOrderByDataPedidoDesc(usuarioId);
        if (pedidos == null || pedidos.isEmpty()) {
            return "{\"mensagem\":\"Nenhum pedido encontrado.\"}";
        }
        List<Map<String, Object>> result = pedidos.stream().limit(10).map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("codigo", p.getCodigoPedido());
            m.put("data", p.getDataPedido() != null ? p.getDataPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null);
            m.put("status", p.getStatus() != null ? p.getStatus().name() : null);
            m.put("valorTotal", p.getValorTotal() != null ? String.format("R$%.2f", p.getValorTotal()) : null);
            return m;
        }).collect(Collectors.toList());
        return mapToJson(Map.of("pedidos", result, "total", pedidos.size()));
    }

    private String executeBuscarMedicamentos(Long usuarioId) {
        List<Pedido> pedidos = pedidoRepository.findPedidosComItensByUsuario(usuarioId);
        if (pedidos == null || pedidos.isEmpty()) {
            return "{\"mensagem\":\"Nenhum medicamento encontrado.\"}";
        }

        Map<String, Map<String, Object>> medMap = new LinkedHashMap<>();
        for (Pedido p : pedidos) {
            for (ItemPedido item : p.getItens()) {
                if (item.getProduto() == null) continue;
                String nome = item.getProduto().getNome();
                medMap.computeIfAbsent(nome, k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("nome", k);
                    m.put("categoria", item.getProduto().getCategoria() != null ? item.getProduto().getCategoria().getNome() : "Outros");
                    m.put("totalCompras", 0);
                    m.put("ultimaCompra", "");
                    return m;
                });
                Map<String, Object> m = medMap.get(nome);
                m.put("totalCompras", (int) m.get("totalCompras") + 1);
                if (p.getDataPedido() != null) {
                    String dataStr = p.getDataPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    m.put("ultimaCompra", dataStr);
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>(medMap.values());
        return mapToJson(Map.of("medicamentos", result, "total", result.size()));
    }

    private String executeRastrearPedido(String args) {
        try {
            Map<String, Object> params = mapper.readValue(args, Map.class);
            String codigo = (String) params.get("codigo");
            if (codigo == null) return "{\"erro\":\"Codigo do pedido nao informado.\"}";

            Pedido pedido = pedidoRepository.findByCodigoPedidoWithRastreio(codigo);
            if (pedido == null) {
                return "{\"mensagem\":\"Pedido " + codigo + " nao encontrado.\"}";
            }
            var r = pedido.getRastreio();
            if (r == null) {
                return "{\"mensagem\":\"Pedido " + codigo + " ainda nao possui codigo de rastreio.\"}";
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("codigoRastreio", r.getCodigoRastreio());
            data.put("transportadora", r.getTransportadora());
            data.put("status", r.getStatus());
            data.put("ultimaLocalizacao", r.getUltimaLocalizacao());
            data.put("dataPrevisao", r.getDataPrevisaoEntrega() != null ? r.getDataPrevisaoEntrega().toString() : null);
            return mapToJson(data);
        } catch (Exception e) {
            return "{\"erro\":\"Erro ao processar rastreio: " + e.getMessage() + "\"}";
        }
    }

    private String executeConsultarEstoque(String args) {
        try {
            Map<String, Object> params = mapper.readValue(args, Map.class);
            String nome = (String) params.get("produtoNome");
            if (nome == null) return "{\"erro\":\"Nome do produto nao informado.\"}";

            List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCase(nome);
            if (produtos.isEmpty()) {
                return "{\"mensagem\":\"Nenhum produto encontrado com o nome '" + nome + "'.\"}";
            }
            List<Map<String, Object>> result = produtos.stream().map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("nome", p.getNome());
                m.put("estoque", p.getEstoque());
                m.put("preco", p.getPreco() != null ? String.format("R$%.2f", p.getPreco()) : null);
                m.put("categoria", p.getCategoria() != null ? p.getCategoria().getNome() : null);
                return m;
            }).collect(Collectors.toList());
            return mapToJson(Map.of("produtos", result));
        } catch (Exception e) {
            return "{\"erro\":\"" + e.getMessage() + "\"}";
        }
    }

    @SuppressWarnings("unchecked")
    private String executeBuscarProdutos(String args) {
        try {
            Map<String, Object> params = mapper.readValue(args, Map.class);
            String termo = (String) params.get("termo");
            if (termo == null) return "{\"erro\":\"Termo de busca nao informado.\"}";

            List<Produto> produtos = produtoRepository.findByNomeContainingIgnoreCase(termo);
            if (produtos.isEmpty()) {
                return "{\"mensagem\":\"Nenhum produto encontrado para '" + termo + "'.\"}";
            }
            List<Map<String, Object>> result = produtos.stream().limit(10).map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", p.getId());
                m.put("nome", p.getNome());
                m.put("preco", p.getPreco() != null ? String.format("R$%.2f", p.getPreco()) : null);
                m.put("estoque", p.getEstoque());
                m.put("categoria", p.getCategoria() != null ? p.getCategoria().getNome() : null);
                return m;
            }).collect(Collectors.toList());
            return mapToJson(Map.of("produtos", result));
        } catch (Exception e) {
            return "{\"erro\":\"" + e.getMessage() + "\"}";
        }
    }

    private String executeHistoricoMedicamento(Long usuarioId, String args) {
        try {
            Map<String, Object> params = mapper.readValue(args, Map.class);
            String nome = (String) params.get("produtoNome");
            if (nome == null) return "{\"erro\":\"Nome do medicamento nao informado.\"}";

            List<Pedido> pedidos = pedidoRepository.findPedidosComItensByUsuario(usuarioId);
            List<Map<String, Object>> historico = new ArrayList<>();

            for (Pedido p : pedidos) {
                for (ItemPedido item : p.getItens()) {
                    if (item.getProduto() != null &&
                        item.getProduto().getNome().toLowerCase().contains(nome.toLowerCase())) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("data", p.getDataPedido() != null ? p.getDataPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null);
                        m.put("quantidade", item.getQuantidade());
                        m.put("precoUnitario", item.getPrecoUnitario() != null ? String.format("R$%.2f", item.getPrecoUnitario()) : null);
                        m.put("subtotal", item.getSubtotal() != null ? String.format("R$%.2f", item.getSubtotal()) : null);
                        m.put("pedido", p.getCodigoPedido());
                        historico.add(m);
                    }
                }
            }

            if (historico.isEmpty()) {
                return "{\"mensagem\":\"Nenhum historico de compra encontrado para " + nome + ".\"}";
            }
            return mapToJson(Map.of("medicamento", nome, "compras", historico, "total", historico.size()));
        } catch (Exception e) {
            return "{\"erro\":\"" + e.getMessage() + "\"}";
        }
    }

    private String mapToJson(Map<String, Object> map) {
        try {
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
