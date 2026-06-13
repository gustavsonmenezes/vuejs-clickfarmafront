describe('Carrinho de Compras', () => {
  beforeEach(() => {
    // Visit products page first to add items
    cy.visit('/products')
  })

  it('deve mostrar carrinho vazio inicialmente', () => {
    cy.visit('/cart')
    cy.contains('Meu Carrinho').should('be.visible')
    cy.contains('Seu carrinho está vazio').should('be.visible')
  })

  it('deve adicionar produto ao carrinho', () => {
    // Wait for products to load
    cy.get('.spinner-border').should('not.exist')
    
    // Click on first product's add to cart button
    cy.get('.cf-add-btn', { timeout: 10000 }).first().click()
    
    // Wait for confirmation (the button text changes to "OK!")
    cy.get('.cf-add-btn').first().should('contain', 'OK!')
    
    // Wait a moment for cart to update
    cy.wait(1000)
    
    // Navigate to cart via router (preserves Vuex state)
    cy.get('.cf-cart').first().click()
    
    // Verify product is in cart
    cy.contains('Meu Carrinho').should('be.visible')
    cy.get('.cart-item', { timeout: 10000 }).should('have.length.greaterThan', 0)
    cy.get('.cart-item').first().within(() => {
      cy.get('.item-name').should('be.visible')
      cy.get('.item-total').should('be.visible')
      cy.get('.quantity-control input').should('be.visible')
    })
  })

  it('deve remover produto do carrinho', () => {
    // First add a product
    cy.get('.spinner-border').should('not.exist')
    cy.get('.cf-add-btn').first().click()
    // Wait for confirmation (the button text changes to "OK!")
    cy.get('.cf-add-btn').first().should('contain', 'OK!')
    
    // Wait a moment for cart to update
    cy.wait(1000)
    
    // Navigate to cart via router
    cy.get('.cf-cart').first().click()
    
    // Verify product is in cart
    cy.get('.cart-item', { timeout: 10000 }).should('have.length.greaterThan', 0)
    
    // Click remove button (❌ Remover)
    cy.get('.btn-outline-danger').first().click()
    
    // Wait for removal to complete
    cy.wait(1000)
    
    // Verify cart is empty again
    cy.contains('Seu carrinho está vazio').should('be.visible')
  })

  it('deve atualizar quantidade de produto no carrinho', () => {
    // First add a product
    cy.get('.spinner-border').should('not.exist')
    cy.get('.cf-add-btn').first().click()
    // Wait for confirmation (the button text changes to "OK!")
    cy.get('.cf-add-btn').first().should('contain', 'OK!')
    
    // Wait a moment for cart to update
    cy.wait(1000)
    
    // Navigate to cart via router
    cy.get('.cf-cart').first().click()
    
    // Increase quantity using the input field
    cy.get('.quantity-control input').first().type('{selectall}3')
    
    // Wait for quantity update
    cy.wait(500)
    
    // Verify quantity updated
    cy.get('.quantity-control input').first().should('have.value', '3')
    
    // Decrease quantity
    cy.get('.quantity-control input').first().type('{selectall}1')
    
    // Wait for quantity update
    cy.wait(500)
    
    // Verify quantity updated
    cy.get('.quantity-control input').first().should('have.value', '1')
  })
})