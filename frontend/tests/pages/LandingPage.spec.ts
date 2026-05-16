import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import LandingPage from '../../src/pages/LandingPage.vue'

describe('LandingPage', () => {
  it('offers a visible Keycloak registration CTA', () => {
    const wrapper = mount(LandingPage, {
      global: {
        stubs: {
          RouterLink: {
            props: ['to'],
            template: '<a :href="to"><slot /></a>',
          },
        },
      },
    })

    const registerLink = wrapper.find('a[href="/register"]')

    expect(registerLink.exists()).toBe(true)
    expect(registerLink.text()).toContain('Create account')
  })
})
