<template>
  <div class="cf-catalog">

    <!-- ============================
         TOPO — FILTROS + BUSCA
         ============================ -->
    <div class="cf-catalog-top">
      <div class="cf-container">
        <div class="cf-catalog-header">
          <div>
            <h1 class="cf-catalog-title">Medicamentos e Produtos</h1>
            <p class="cf-catalog-sub">{{ filteredProducts.length }} produtos encontrados</p>
          </div>
        </div>

        <div class="cf-catalog-search">
          <div class="cf-catalog-search-box">
            <svg class="cf-cat-search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7"/><path d="m16.5 16.5 4 4"/>
            </svg>
            <input
              v-model="searchTerm"
              type="text"
              class="cf-cat-search-input"
              placeholder="Buscar por nome, sintoma ou princípio ativo..."
            />
          </div>
          <div class="cf-cat-filters">
            <select v-model="filters.category" class="cf-select">
              <option value="">Todas as categorias</option>
              <option v-for="cat in categoriesList" :key="cat" :value="cat">{{ cat }}</option>
            </select>
            <select v-model="filters.sortBy" class="cf-select">
              <option value="name">Nome A-Z</option>
              <option value="price">Menor preço</option>
              <option value="price_desc">Maior preço</option>
            </select>
            <button
              v-if="hasActiveFilters"
              class="cf-btn cf-btn-ghost cf-btn-sm"
              @click="clearAllFilters"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6 6 18M6 6l12 12"/>
              </svg>
              Limpar
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="cf-container">

      <!-- AI Banner -->
      <div v-if="aiSintoma" class="cf-ai-banner">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--cf-blue)" stroke-width="2">
          <path d="M12 2a4 4 0 0 1 4 4v2a4 4 0 0 1-8 0V6a4 4 0 0 1 4-4z"/>
          <path d="M18 10c0 3.3-2.7 6-6 6s-6-2.7-6-6"/>
          <path d="M12 16v4"/>
          <path d="M8 22h8"/>
        </svg>
        <span><strong>Busca inteligente:</strong> resultados para "{{ aiSintoma }}"</span>
        <button class="cf-btn cf-btn-ghost cf-btn-sm" @click="limparBuscaIA">Limpar</button>
      </div>

      <!-- Semantic Search Banner -->
      <div v-else-if="activeSintomaLabel" class="cf-ai-banner cf-semantic-banner">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--cf-teal)" stroke-width="2">
          <circle cx="11" cy="11" r="7"/><path d="m16.5 16.5 4 4"/>
          <path d="M11 7v8M7 11h8"/>
        </svg>
        <span><strong>Busca semântica:</strong> mostrando resultados para "{{ activeSintomaLabel }}"</span>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="cf-loading-state">
        <div class="cf-loading-grid">
          <div v-for="i in 8" :key="i" class="cf-skeleton" style="height:320px;border-radius:var(--cf-r-lg)"></div>
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="cf-empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--cf-danger)" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/>
        </svg>
        <h3>Ops! Algo deu errado</h3>
        <p>{{ error }}</p>
        <button class="cf-btn cf-btn-primary" @click="retryLoading">Tentar novamente</button>
      </div>

      <!-- Empty -->
      <div v-else-if="filteredProducts.length === 0" class="cf-empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--cf-gray-400)" stroke-width="1.5">
          <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
        </svg>
        <h3>Nenhum produto encontrado</h3>
        <p>Tente ajustar os filtros ou buscar por outro termo.</p>
        <button class="cf-btn cf-btn-outline" @click="clearAllFilters">Limpar filtros</button>
      </div>

      <!-- Grid de Produtos -->
      <div v-else class="cf-product-grid cf-stagger">
        <div
          v-for="product in filteredProducts"
          :key="product.id"
          class="cf-product-card"
        >
          <!-- Tarja (stripe) -->
          <div v-if="product.tarja" class="cf-product-stripe" :class="product.tarja === 'PRETA' ? 'stripe-black' : 'stripe-red'">
            <span>{{ product.tarja === 'PRETA' ? 'Tarja Preta' : 'Tarja Vermelha' }}</span>
            <svg v-if="product.tarja === 'PRETA'" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </div>

          <!-- Imagem / Ícone -->
          <div class="cf-product-visual">
            <img
              v-if="product.imagem"
              :src="product.imagem"
              :alt="product.nome"
              class="cf-product-img"
              loading="lazy"
            />
            <div v-else class="cf-product-fallback">
              <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="var(--cf-gray-300)" stroke-width="1.3">
                <rect x="4" y="2" width="16" height="20" rx="2"/><path d="M9 22v-4h6v4"/><path d="M8 12h8"/><path d="M12 8v8"/>
              </svg>
            </div>
            <span v-if="product.estoque <= 0" class="cf-out-stock-badge">Indisponível</span>
          </div>

          <!-- Info -->
          <div class="cf-product-body">
            <span class="cf-product-cat">{{ product.categoriaNome || product.categoria }}</span>
            <h3 class="cf-product-name">{{ product.nome }}</h3>
            <span class="cf-product-active">{{ product.principioAtivo }}</span>

            <div class="cf-product-meta">
              <span class="cf-product-dosage">{{ product.dosagem }}</span>
              <span class="cf-product-brand" v-if="product.fabricante">{{ product.fabricante }}</span>
            </div>

            <div class="cf-product-foot">
              <div class="cf-product-pricing">
                <span class="cf-product-price">R$ {{ formatPrice(product.preco) }}</span>
                <span v-if="product.precoOriginal > product.preco" class="cf-product-old-price">
                  R$ {{ formatPrice(product.precoOriginal) }}
                </span>
              </div>
              <button
                class="cf-btn cf-add-btn"
                :class="{ 'added': addedItems[product.id] }"
                :disabled="product.estoque <= 0"
                @click="addToCart(product)"
              >
                <svg v-if="addedItems[product.id]" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 5v14m7-7H5"/>
                </svg>
                {{ addedItems[product.id] ? 'Adicionado' : 'Adicionar' }}
              </button>
            </div>
          </div>

          <!-- Exigência de receita -->
          <div v-if="product.exigeReceita" class="cf-rx-warning">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4"/><path d="M12 16h.01"/>
            </svg>
            <span>Venda sob prescrição médica</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'

