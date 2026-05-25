import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

describe('LoadingSpinner.vue', () => {
  it('deve renderizar o spinner de carregamento', () => {
    const wrapper = mount(LoadingSpinner, {
      props: { loading: true }
    })

    expect(wrapper.find('.loading-overlay').exists()).toBe(true)
    expect(wrapper.find('.spinner-border').exists()).toBe(true)
  })

  it('deve mostrar texto de carregamento', () => {
    const wrapper = mount(LoadingSpinner, {
      props: {
        loading: true,
        loadingMessage: 'Carregando produtos...'
      }
    })

    expect(wrapper.text()).toContain('Carregando produtos...')
  })
})
