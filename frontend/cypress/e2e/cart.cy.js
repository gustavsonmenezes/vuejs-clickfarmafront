describe('Carrinho', () => {
  it('deve mostrar carrinho vazio', () => {
    cy.visit('/cart')
    cy.contains('Meu Carrinho').should('be.visible')
  })
})
