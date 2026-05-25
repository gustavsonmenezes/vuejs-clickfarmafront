import { describe, it, expect, vi, beforeAll } from 'vitest'
import { mount } from '@vue/test-utils'
import Login from '@/views/Login.vue'

beforeAll(() => {
  Element.prototype.scrollIntoView = vi.fn()
  window.alert = vi.fn()
})

const mockRouter = {
  push: vi.fn()
}

const mockStore = {
  dispatch: vi.fn().mockResolvedValue({ name: 'Usuário' })
}

describe('Login.vue', () => {
  it('deve renderizar o formulário de login', () => {
    const wrapper = mount(Login, {
      global: {
        mocks: {
          $router: mockRouter,
          $store: mockStore
        },
        stubs: ['router-link']
      }
    })

    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('deve validar campos obrigatórios', async () => {
    const wrapper = mount(Login, {
      global: {
        mocks: {
          $router: mockRouter,
          $store: mockStore
        },
        stubs: ['router-link']
      }
    })

    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.text()).toContain('Email é obrigatório')
  })

  it('deve permitir login com credenciais válidas', async () => {
    const wrapper = mount(Login, {
      global: {
        mocks: {
          $router: mockRouter,
          $store: mockStore
        },
        stubs: ['router-link']
      }
    })

    await wrapper.find('input[type="email"]').setValue('usuario@clickfarma.com')
    await wrapper.find('input[type="password"]').setValue('senha123')
    await wrapper.find('form').trigger('submit.prevent')

    expect(mockStore.dispatch).toHaveBeenCalledWith('login', {
      email: 'usuario@clickfarma.com',
      senha: 'senha123'
    })
    expect(mockRouter.push).toHaveBeenCalledWith('/')
  })
})
