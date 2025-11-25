import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ProductCard from '@/components/products/ProductCard.vue'

// Mock das funções
const mockAddToCart = vi.fn()

describe('ProductCard.vue', () => {
  const productProps = {
    product: {
      id: 1,
      name: 'Paracetamol 500mg',
      price: 15.90,
      image: 'paracetamol.jpg',
      inStock: true,
      requiresPrescription: false
    }
  }

  it('deve renderizar informações do produto', () => {
    const wrapper = mount(ProductCard, {
      props: productProps
    })

    expect(wrapper.text()).toContain('Paracetamol 500mg')
    expect(wrapper.text()).toContain('R$ 15,90')
    expect(wrapper.find('img').exists()).toBe(true)
  })

  it('deve emitir evento ao adicionar ao carrinho', async () => {
    const wrapper = mount(ProductCard, {
      props: productProps
    })

    await wrapper.find('.add-to-cart-btn').trigger('click')
    
    expect(wrapper.emitted('add-to-cart')).toBeTruthy()
    expect(wrapper.emitted('add-to-cart')[0]).toEqual([productProps.product])
  })

  it('deve mostrar aviso para produtos sem estoque', () => {
    const wrapper = mount(ProductCard, {
      props: {
        product: {
          ...productProps.product,
          inStock: false
        }
      }
    })

    expect(wrapper.text()).toContain('Sem Estoque')
    expect(wrapper.find('.add-to-cart-btn').attributes('disabled')).toBeDefined()
  })
})