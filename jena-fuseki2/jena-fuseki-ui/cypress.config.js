/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
