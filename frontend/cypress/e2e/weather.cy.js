describe('Weather Integration', () => {
  beforeEach(() => {
    cy.intercept('GET', '/api/weather*', {
      statusCode: 200,
      body: {
        temp: 28.5,
        condition: 'Clear',
        conditionDescription: 'Céu limpo',
        city: 'Recife',
        recommendedProducts: [
          { id: 5, nome: 'Protetor Solar FPS 50', preco: 32.90, categoriaNome: 'Cosméticos', descricao: 'Protetor solar facial', estoque: 10 }
        ]
      }
    }).as('weatherApi')
    cy.visit('/')
  })

  it('should show weather section on home page', () => {
    cy.contains('Recomendações para Hoje').should('be.visible')
    cy.get('.weather-recommendations').should('exist')
  })

  it('should show weather data after loading', () => {
    cy.get('.weather-recommendations').should('exist')
    cy.get('.weather-recommendations')
      .find('.weather-badge', { timeout: 10000 })
      .should('be.visible')
    cy.get('.weather-recommendations').should('contain', 'Recife')
  })

  it('should display weather information when available', () => {
    cy.get('.weather-recommendations').should('exist')
    cy.get('.weather-recommendations')
      .find('.container')
      .should('exist')
    cy.get('.weather-recommendations')
      .find('.section-title')
      .should('contain', 'Recomendações para Hoje')
  })

  it('should show weather-based product recommendations when data is available', () => {
    cy.get('.weather-recommendations').should('exist')
    cy.get('.weather-product-card', { timeout: 10000 }).should('have.length', 1)
    cy.get('.weather-product-card').first().within(() => {
      cy.get('.product-icon').should('exist')
      cy.get('.product-name').should('be.visible')
      cy.get('.product-price').should('be.visible')
      cy.get('.add-btn').should('be.visible')
    })
  })

  it('should show section structure regardless of network state', () => {
    // Weather action always falls back to mock data, so structure is consistent
    cy.get('.weather-recommendations').should('exist')
    cy.get('.weather-recommendations').find('.section-title').should('contain', 'Recomendações para Hoje')
    cy.get('.container', { timeout: 10000 }).should('exist')
  })
})