import { describe, it, expect } from 'vitest'
import { formatPrice, formatDate, capitalizeFirst } from '@/utils/formatters'

describe('Utilitários de Formatação', () => {
  describe('formatPrice', () => {
    it('deve formatar preços corretamente', () => {
      expect(formatPrice(15.9)).toEqual('R$ 15,90')
      expect(formatPrice(100)).toEqual('R$ 100,00')
      expect(formatPrice(0)).toEqual('R$ 0,00')
      expect(formatPrice(1234.56)).toEqual('R$ 1.234,56')
    })

    it('deve lidar com valores inválidos', () => {
      expect(formatPrice(null)).toEqual('R$ 0,00')
      expect(formatPrice(undefined)).toEqual('R$ 0,00')
      expect(formatPrice('')).toEqual('R$ 0,00')
    })
  })

  describe('formatDate', () => {
    it('deve formatar datas no padrão brasileiro', () => {
      expect(formatDate('2024-01-15')).toEqual('15/01/2024')
      expect(formatDate(new Date(2024, 0, 15))).toEqual('15/01/2024')
    })

    it('deve lidar com datas inválidas', () => {
      expect(formatDate(null)).toEqual('')
      expect(formatDate('')).toEqual('')
    })
  })

  describe('capitalizeFirst', () => {
    it('deve capitalizar a primeira letra', () => {
      expect(capitalizeFirst('paracetamol')).toEqual('Paracetamol')
      expect(capitalizeFirst('DIPIRONA')).toEqual('Dipirona')
      expect(capitalizeFirst('')).toEqual('')
    })
  })
})