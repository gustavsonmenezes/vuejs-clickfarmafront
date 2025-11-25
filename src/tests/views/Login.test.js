import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Login from '@/views/Login.vue'

// Mock do router e store
const mockRouter = {
  push: vi.fn()
}

const mockStore = {
  dispatch: vi.fn().mockResolvedValue({})
}

describe('Login.vue', () => {
  it('deve renderizar o formulário de login', () => {
    const wrapper = mount(Login, {
      global: {
        mocks: {
          $router: mockRouter,
          $store: mockStore
        }
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
        }
      }
    })

    await wrapper.find('form').trigger('submit.prevent')

    // Deve mostrar mensagens de erro
    expect(wrapper.text()).toContain('Email é obrigatório')
    expect(wrapper.text()).toContain('Senha é obrigatória')
  })

  it('deve permitir login com credenciais válidas', async () => {
    const wrapper = mount(Login, {
      global: {
        mocks: {
          $router: mockRouter,
          $store: mockStore
        }
      }
    })

    // Preenche formulário
    await wrapper.find('input[type="email"]').setValue('usuario@clickfarma.com')
    await wrapper.find('input[type="password"]').setValue('senha123')
    await wrapper.find('form').trigger('submit.prevent')

    // Verifica se a action de login foi chamada
    expect(mockStore.dispatch).toHaveBeenCalledWith('auth/login', {
      email: 'usuario@clickfarma.com',
      password: 'senha123'
    })
  })
})