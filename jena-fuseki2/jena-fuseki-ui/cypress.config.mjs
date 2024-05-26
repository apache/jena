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

import {defineConfig} from 'cypress'
import codeCoverageTask from '@cypress/code-coverage/task.js'
import vitePreprocessor from 'cypress-vite'
import {resolve} from 'path'

const __dirname = resolve()

export default defineConfig({
  video: false,
  defaultCommandTimeout: 20000,
  execTimeout: 30000,
  taskTimeout: 30000,
  pageLoadTimeout: 30000,
  requestTimeout: 10000,
  responseTimeout: 10000,

  e2e: {
    baseUrl: 'http://localhost:' + (process.env.PORT || 8080),
    setupNodeEvents (on, config) {
      codeCoverageTask(on, config)
      on('before:browser:launch', (browser, launchOptions) => {
        if (browser.name === 'chrome' && browser.isHeadless) {
          launchOptions.args.push('--window-size=1440,1024', '--force-device-scale-factor=1')
        }
        return launchOptions
      })
      on('file:preprocessor', vitePreprocessor({
        configFile: resolve(__dirname, './vite.config.js'),
        mode: 'development',
      }))
      return config
    },
    specPattern: 'tests/e2e/specs/**/*.cy.{js,jsx,ts,tsx}',
    fixturesFolder: 'tests/e2e/fixtures',
    screenshotsFolder: 'tests/e2e/screenshots',
    videosFolder: 'tests/e2e/videos',
    supportFile: 'tests/e2e/support/index.js',
  },
  components: {
    framework: 'vue',
    bundler: 'vite'
  },
  env: {
    codeCoverage: {
      exclude: [
        'src/services/mock/**/*.*',
        'cypress/**/*.*',
        'tests/**/*.*'
      ],
    },
  },
})
