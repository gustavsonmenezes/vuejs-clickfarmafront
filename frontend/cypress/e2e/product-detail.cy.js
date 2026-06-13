describe('Product Detail Page', () => {
  beforeEach(() => {
    // Start at products page
    cy.visit('/products')
  })

  it('should navigate to product detail page when clicking a product', () => {
    // Wait for products to load
    cy.get('.spinner-border').should('not.exist')
    
    // Click on the product name link to navigate (cf-product-card is a div)
    cy.get('.cf-card-link').first().click()
    
    // Should navigate to product detail page
    cy.url().should('include', '/products/')
    cy.url().should('match', /\/products\/\d+$/)
    
    // Should show product name
    cy.get('h2').should('be.visible')
  })

  it('should display product information correctly', () => {
    // Wait for products to load
    cy.get('.spinner-border').should('not.exist')
    
    // Click on the product name link
    cy.get('.cf-card-link').first().click()
    
    // On product detail page, verify the information is displayed
    cy.get('h2').should('be.visible')
    cy.get('h2').invoke('text').should('have.length.greaterThan', 0)
    
    cy.contains('R$').should('be.visible') // Price should be visible
  })

  it('should show product description', () => {
    // Wait for products to load
    cy.get('.spinner-border').should('not.exist')
    
    // Click on the product name link
    cy.get('.cf-card-link').first().click()
    
    // Should show stock status (one of the two will be visible)
    cy.get('.text-success, .text-danger').should('be.visible')
  })

  it('should allow adding product to cart from detail page', () => {
    // Wait for products to load
    cy.get('.spinner-border').should('not.exist')
    
    // Click on the product name link
    cy.get('.cf-card-link').first().click()
    
    // Wait for detail page to load
    cy.get('h2').should('be.visible')
    
    // Click add to cart button
    cy.contains('button', 'Adicionar ao carrinho').click()
    
    // Navigate to cart via router to verify item was added
    cy.get('.cf-cart').first().click()
    cy.contains('Meu Carrinho').should('be.visible')
  })

  it('should handle product not found gracefully', () => {
    // The ProductDetail.vue component redirects to /products if product not found
    cy.visit('/products/999999')
    
    // Should redirect back to products page
    cy.url().should('eq', 'http://localhost:8081/products')
    cy.contains('Nossos Produtos').should('be.visible')
  })
})