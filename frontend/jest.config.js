module.exports = {
  preset: 'jest-preset-angular',
  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],
  testEnvironment: 'jsdom',
  transformIgnorePatterns: ['node_modules/(?!.*\\.mjs$)'],
  collectCoverage: false,
  coverageDirectory: 'coverage',
  coverageReporters: ['html', 'text', 'lcov'],
  testMatch: ['**/__tests__/**/*.ts', '**/?(*.)+(spec|test).ts'],
  moduleFileExtensions: ['ts', 'html', 'js', 'json', 'mjs'],
};
