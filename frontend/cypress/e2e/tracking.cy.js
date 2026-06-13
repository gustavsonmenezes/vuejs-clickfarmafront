describe('Rastreamento de Pedidos', () => {
  const mockTrackingData = {
    codigoPedido: 'CF-TEST-001',
    pedidoId: 'CF-TEST-001',
    status: 'ENVIADO',
    dataEnvio: '2025-06-10T10:30:00Z',
    itens: [
      { nome: 'Dipirona 500mg', quantidade: 2, preco: 12.90 }
    ],
    valorTotal: 25.80,
    enderecoEntrega: 'Rua das Flores, 123 · Centro · Recife - PE · CEP 50000-000',
    metodoPagamento: 'MERCADO_PAGO'
  }

  it('deve mostrar estado inicial com campo de busca', () => {
    cy.visit('/rastrear')
    cy.contains('Rastrear Pedido').should('be.visible')
    cy.get('input[placeholder*="código"]').should('be.visible')
    cy.contains('button', 'Rastrear').should('be.visible')
    cy.contains('Digite o código do seu pedido').should('be.visible')
  })

  it('deve exibir resultado do rastreamento para pedido encontrado', () => {
    cy.intercept('GET', '/api/pedidos/CF-TEST-001/rastreio', {
      statusCode: 200,
      body: mockTrackingData
    }).as('trackOrder')

    cy.visit('/rastrear')
    cy.get('input[placeholder*="código"]').type('CF-TEST-001')
    cy.contains('button', 'Rastrear').click()
    cy.wait('@trackOrder')

    cy.get('.track-result').should('be.visible')
    cy.contains('#CF-TEST-001').should('be.visible')
    cy.contains('Enviado').should('be.visible')
    cy.contains('Rua das Flores').should('be.visible')
  })

  it('deve exibir mensagem para pedido não encontrado', () => {
    cy.intercept('GET', '/api/pedidos/INVALIDO/rastreio', {
      statusCode: 404,
      body: { message: 'Pedido não encontrado' }
    }).as('trackNotFound')

    cy.visit('/rastrear')
    cy.get('input[placeholder*="código"]').type('INVALIDO')
    cy.contains('button', 'Rastrear').click()
    cy.wait('@trackNotFound')

    cy.contains('Pedido não encontrado').should('be.visible')
    cy.contains('Verifique o código e tente novamente').should('be.visible')
  })

  it('deve desabilitar botão com campo vazio', () => {
    cy.visit('/rastrear')
    cy.contains('button', 'Rastrear').should('be.disabled')
  })

  it('deve pesquisar ao pressionar Enter', () => {
    cy.intercept('GET', '/api/pedidos/CF-TEST-002/rastreio', {
      statusCode: 200,
      body: {
        ...mockTrackingData,
        codigoPedido: 'CF-TEST-002',
        pedidoId: 'CF-TEST-002'
      }
    }).as('trackOrderEnter')

    cy.visit('/rastrear')
    cy.get('input[placeholder*="código"]').type('CF-TEST-002{enter}')
    cy.wait('@trackOrderEnter')
    cy.get('.track-result').should('be.visible')
  })
})