const SEMANTIC_MAP = {
  'dor de cabeça':        { keywords: ['enxaqueca', 'cefaleia', 'analgésico', 'dipirona', 'paracetamol', 'ibuprofeno', 'cafeína'], cats: ['Analgésicos'] },
  'dor':                  { keywords: ['analgésico', 'anti-inflamatório', 'dipirona', 'paracetamol', 'ibuprofeno', 'nimesulida', 'cetoprofeno', 'tramadol', 'codeína', 'dor muscular', 'cólica'], cats: ['Analgésicos'] },
  'febre':                { keywords: ['antitérmico', 'dipirona', 'paracetamol', 'ibuprofeno', 'nimesulida'], cats: ['Antitérmicos', 'Analgésicos'] },
  'gripe':                { keywords: ['resfriado', 'antigripal', 'congestão', 'coriza', 'tosse', 'espirro', 'adinil', 'benegripe'], cats: ['Gripe e Resfriado', 'Respiratório'] },
  'resfriado':            { keywords: ['gripe', 'antigripal', 'coriza', 'adinil', 'benegripe'], cats: ['Gripe e Resfriado'] },
  'tosse':                { keywords: ['xarope', 'expectorante', 'antitussígeno', 'bromexina', 'acetilcisteína', 'vick'], cats: ['Respiratório'] },
  'alergia':              { keywords: ['antialérgico', 'anti-histamínico', 'loratadina', 'cetirizina', 'dexclorfeniramina', 'allegra', 'polaramine'], cats: ['Alergia', 'Respiratório'] },
  'pressão':              { keywords: ['anti-hipertensivo', 'cardiovascular', 'captopril', 'enalapril', 'losartana', 'hidroclorotiazida'], cats: ['Cardiovascular'] },
  'diabetes':             { keywords: ['diabético', 'glicemia', 'insulina', 'metformina', 'glibenclamida', 'diamicron'], cats: ['Diabetes'] },
  'estômago':             { keywords: ['azia', 'queimação', 'gastrite', 'refluxo', 'omeprazol', 'pantoprazol', 'antiácido', 'ranitidina', 'cimetidina', 'pepcid'], cats: ['Gastrointestinal'] },
  'intestino':            { keywords: ['prisão de ventre', 'constipação', 'laxante', 'diarreia', 'lactulona', 'bisolax', 'loperamida'], cats: ['Gastrointestinal'] },
  'colesterol':           { keywords: ['gordura', 'estatina', 'sinvastatina', 'atorvastatina', 'rosuvastatina'], cats: ['Cardiovascular'] },
  'antibiótico':          { keywords: ['bactericida', 'amoxicilina', 'azitromicina', 'cefalexina', 'sulfa', 'bactrim', 'keflex'], cats: ['Antibióticos'] },
  'infecção':             { keywords: ['antibiótico', 'anti-inflamatório', 'amoxicilina', 'azitromicina'], cats: ['Antibióticos'] },
  'dormir':               { keywords: ['insônia', 'sono', 'calmante', 'ansiolítico', 'melatonina', 'passiflora', 'valeriana', 'maracugina'], cats: ['Bem-Estar', 'Fitoterápicos'] },
  'ansiedade':            { keywords: ['calmante', 'ansiolítico', 'nervosismo', 'estresse', 'fluoxetina', 'sertralina', 'diazepam', 'clonazepam', 'rivotril'], cats: ['Bem-Estar'] },
  'pele':                 { keywords: ['dermatológico', 'pomada', 'corticóide', 'cicatrização', 'betametasona', 'nebacetin', 'hipoglós'], cats: ['Dermocosméticos'] },
  'vitamina':             { keywords: ['suplemento', 'multivitamínico', 'vitamina c', 'vitamina d', 'complexo b', 'ferro', 'cálcio', 'magnésio', 'centrum', 'lavitan'], cats: ['Vitaminas', 'Suplementos'] },
  'criança':              { keywords: ['infantil', 'pediátrico', 'bebê', 'nistatina', 'paracetamol infantil', 'ibuprofeno infantil'], cats: ['Infantil'] },
  'bebê':                 { keywords: ['infantil', 'criança', 'fralda', 'nan', 'aptamil'], cats: ['Infantil'] },
  'higiene':              { keywords: ['bucal', 'sabonete', 'antisséptico', 'álcool', 'máscara', 'higiene pessoal'], cats: ['Higiene'] },
  'músculo':              { keywords: ['dor muscular', 'anti-inflamatório', 'pomada', 'relaxante', 'cataflan', 'gelol', 'calminex'], cats: ['Analgésicos'] },
  'cólica':               { keywords: ['menstrual', 'dismenorreia', 'analgésico', 'anti-inflamatório', 'buscopan', 'ibupirac'], cats: ['Analgésicos'] },
  'enjoo':                { keywords: ['náusea', 'vômito', 'tontura', 'labirintite', 'dramin', 'plasil', 'bromoprida', 'vonau'], cats: ['Gastrointestinal'] },
  'cicatrização':         { keywords: ['ferida', 'corte', 'queimadura', 'pomada', 'nebacetin', 'hipoglós', 'esparadrapo', 'gaze'], cats: ['Dermocosméticos', 'Primeiros Socorros'] },
  'contraceptivo':        { keywords: ['anticoncepcional', 'pílula', 'ciclo', 'selene', 'iane', 'elani', 'mirelle', 'diane'], cats: ['Ginecológicos'] },
  'emagrecer':            { keywords: ['dieta', 'emagrecimento', 'termogênico', 'sibutramina', 'orlistat', 'saccharomyces'], cats: ['Bem-Estar', 'Suplementos'] },
  'coração':              { keywords: ['cardíaco', 'cardiovascular', 'coração', 'aspirina', 'aas', 'clopidogrel'], cats: ['Cardiovascular'] },
  'depressão':            { keywords: ['antidepressivo', 'fluoxetina', 'sertralina', 'escitalopram', 'paroxetina', 'venlafaxina'], cats: ['Bem-Estar'] },
  'olho':                 { keywords: ['ocular', 'colírio', 'conjuntivite', 'lacrimejamento', 'lágrima'], cats: ['Oftalmológicos'] },
}

