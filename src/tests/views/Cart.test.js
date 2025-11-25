import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Cart from '@/views/Cart.vue'

describe('Cart.vue', () => {
  const mockCartItems = [
    {
      id: 1,
      name: 'Paracetamol 500mg',
      price: 15.90,
      quantity: 2,
      image: 'paracetamol.jpg'
    },
    {
      id: 2,
      name: 'Dipirona 500mg',
      price: 12.50,
      quantity: 1,
      image: 'dipirona.jpg'
    }
  ]

  it('deve mostrar carrinho vazio quando não há itens', () => {
    const wrapper = mount(Cart, {
      global: {
        mocks: {
          $store: {
            state: {
              cart: {
                items: [],
                total: 0
              }
            },
            getters: {
              'cart/cartTotal': 0,
              'cart/totalItems': 0
            }
          }
        }
      }
    })

    expect(wrapper.text()).toContain('Carrinho vazio')
    expect(wrapper.find('.empty-cart').exists()).toBe(true)
  })

  it('deve listar itens do carrinho', () => {
    const wrapper = mount(Cart, {
      global: {
        mocks: {
          $store: {
            state: {
              cart: {
                items: mockCartItems,
                total: 44.30
              }
            },
            getters: {
              'cart/cartTotal': 44.30,
              'cart/totalItems': 3
            }
          }
        }
      }
    })

    expect(wrapper.text()).toContain('Paracetamol 500mg')
    expect(wrapper.text()).toContain('Dipirona 500mg')
    expect(wrapper.text()).toContain('R$ 44,30')
  })
})