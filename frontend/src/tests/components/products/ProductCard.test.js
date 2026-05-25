import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ProductCard from '@/components/products/ProductCard.vue'

const mockStore = {
  dispatch: vi.fn().mockResolvedValue({})
}

describe('ProductCard.vue', () => {
  const productProps = {
    product: {
      id: 1,
      nome: 'Paracetamol 500mg',
      preco: 15.90,
      descricao: 'Analgésico e antitérmico',
      categoriaNome: 'Medicamentos',
      estoque: 10,
      image: 'paracetamol.jpg'
    }
  }

  it('deve renderizar informações do produto', () => {
    const wrapper = mount(ProductCard, {
      props: productProps,
      global: {
        mocks: { $store: mockStore },
        stubs: {
          'router-link': { template: '<a><slot /></a>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Paracetamol 500mg')
    expect(wrapper.text()).toContain('R$')
    expect(wrapper.text()).toContain('15,90')
    expect(wrapper.find('.cf-product-visual').exists()).toBe(true)
  })

  it('deve emitir evento ao adicionar ao carrinho', async () => {
    const wrapper = mount(ProductCard, {
      props: productProps,
      global: {
        mocks: { $store: mockStore },
        stubs: {
          'router-link': { template: '<a><slot /></a>' }
        }
      }
    })

    await wrapper.find('.cf-add-btn').trigger('click')
    await new Promise(r => setTimeout(r, 50))

    expect(wrapper.emitted('add-to-cart')).toBeTruthy()
  })

  it('deve mostrar aviso para produtos sem estoque', () => {
    const wrapper = mount(ProductCard, {
      props: {
        product: {
          ...productProps.product,
          estoque: 0
        }
      },
      global: {
        mocks: { $store: mockStore },
        stubs: {
          'router-link': { template: '<a><slot /></a>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Indisponível')
    expect(wrapper.find('.cf-add-btn').attributes('disabled')).toBeDefined()
  })
})