const CATEGORY_ALIASES = {
  medicamentos: 'Medicamentos',
  cosméticos: 'Dermocosméticos',
  derma: 'Dermocosméticos',
  'bem estar': 'Bem-Estar',
  suplemento: 'Suplementos',
  baby: 'Infantil',
  cardio: 'Cardiovascular',
  gastro: 'Gastrointestinal',
  alergia: 'Alergia',
  respiratório: 'Respiratório',
  analgésico: 'Analgésicos',
}

function findSemanticMatch(term) {
  const t = term.toLowerCase().trim()
  for (const [sintoma, data] of Object.entries(SEMANTIC_MAP)) {
    if (t.includes(sintoma) || sintoma.includes(t)) {
      return { sintoma, ...data }
    }
  }
  if (CATEGORY_ALIASES[t]) {
    return { sintoma: t, keywords: [], cats: [CATEGORY_ALIASES[t]] }
  }
  return null
}

export default {
  name: 'Products',
  data() {
    return {
      searchTerm: '',
      filters: { category: '', sortBy: 'name' },
      loading: false,
      error: null,
      aiSintoma: '',
      aiProducts: null,
      addedItems: {},
      _semanticCandidates: null
    }
  },
  computed: {
    ...mapState(['products']),
    semanticMatch() {
      if (!this.searchTerm || this.aiSintoma) return null
      return findSemanticMatch(this.searchTerm)
    },
    activeSintomaLabel() {
      if (this.aiSintoma) return this.aiSintoma
      if (this.semanticMatch) return this.semanticMatch.sintoma
      return null
    },
    categoriesList() {
      if (!this.products) return []
      return [...new Set(this.products.map(p => p.categoriaNome || p.categoria).filter(Boolean))].sort()
    },
    filteredProducts() {
      let base = this.products
      if (!base || !Array.isArray(base)) return []

      if (this.aiSintoma && this.aiProducts) {
        let r = this.aiProducts
        if (this.filters.category) {
          r = r.filter(p => (p.categoriaNome || p.categoria) === this.filters.category)
        }
        return this._sortProducts(r)
      }

      const term = (this.searchTerm || '').trim().toLowerCase()
      if (!term) {
        let r = base
        if (this.filters.category) {
          r = r.filter(p => (p.categoriaNome || p.categoria) === this.filters.category)
        }
        return this._sortProducts(r.map(p => ({ ...p, _score: 0 })))
      }

      const semantic = this.semanticMatch
      let results = []

      for (const p of base) {
        const nome = (p.nome || '').toLowerCase()
        const ativo = (p.principioAtivo || '').toLowerCase()
        const desc = (p.descricao || '').toLowerCase()
        const marca = (p.fabricante || '').toLowerCase()
        const cat = (p.categoriaNome || p.categoria || '').toLowerCase()
        const catFilter = !this.filters.category || cat === this.filters.category.toLowerCase()

        const literalMatch = nome.includes(term) || ativo.includes(term) || desc.includes(term) || marca.includes(term)

        if (literalMatch && catFilter) {
          let s = 0
          if (nome.includes(term)) s += 100
          if (ativo.includes(term)) s += 80
          if (desc.includes(term)) s += 40
          if (marca.includes(term)) s += 20
          if (this.filters.sortBy === 'price' || this.filters.sortBy === 'price_desc') s = 0
          results.push({ ...p, _score: 999 + s })
          continue
        }

        if (semantic && catFilter) {
          const allText = `${nome} ${ativo} ${desc} ${marca}`
          let s = 0
          for (const kw of semantic.keywords) {
            if (allText.includes(kw)) s += 30
            if (ativo.includes(kw)) s += 20
          }
          for (const sc of semantic.cats) {
            if (cat === sc.toLowerCase()) s += 25
          }
          if (s > 0) {
            results.push({ ...p, _score: s })
          }
        }
      }

      this._semanticCandidates = results
      return this._sortProducts(results)
    },
    hasActiveFilters() {
      return this.searchTerm || this.filters.category || this.aiSintoma
    }
  },
  async mounted() {
    await this.initializeComponent()
  },
  methods: {
    ...mapActions(['fetchProducts']),
    formatPrice(v) { return (v || 0).toFixed(2).replace('.', ',') },
    _sortProducts(list) {
      const sort = this.filters.sortBy || 'name'
      return [...list].sort((a, b) => {
        const scoreDiff = (b._score || 0) - (a._score || 0)
        if (scoreDiff !== 0) return scoreDiff
        const na = a.nome || '', nb = b.nome || ''
        const pa = a.preco || 0, pb = b.preco || 0
        if (sort === 'price') return pa - pb
        if (sort === 'price_desc') return pb - pa
        return na.localeCompare(nb)
      })
    },
    async initializeComponent() {
      this.loading = true
      this.error = null
      try {
        await this.fetchProducts()
      } catch {
        this.error = 'Erro ao carregar produtos.'
      } finally {
        this.loading = false
      }
    },
    retryLoading() { this.initializeComponent() },
    clearAllFilters() {
      this.searchTerm = ''
      this.filters = { category: '', sortBy: 'name' }
      this.aiSintoma = ''
      this.aiProducts = null
    },
    limparBuscaIA() {
      this.aiSintoma = ''
      this.aiProducts = null
    },
    addToCart(product) {
      this.$store.dispatch('addToCart', product)
      this.addedItems = { ...this.addedItems, [product.id]: true }
      setTimeout(() => {
        this.addedItems = { ...this.addedItems, [product.id]: false }
      }, 2000)
    }
  }
}
</script>

