describe('Página Inicial', () => {
  it('deve carregar a página inicial', () => {
    cy.visit('/')
    cy.title().should('not.be.empty')
    cy.get('body').should('be.visible')
  })
})
