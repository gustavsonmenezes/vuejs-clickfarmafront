import { describe, it, expect } from 'vitest'

describe('Testes Básicos - ClickFarma', () => {
  it('deve somar números corretamente', () => {
    expect(1 + 1).toBe(2)
  })

  it('deve verificar strings', () => {
    expect('ClickFarma').toBe('ClickFarma')
  })

  it('deve testar formatação manual', () => {
    const formatPrice = (price) => {
      if (!price && price !== 0) return 'R$ 0,00'
      return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
      }).format(price)
    }
    
    // Use toEqual em vez de toBe para strings
    expect(formatPrice(15.9)).toEqual('R$ 15,90')
    expect(formatPrice(100)).toEqual('R$ 100,00')
    expect(formatPrice(0)).toEqual('R$ 0,00')
  })
})