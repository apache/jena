/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the 'License'); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'

export default [
  js.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  {
    'files': [
      '**/*.mjs',
      '**/*.js',
      '**/*.vue'
    ],
    'ignores': [
      'node_modules/*',
      'dist/*',
      'tests/coverage/*'
    ],
    'rules': {
      'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
      'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
      'vue/custom-event-name-casing': 'off',
      'vue/multi-word-component-names': 'off',
      'vue/no-reserved-component-names': 'off',
      'vue/order-in-components': 'off',
      'vue/max-attributes-per-line': 'off',
      'vue/attributes-order': 'off',
      'vue/html-self-closing': 'off'
    },
    'languageOptions': {
      'ecmaVersion': 2021,
      'globals': {
        'process': true,
        'describe': true,
        'it': true,
        'Cypress': true,
        'cy': true,
        'expect': true,
        'before': true,
        'beforeEach': true,
        'after': true,
        'afterEach': true,
        '__dirname': true
      }
    }
  }
];
