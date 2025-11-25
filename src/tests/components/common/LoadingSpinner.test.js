import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

describe('LoadingSpinner.vue', () => {
  it('deve renderizar o spinner de carregamento', () => {
    const wrapper = mount(LoadingSpinner)
    
    expect(wrapper.find('.loading-spinner').exists()).toBe(true)
    expect(wrapper.find('.spinner').exists()).toBe(true)
  })

  it('deve mostrar texto de carregamento', () => {
    const wrapper = mount(LoadingSpinner, {
      props: {
        message: 'Carregando produtos...'
      }
    })
    
    expect(wrapper.text()).toContain('Carregando produtos...')
  })
})