<style scoped>
/* ============================
   TOPO
   ============================ */
.cf-catalog-top {
  background: var(--cf-white);
  border-bottom: 1px solid var(--cf-gray-200);
  padding: 1.5rem 0 1rem;
  position: sticky;
  top: 60px;
  z-index: 50;
}
.cf-catalog-header { margin-bottom: 1rem; }
.cf-catalog-title {
  font-family: var(--cf-heading);
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--cf-gray-900);
}
.cf-catalog-sub {
  font-size: 0.85rem;
  color: var(--cf-gray-500);
}

.cf-catalog-search {
  display: flex;
  gap: 12px;
  align-items: center;
}
.cf-catalog-search-box {
  flex: 1;
  position: relative;
}
.cf-cat-search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--cf-gray-400);
  pointer-events: none;
}
.cf-cat-search-input {
  width: 100%;
  height: 44px;
  padding: 0 1rem 0 2.75rem;
  border: 1.5px solid var(--cf-gray-200);
  border-radius: var(--cf-r-md);
  font-family: var(--cf-body);
  font-size: 0.9rem;
  color: var(--cf-gray-900);
  background: var(--cf-white);
  transition: all var(--cf-fast);
}
.cf-cat-search-input:focus {
  outline: none;
  border-color: var(--cf-teal);
  box-shadow: 0 0 0 3px rgba(13,148,136,0.1);
}
.cf-cat-search-input::placeholder { color: var(--cf-gray-400); }
.cf-cat-filters {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* ============================
   AI BANNER
   ============================ */
.cf-ai-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0.75rem 1rem;
  background: var(--cf-blue-xlight);
  border-radius: var(--cf-r-md);
  font-size: 0.85rem;
  color: var(--cf-blue-dark);
  margin: 1rem 0;
  border-left: 3px solid var(--cf-blue);
}
.cf-semantic-banner {
  background: #F0FDFA;
  color: #0D9488;
  border-left-color: var(--cf-teal);
}

