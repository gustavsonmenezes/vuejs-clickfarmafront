import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createStore } from 'vuex'
import Cart from '@/views/Cart.vue'

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

function createMockStore(initialState) {
  return createStore({
    state: {
      cart: initialState
    },
    getters: {
      cartTotal: (state) => {
        if (Array.isArray(state.cart)) {
          return state.cart.reduce((sum, item) => sum + item.price * item.quantity, 0)
        }
        return 0
      },
      cartItemsCount: (state) => Array.isArray(state.cart) ? state.cart.length : 0
    },
    actions: {
      removeFromCart: vi.fn(),
      updateCartQuantity: vi.fn()
    }
  })
}

describe('Cart.vue', () => {
  it('deve mostrar carrinho vazio quando não há itens', () => {
    const store = createMockStore([])
    const wrapper = mount(Cart, {
      global: {
        plugins: [store],
        stubs: ['router-link', 'EmptyCart', 'CartItem', 'OrderSummary', 'CartAIAdvisor']
      }
    })

    expect(wrapper.text()).toContain('Meu Carrinho')
  })

  it('deve listar itens do carrinho', () => {
    const store = createMockStore(mockCartItems)
    const wrapper = mount(Cart, {
      global: {
        plugins: [store],
        stubs: {
          'router-link': { template: '<a><slot /></a>' },
          CartItem: { template: '<div class="cart-item-stub"></div>' },
          OrderSummary: { template: '<div class="order-summary-stub"></div>' },
          CartAIAdvisor: { template: '<div class="advisor-stub"></div>' },
          EmptyCart: { template: '<div class="empty-cart-stub"></div>' }
        }
      }
    })

    expect(wrapper.findAll('.cart-item-stub').length).toBe(2)
    expect(wrapper.find('.order-summary-stub').exists()).toBe(true)
    expect(wrapper.find('.advisor-stub').exists()).toBe(true)
  })
})
