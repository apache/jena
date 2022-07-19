const { defineConfig } = require('cypress')

module.exports = defineConfig({
  video: false,
  defaultCommandTimeout: 7500,
  execTimeout: 15000,
  taskTimeout: 15000,
  pageLoadTimeout: 15000,
  requestTimeout: 7500,
  responseTimeout: 7500,
  fixturesFolder: 'tests/e2e/fixtures',
  screenshotsFolder: 'tests/e2e/screenshots',
  videosFolder: 'tests/e2e/videos',

  e2e: {
    setupNodeEvents (on, config) {
      return require('./tests/e2e/plugins/index.js')(on, config)
    },
    specPattern: 'tests/e2e/specs/**/*.cy.{js,jsx,ts,tsx}',
    supportFile: 'tests/e2e/support/index.js'
  },

  component: {
    devServer: {
      framework: 'vue-cli',
      bundler: 'webpack'
    }
  }
})