/* ============================
   ESTADOS
   ============================ */
.cf-loading-state { padding: 2rem 0; }
.cf-loading-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.cf-empty-state {
  text-align: center;
  padding: 4rem 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.cf-empty-state h3 {
  font-family: var(--cf-heading);
  font-size: 1.2rem;
  color: var(--cf-gray-700);
}
.cf-empty-state p { font-size: 0.9rem; color: var(--cf-gray-500); margin: 0; }

/* ============================
   GRID DE PRODUTOS
   ============================ */
.cf-product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 1.5rem 0 3rem;
}
.cf-product-card {
  border: 1px solid var(--cf-gray-200);
  border-radius: var(--cf-r-lg);
  background: var(--cf-white);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all var(--cf-base);
  position: relative;
}
.cf-product-card:hover {
  box-shadow: var(--cf-shadow-md);
  transform: translateY(-3px);
  border-color: var(--cf-teal-light);
}
.cf-product-card:hover .cf-add-btn:not(:disabled) {
  background: var(--cf-teal);
  color: white;
}

/* Stripe */
.cf-product-stripe {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 0.25rem;
  font-size: 0.55rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  z-index: 2;
}
.stripe-black {
  background: var(--cf-stripe-black);
  color: white;
}
.stripe-red {
  background: var(--cf-stripe-red);
  color: white;
}

