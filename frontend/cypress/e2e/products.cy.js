describe('Product Listing', () => {
  beforeEach(() => {
    cy.visit('/products')
  })

  it('should load product listing page', () => {
    cy.contains('Nossos Produtos').should('be.visible')
    cy.contains('produtos encontrados').should('exist')
  })

  it('should display product cards', () => {
    // Wait for products to load (loading state disappears)
    cy.get('.spinner-border').should('not.exist')
    // Check for product cards using the actual class from ProductCard.vue
    cy.get('.cf-product-card').should('have.length.greaterThan', 0)
    cy.get('.cf-product-card').first().within(() => {
      cy.get('.cf-product-name').should('be.visible')
      cy.get('.cf-price').should('be.visible')
      cy.get('.cf-product-icon').should('be.visible')
    })
  })

  it('should show loading state when fetching products', () => {
    // Check if loading spinner appears initially
    cy.get('.spinner-border').should('exist')
    // Then wait for it to disappear (products loaded)
    cy.get('.spinner-border').should('not.exist')
  })
})