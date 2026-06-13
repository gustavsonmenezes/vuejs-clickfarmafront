describe('Checkout', () => {
  const mockUser = {
    id: 1,
    name: 'Usuário Teste',
    email: 'teste@clickfarma.com.br',
    token: 'eyJhbGciOiJIUzI1NiJ9.mock-token-for-testing'
  }

  beforeEach(() => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: mockUser
    }).as('loginRequest')

    cy.intercept('POST', '/api/pedidos', {
      statusCode: 200,
      body: {
        id: 999,
        codigoPedido: 'CF-TEST-001',
        linkPagamento: null
      }
    }).as('createOrder')
  })

  it('deve redirecionar para login se não estiver autenticado', () => {
    cy.visit('/checkout')
    cy.url().should('include', '/login')
  })

  it('deve realizar checkout completo com sucesso', () => {
    cy.visit('/login')
    cy.get('input[type="email"]').first().type('teste@clickfarma.com.br')
    cy.get('input[type="password"]').type('senha123')
    cy.get('button[type="submit"]').click()
    cy.wait('@loginRequest')

    cy.url().should('eq', Cypress.config().baseUrl + '/')

    cy.window().then(win => {
      const store = win.__vue_app__._instance.proxy.$store
      store.commit('ADD_TO_CART', { id: 10, nome: 'Dipirona 500mg', preco: 12.90, quantity: 2 })
      store.commit('ADD_TO_CART', { id: 20, nome: 'Paracetamol 750mg', preco: 8.50, quantity: 1 })
    })

    cy.visit('/checkout')
    cy.url().should('include', '/checkout')

    cy.get('input[placeholder*="Rua"]').type('Rua das Flores')
    cy.get('input[placeholder*="Número"]').type('123')
    cy.get('input[placeholder*="Complemento"]').type('Apto 42')
    cy.get('input[placeholder*="Bairro"]').type('Centro')
    cy.get('input[placeholder*="Cidade"]').type('Recife')
    cy.get('input[placeholder*="UF"]').type('PE')
    cy.get('input[placeholder*="CEP"]').type('50000-000')

    cy.contains('button', 'FINALIZAR E PAGAR').click()
    cy.wait('@createOrder')

    cy.url().should('include', '/sucesso-pagamento')
    cy.contains('Pedido Confirmado').should('be.visible')
  })

  it('deve exibir erro se endereço estiver incompleto', () => {
    cy.visit('/login')
    cy.get('input[type="email"]').first().type('teste@clickfarma.com.br')
    cy.get('input[type="password"]').type('senha123')
    cy.get('button[type="submit"]').click()
    cy.wait('@loginRequest')

    cy.window().then(win => {
      const store = win.__vue_app__._instance.proxy.$store
      store.commit('ADD_TO_CART', { id: 10, nome: 'Dipirona 500mg', preco: 12.90, quantity: 1 })
    })

    cy.visit('/checkout')

    cy.contains('button', 'FINALIZAR E PAGAR').click()
    cy.contains('Preencha os dados de entrega para continuar').should('be.visible')
  })
})
