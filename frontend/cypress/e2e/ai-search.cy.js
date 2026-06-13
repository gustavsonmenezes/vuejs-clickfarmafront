describe('AI-Powered Search', () => {
  beforeEach(() => {
    // Start at products page where AI search is available
    cy.visit('/products')
  })

  it('should show AI search button and input', () => {
    cy.contains('Buscar com IA').should('be.visible')
    cy.get('input[placeholder*="O que você procura"]').should('be.visible')
  })

  it('should disable AI search when input is empty', () => {
    cy.get('.btn-ai').should('be.disabled')
    cy.get('input[placeholder*="O que você procura"]').type('dor de cabeça')
    cy.get('.btn-ai').should('not.be.disabled')
    cy.get('input[placeholder*="O que você procura"]').clear()
    cy.get('.btn-ai').should('be.disabled')
  })

  it('should show loading state during AI search', () => {
    cy.get('.spinner-border').should('not.exist') // Initial state
    
    cy.get('input[placeholder*="O que você procura"]').type('dor de cabeça')
    cy.get('.btn-ai').click()
    
    // Should show loading spinner in button - wait for aiLoading to be true
    cy.get('.btn-ai .spinner-border-sm').should('exist')
    cy.get('.btn-ai').should('contain', 'Pensando...')
  })

  it('should display AI search results when successful', () => {
    // Mock the AI search response by intercepting the API call
    cy.intercept('POST', '/api/produtos/busca-inteligente', {
      statusCode: 200,
      body: {
        produtos: [
          { 
            id: 1, 
            nome: 'Dorflex 500mg', 
            preco: 15.90, 
            categoriaNome: 'Medicamentos',
            descricao: 'Relaxante muscular',
            estoque: 20
          },
          { 
            id: 2, 
            nome: 'Cataflan 25mg', 
            preco: 22.50, 
            categoriaNome: 'Medicamentos',
            descricao: 'Anti-inflamatório',
            estoque: 15
          }
        ]
      }
    }).as('aiSearch')

    cy.get('input[placeholder*="O que você procura"]').type('dor nas costas')
    cy.get('.btn-ai').click()
    
    // Wait for the API call
    cy.wait('@aiSearch')
    
    // Should hide loading state
    cy.get('.btn-ai .spinner-border-sm').should('not.exist')
    cy.get('.btn-ai').should('contain', 'Buscar com IA')
    
    // Should show AI banner
    cy.contains('Busca Inteligente:').should('be.visible')
    cy.contains('dor nas costas').should('be.visible')
    
    // Wait for AI products to render (replace the regular product cards)
    cy.get('.cf-product-card', { timeout: 10000 }).should('have.length', 2)
    cy.contains('.cf-product-card', 'Dorflex 500mg').should('be.visible')
    cy.contains('.cf-product-card', 'Cataflan 25mg').should('be.visible')
    
    // Should show "Limpar" button to return to catalog
    cy.contains('Voltar para catálogo').should('be.visible')
  })

  it('should handle AI search with no results', () => {
    cy.intercept('POST', '/api/produtos/busca-inteligente', {
      statusCode: 200,
      body: {
        produtos: []
      }
    }).as('aiSearchEmpty')

    cy.get('input[placeholder*="O que você procura"]').type('sintoma inexistente')
    cy.get('.btn-ai').click()
    cy.wait('@aiSearchEmpty')
    
    // Should show error message
    cy.contains('A IA não encontrou produtos para').should('be.visible')
    cy.contains('sintoma inexistente').should('be.visible')
    
    // Should still show "Voltar para catálogo" button
    cy.contains('Voltar para catálogo').should('be.visible')
  })

  it('should clear AI search and return to regular catalog', () => {
    // First perform a search
    cy.intercept('POST', '/api/produtos/busca-inteligente', {
      statusCode: 200,
      body: {
        produtos: [{ id: 1, nome: 'Test Product', preco: 10, categoriaNome: 'Medicamentos', descricao: 'Test', estoque: 5 }]
      }
    }).as('aiSearch')

    cy.get('input[placeholder*="O que você procura"]').type('qualquer sintoma')
    cy.get('.btn-ai').click()
    cy.wait('@aiSearch')
    
    // Verify AI search results are shown
    cy.contains('Busca Inteligente:').should('be.visible')
    cy.get('.cf-product-card').should('have.length', 1)
    
    // Clear the AI search
    cy.contains('Voltar para catálogo').click()
    
    // Should return to regular catalog view
    cy.contains('Busca Inteligente:').should('not.exist')
    cy.contains('Nossos Produtos').should('be.visible')
    // Note: We can't guarantee products will show without mocking the regular API too
  })
})