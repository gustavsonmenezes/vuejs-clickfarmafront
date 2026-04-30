// Usa o proxy do vue.config.js em dev e o mesmo origin em produção
const API_URL = '/api/receita';

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
                const error = await response.json();
                throw new Error(error.erro || 'Erro ao processar receita');
            }

            const data = await response.json();
            
            // Garantir que os medicamentos têm todos os campos necessários
            if (data.medicamentos && Array.isArray(data.medicamentos)) {
                data.medicamentos = data.medicamentos.map(med => ({
                    nome: med.nome || '',
                    nomeCompleto: med.nomeCompleto || med.nome || '',
                    quantidade: med.quantidade || 1,
                    dosagem: med.dosagem || '',
                    preco: med.preco || 0,
                    descricaoProduto: med.descricaoProduto || med.descricaoIA || '',
                    descricaoIA: med.descricaoIA || '',
                    produtoId: med.produtoId || null,
                    estoque: med.estoque !== undefined ? med.estoque : -1
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
