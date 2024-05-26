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

import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import TableListing from '@/components/dataset/TableListing.vue'

describe('TableListing', () => {
  const mountFunction = options => {
    return mount(TableListing, {
      ...options,
      global: {
        mocks: {
          $fusekiService: {}
        }
      }
    })
  }
  it('shows a message when table is empty', () => {
    const component = mountFunction({
      propsData: {
        fields: [
          'name', 'age'
        ],
        items: []
      }
    })
    expect(component.find('.jena-table-empty').text()).to.equal('No datasets created')
  })
  it('populates the table', () => {
    const component = mountFunction({
      propsData: {
        fields: [
          'name', 'age'
        ],
        items: [
          {
            name: 'Stephen',
            age: 35
          },
          {
            name: 'Bruno',
            age: 38
          },
          {
            name: 'Jean',
            age: 41
          },
          {
            name: 'Joe',
            age: 44
          }
        ]
      }
    })
    // 2 columns
    expect(component.findAll('th[role="columnheader"]').length).to.equal(2)
    // 4 lines
    expect(component.findAll('tbody > tr[role="row"]').length).to.equal(4)
  })
})
