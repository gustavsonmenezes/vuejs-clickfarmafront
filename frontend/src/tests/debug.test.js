import { describe, it, expect } from 'vitest'

describe('Debug - Caracteres', () => {
  it('deve mostrar os caracteres exatos', () => {
    const formatPrice = (price) => {
      if (!price && price !== 0) return 'R$ 0,00'
      return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
      }).format(price)
    }
    
    const result = formatPrice(15.9)
    console.log('Resultado:', JSON.stringify(result))
    console.log('Comprimento:', result.length)
    console.log('Caracteres:', [...result].map(char => char.charCodeAt(0)))
    
    // Vamos verificar caractere por caractere
    expect(result.charAt(0)).toBe('R')
    expect(result.charAt(1)).toBe('$')
    expect(result.charAt(2)).toBe(' ')
    expect(result.charAt(3)).toBe('1')
    expect(result.charAt(4)).toBe('5')
    expect(result.charAt(5)).toBe(',')
    expect(result.charAt(6)).toBe('9')
    expect(result.charAt(7)).toBe('0')
  })
})