/* Visual */
.cf-product-visual {
  position: relative;
  background: var(--cf-teal-xlight);
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.cf-product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 350ms var(--cf-ease);
}
.cf-product-card:hover .cf-product-img { transform: scale(1.06); }
.cf-product-fallback { opacity: 0.4; }
.cf-out-stock-badge {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: var(--cf-gray-800);
  color: white;
  font-size: 0.62rem;
  font-weight: 600;
  padding: 0.2rem 0.6rem;
  border-radius: 3px;
}

/* Body */
.cf-product-body {
  padding: 0.75rem 0.9rem 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}
.cf-product-cat {
  font-size: 0.62rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--cf-blue);
}
.cf-product-name {
  font-family: var(--cf-heading);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--cf-gray-900);
  line-height: 1.25;
}
.cf-product-active {
  font-size: 0.75rem;
  font-style: italic;
  color: var(--cf-gray-500);
}
.cf-product-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 2px 0;
}
.cf-product-dosage {
  font-size: 0.72rem;
  color: var(--cf-gray-600);
  font-weight: 500;
}
.cf-product-brand {
  font-size: 0.7rem;
  color: var(--cf-gray-400);
}

/* Footer */
.cf-product-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 0.65rem;
  border-top: 1px solid var(--cf-gray-100);
}
.cf-product-pricing {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.cf-product-price {
  font-family: var(--cf-heading);
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--cf-gray-900);
}
.cf-product-old-price {
  font-size: 0.68rem;
  color: var(--cf-gray-400);
  text-decoration: line-through;
}
.cf-add-btn {
  background: transparent;
  color: var(--cf-teal);
  border: 1.5px solid var(--cf-teal);
  font-size: 0.75rem;
  padding: 0.4rem 0.8rem;
  min-height: 36px;
}
.cf-add-btn:hover:not(:disabled) {
  background: var(--cf-teal);
  color: white;
}
.cf-add-btn:disabled {
  border-color: var(--cf-gray-200);
  color: var(--cf-gray-400);
  background: transparent;
}
.cf-add-btn.added {
  background: var(--cf-teal);
  color: white;
  border-color: var(--cf-teal);
}

/* RX Warning */
.cf-rx-warning {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 0.35rem 0.75rem;
  background: #FFFBEB;
  color: #B45309;
  font-size: 0.62rem;
  font-weight: 500;
  border-top: 1px solid #FDE68A;
}

/* ============================
   RESPONSIVO
   ============================ */
@media (max-width: 1024px) {
  .cf-product-grid { grid-template-columns: repeat(3, 1fr); }
  .cf-loading-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .cf-catalog-search { flex-direction: column; }
  .cf-cat-filters { width: 100%; }
  .cf-product-grid { grid-template-columns: repeat(2, 1fr); }
  .cf-loading-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .cf-product-grid { grid-template-columns: 1fr; }
}
</style>
