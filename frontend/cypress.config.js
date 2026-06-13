import { defineConfig } from 'cypress'

export default defineConfig({
  projectId: 'f25z4v',
  e2e: {
    baseUrl: 'http://localhost:8081',
    supportFile: 'cypress/support/e2e.js',
    specPattern: 'cypress/e2e/**/*.cy.js',
    viewportWidth: 1280,
    viewportHeight: 720,
  },
})
