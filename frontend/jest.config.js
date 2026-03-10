module.exports = {
  preset: '@vue/cli-plugin-unit-jest',
  testMatch: [
    '**/tests/**/*.test.[jt]s?(x)',
    '**/tests/**/*.spec.[jt]s?(x)',
    '**/src/**/*.test.[jt]s?(x)',
    '**/src/**/*.spec.[jt]s?(x)'
  ],
  moduleNameMapping: {
    '^@/(.*)$': '<rootDir>/src/$1'
  },
  collectCoverage: true,
  collectCoverageFrom: [
    'src/**/*.{js,vue}',
    '!src/main.js',
    '!src/router/**',
    '!src/services/**',
    '!src/tests/**'
  ],
  coverageReporters: ['text', 'html'],
  testEnvironment: 'jsdom',
  setupFiles: ['<rootDir>/src/tests/setup.js']
}