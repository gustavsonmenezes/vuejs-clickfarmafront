// Configuração da URL da API
// Para desenvolvimento local: http://localhost:8080/api/receita
// Para produção: /api/receita (proxy do servidor)

// Vue CLI normalmente usa process.env.VUE_APP_*
// Vite usa import.meta.env.VITE_*
// Importante: o default deve ser relativo (/api/receita) para funcionar com:
// - proxy do vue.config.js no dev
// - proxy do nginx no Docker
// - acesso por IP/domínio (evita "localhost" apontar para a máquina do navegador)
const getApiUrl = () => {
    try {
        // Tenta usar a variável de ambiente do Vite
        if (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_API_URL) {
            return import.meta.env.VITE_API_URL;
        }
        // Tenta usar a variável de ambiente do Webpack/Vue CLI (caso mude de bundler)
        if (typeof process !== 'undefined' && process.env && process.env.VUE_APP_API_URL) {
            return process.env.VUE_APP_API_URL;
        }
    } catch (e) {
        console.warn('Aviso: Não foi possível ler as variáveis de ambiente.', e);
    }
    // Fallback padrão (funciona com proxy /api)
    return '/api/receita';
};

const normalizeBaseUrl = (url) => String(url || '').replace(/\/+$/, '');
const API_URL = normalizeBaseUrl(getApiUrl());

class ReceitaService {
    /**
     * Processa uma receita (imagem) e extrai os medicamentos usando IA
     * @param {string} imagemBase64 - Imagem em formato Base64
     * @param {string} nomeArquivo - Nome do arquivo da imagem
     * @returns {Promise<Object>} Resposta com medicamentos identificados
     */
    async processarReceita(imagemBase64, nomeArquivo) {
        try {
            const token = localStorage.getItem('token');

            const response = await fetch(`${API_URL}/processar`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(token && { 'Authorization': `Bearer ${token}` })
                },
                body: JSON.stringify({ imagemBase64, nomeArquivo })
            });

            if (!response.ok) {
                const contentType = response.headers.get('content-type') || '';
                let payload = null;
                if (contentType.includes('application/json')) {
                    payload = await response.json().catch(() => null);
                } else {
                    payload = await response.text().catch(() => null);
                }
                const msg =
                    (payload && typeof payload === 'object' && (payload.erro || payload.message)) ||
                    (typeof payload === 'string' && payload.trim()) ||
                    `Erro ao processar receita (HTTP ${response.status})`;
                throw new Error(msg);
            }

            const data = await response.json();

            if (data.mensagemOrientacao === undefined) {
                data.mensagemOrientacao = null;
            }

            // Garantir que os medicamentos têm todos os campos necessários
            if (data.medicamentos && Array.isArray(data.medicamentos)) {
                data.medicamentos = data.medicamentos.map(med => ({
                    nome: med.nome || '',
                    nomeCompleto: med.nomeCompleto || med.nome || '',
                    quantidade: med.quantidade || 1,
                    dosagem: med.dosagem || '',
                    preco: med.preco ?? 0,
                    descricaoProduto: med.descricaoProduto || med.descricaoIA || '',
                    descricaoIA: med.descricaoIA || '',
                    produtoId: med.produtoId ?? null,
                    estoque: med.estoque !== undefined && med.estoque !== null ? med.estoque : null,
                    situacaoCatalogo: med.situacaoCatalogo || null,
                    posologia: med.posologia || '',
                    diasDuracao: med.diasDuracao,
                    alternativasSugeridas: Array.isArray(med.alternativasSugeridas)
                        ? med.alternativasSugeridas
                        : []
                }));
            }

            return data;
        } catch (error) {
            console.error('Erro ao processar receita:', error);
            throw error;
        }
    }

    /**
     * Verifica a saúde do serviço de receita
     * @returns {Promise<Object>} Status do serviço
     */
    async healthCheck() {
        try {
            const response = await fetch(`${API_URL}/health`);
            if (!response.ok) {
                throw new Error('Serviço indisponível');
            }
            return response.json();
        } catch (error) {
            console.error('Erro ao verificar saúde do serviço:', error);
            throw error;
        }
    }

    /**
     * Busca produtos no catálogo por nome
     * @param {string} termo - Termo de busca
     * @returns {Promise<Array>} Lista de produtos encontrados
     */
    async buscarProdutos(termo) {
        try {
            const response = await fetch(`/api/produtos/buscar?termo=${encodeURIComponent(termo)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Erro ao buscar produtos');
            }

            return response.json();
        } catch (error) {
            console.error('Erro ao buscar produtos:', error);
            return [];
        }
    }

    /**
     * Obtém detalhes de um produto específico
     * @param {number} produtoId - ID do produto
     * @returns {Promise<Object>} Detalhes do produto
     */
    async obterProduto(produtoId) {
        try {
            const response = await fetch(`/api/produtos/${produtoId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Erro ao obter produto');
            }

            return response.json();
        } catch (error) {
            console.error('Erro ao obter produto:', error);
            return null;
        }
    }

    /**
     * Valida se um medicamento está disponível no catálogo
     * @param {string} nomeMedicamento - Nome do medicamento
     * @returns {Promise<Object>} Dados do medicamento se encontrado
     */
    async validarDisponibilidade(nomeMedicamento) {
        try {
            const produtos = await this.buscarProdutos(nomeMedicamento);
            return produtos.length > 0 ? produtos[0] : null;
        } catch (error) {
            console.error('Erro ao validar disponibilidade:', error);
            return null;
        }
    }
}

export default new ReceitaService();
