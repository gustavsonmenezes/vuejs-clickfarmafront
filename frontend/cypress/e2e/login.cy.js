describe('Login', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('deve mostrar o formulário de login', () => {
    cy.get('input[type="email"]').first().should('be.visible')
    cy.get('input[type="password"]').should('be.visible')
    cy.get('button[type="submit"]').should('be.visible')
  })

  it('deve exibir erro ao submeter formulário vazio', () => {
    cy.get('form').submit()
    cy.contains('Email é obrigatório').should('be.visible')
  })

  it('deve exibir erro para email inválido', () => {
    cy.get('input[type="email"]').first().type('invalido')
    cy.get('form').submit()
    cy.contains('Email inválido').should('be.visible')
  })

  it('deve mostrar botão de login com Google', () => {
    cy.get('.google-login-wrapper').should('be.visible')
    cy.contains('ou').should('be.visible')
  })
})
